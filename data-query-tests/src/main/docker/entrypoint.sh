#!/usr/bin/env bash

set -o pipefail

[ -z "$SENTINEL_BASE_DIR" ] && SENTINEL_BASE_DIR=/sentinel
cd $SENTINEL_BASE_DIR
MAIN_JAR=$(find -maxdepth 1 -name "data-query-tests-*.jar" -a -not -name "data-query-tests-*-tests.jar")
TESTS_JAR=$(find -maxdepth 1 -name "data-query-tests-*-tests.jar")
WEB_DRIVER_PROPERTIES="-Dwebdriver.chrome.driver=/usr/local/bin/chromedriver -Dwebdriver.chrome.headless=true"
SYSTEM_PROPERTIES=$WEB_DRIVER_PROPERTIES
EXCLUDE_CATEGORY=
INCLUDE_CATEGORY=

usage() {
cat <<EOF
Commands
  list-tests
  list-categories
  test [--include-category <category>] [--exclude-category <category>] [--trust <host>] [-Dkey=value] <name> [name] [...]
  smoke-test
  regression-test [--skip-crawler]
  crawler-test


Example
  test\
    --exclude-category gov.va.api.health.sentinel.categories.Local\
    --include-category gov.va.api.health.sentinel.categories.Manual\
    --trust example.something.elb.amazonaws.com\
    -Dlab.client-id=12345\
    -Dlab.client-secret=ABCDEF\
    -Dlab.user-password=secret\
    gov.va.api.health.dataquery.tests.UsingMagicPatientCrawlerTest

Docker Run Examples
  docker run --rm --init --network=host\
  --env-file qa.testvars --env K8S_LOAD_BALANCER=example.com --env K8S_ENVIRONMENT=qa\
  vasdvp/health-apis-data-query-tests:latest smoke-test

  docker run --rm --init --network=host\
  --env-file production.testvars --env K8S_LOAD_BALANCER=example.com --env K8S_ENVIRONMENT=production\
  vasdvp/health-apis-data-query-tests crawler-test

  docker run --rm --init --network=host\
  --env-file lab.testvars --env K8S_LOAD_BALANCER=example.com --env K8S_ENVIRONMENT=lab\
  vasdvp/health-apis-data-query-tests:1.0.210 regression-test -s
$1
EOF
exit 1
}

trustServer() {
  local host=$1
  curl -sk https://$host > /dev/null 2>&1
  [ $? == 6 ] && return
  echo "Trusting $host"
  local cacertsDir="$JAVA_HOME/jre/lib/security/cacerts"
  [ -f "$JAVA_HOME/lib/security/cacerts" ] && cacertsDir="$JAVA_HOME/lib/security/cacerts"
  keytool -printcert -rfc -sslserver $host > $host.pem
  keytool \
    -importcert \
    -file $host.pem \
    -alias $host \
    -keystore $cacertsDir \
    -storepass changeit \
    -noprompt
}

defaultTests() {
  doListTests | grep 'IT$'
}

doTest() {
  local tests="$@"
  [ -z "$tests" ] && tests=$(defaultTests)
  local filter
  [ -n "$EXCLUDE_CATEGORY" ] && filter+=" --filter=org.junit.experimental.categories.ExcludeCategories=$EXCLUDE_CATEGORY"
  [ -n "$INCLUDE_CATEGORY" ] && filter+=" --filter=org.junit.experimental.categories.IncludeCategories=$INCLUDE_CATEGORY"
  local noise="org.junit"
  noise+="|groovy.lang.Meta"
  noise+="|io.restassured.filter"
  noise+="|io.restassured.internal"
  noise+="|java.lang.reflect"
  noise+="|java.net"
  noise+="|org.apache.http"
  noise+="|org.codehaus.groovy"
  noise+="|sun.reflect"
  java -cp "$(pwd)/*" $SYSTEM_PROPERTIES org.junit.runner.JUnitCore $filter $tests \
    | grep -vE "^	at ($noise)"

  # Exit on failure otherwise let other actions run.
  [ $? != 0 ] && exit 1
}

doListTests() {
  jar -tf $TESTS_JAR \
    | grep -E '(IT|Test)\.class' \
    | sed 's/\.class//' \
    | tr / . \
    | sort
}

doListCategories() {
  jar -tf $MAIN_JAR \
    | grep -E 'gov/va/api/health/sentinel/categories/.*\.class|gov/va/api/health/dataquery/tests/categories/.*\.class' \
    | sed 's/\.class//' \
    | tr / . \
    | sort
}

doSmokeTest() {
  setupForAutomation

  INCLUDE_CATEGORY=$SENTINEL_SMOKE_TEST_CATEGORY
  doTest
}

doRegressionTest() {
  setupForAutomation

  INCLUDE_CATEGORY=$SENTINEL_REGRESSION_TEST_CATEGORY
  doTest

  doCrawlerTest
}

doCrawlerTest() {
  # If crawler test was specified and not explicitly told to skip then it's crawl time.
  if [ "$SKIP_CRAWLER" == "true" -o -z "$SENTINEL_CRAWLER" ]; then return; fi

  setupForAutomation

  # Wipe out any included or excluded categories to make sure we pick up the crawler test class
  INCLUDE_CATEGORY=
  EXCLUDE_CATEGORY=

  # This way, the ITs use defaults when INTERNAL_API_PATH isnt set,
  # but the crawler will use the internal path always.
  [ -z "$INTERNAL_API_PATH" ] && INTERNAL_API_PATH="/data-query"
  [[ "$INTERNAL_API_PATH" =~ .*/$ ]] && INTERNAL_API_PATH=${INTERNAL_API_PATH:0:-1}
  [ -z "$DSTU2_CRAWL_PATH" ] && DSTU2_CRAWL_PATH=${INTERNAL_API_PATH}/dstu2
  [ -z "$R4_CRAWL_PATH" ] && R4_CRAWL_PATH=${INTERNAL_API_PATH}/r4

  # Crawl DSTU2
  addToSystemProperties "crawler.url.replace" "${DATA_QUERY_REPLACEMENT_URL_PREFIX}/dstu2"
  addToSystemProperties "crawler.base-url" "${DQ_URL}${DSTU2_CRAWL_PATH}"
  doTest $SENTINEL_CRAWLER

  # Crawl R4
  addToSystemProperties "crawler.url.replace" "${DATA_QUERY_REPLACEMENT_URL_PREFIX}/r4"
  addToSystemProperties "crawler.base-url" "${DQ_URL}${R4_CRAWL_PATH}"
  doTest $SENTINEL_CRAWLER
}

checkVariablesForAutomation() {
  # Check out required deployment variables and data query specific variables.
  for param in "K8S_LOAD_BALANCER" "K8S_ENVIRONMENT" "SENTINEL_ENV" "TOKEN" \
    "SENTINEL_SMOKE_TEST_CATEGORY" "SENTINEL_REGRESSION_TEST_CATEGORY" \
    "DATA_QUERY_REPLACEMENT_URL_PREFIX" "USER_PASSWORD" "CLIENT_ID" "CLIENT_SECRET" "PATIENT_ID"; do
    [ -z ${!param} ] && usage "Variable $param must be specified."
  done
}

addToSystemProperties() {
  SYSTEM_PROPERTIES+=" -D$1=$2"
}

setupForAutomation() {
  checkVariablesForAutomation

  trustServer $K8S_LOAD_BALANCER

  SYSTEM_PROPERTIES="$WEB_DRIVER_PROPERTIES \
    -Dsentinel=$SENTINEL_ENV \
    -Daccess-token=$TOKEN \
    -Draw-token=$RAW_TOKEN \
    -Dbulk-token=$BULK_TOKEN \
    -D${K8S_ENVIRONMENT}.user-password=$USER_PASSWORD \
    -D${K8S_ENVIRONMENT}.client-id=$CLIENT_ID \
    -D${K8S_ENVIRONMENT}.client-secret=$CLIENT_SECRET \
    -Dcrawler.allow-query-url-pattern=$ALLOW_URLS \
    -Dpatient-id=$PATIENT_ID"

  [ -z "$DQ_URL" ] && DQ_URL=https://$K8S_LOAD_BALANCER

  for property in \
    "sentinel.data-query.public-url" "sentinel.internal.url" \
    "sentinel.dstu2.url" "sentinel.stu3.url" "sentinel.r4.url"
  do
    addToSystemProperties "$property" "$DQ_URL"
  done

  [ -n "$INTERNAL_API_PATH" ] && addToSystemProperties "sentinel.internal.api-path" "$INTERNAL_API_PATH"
  [ -n "$DSTU2_API_PATH" ] && addToSystemProperties "sentinel.dstu2.api-path" "$DSTU2_API_PATH"
  [ -n "$STU3_API_PATH" ] && addToSystemProperties "sentinel.stu3.api-path" "$STU3_API_PATH"
  [ -n "$R4_API_PATH" ] && addToSystemProperties "sentinel.r4.api-path" "$R4_API_PATH"

  # This is an optional, and discouraged flag.
  [ -n "$SENTINEL_CRAWLER_IGNORES" ] && addToSystemProperties "crawler.ignores" "$SENTINEL_CRAWLER_IGNORES"
}

ARGS=$(getopt -n $(basename ${0}) \
    -l "exclude-category:,include-category:,debug,help,trust:,skip-crawler" \
    -o "e:i:D:hs" -- "$@")
[ $? != 0 ] && usage
eval set -- "$ARGS"
while true
do
  case "$1" in
    -e|--exclude-category) EXCLUDE_CATEGORY=$2;;
    -i|--include-category) INCLUDE_CATEGORY=$2;;
    -D) SYSTEM_PROPERTIES+=" -D$2";;
    --debug) set -x;;
    -h|--help) usage "halp! what this do?";;
    --trust) trustServer $2;;
    -s|--skip-crawler) SKIP_CRAWLER="true";;
    --) shift;break;;
  esac
  shift;
done

[ $# == 0 ] && usage "No command specified"
COMMAND=$1
shift

case "$COMMAND" in
  t|test) doTest $@;;
  lc|list-categories) doListCategories;;
  lt|list-tests) doListTests;;
  s|smoke-test) doSmokeTest;;
  r|regression-test) doRegressionTest;;
  c|crawler-test) doCrawlerTest;;
  *) usage "Unknown command: $COMMAND";;
esac

exit 0
