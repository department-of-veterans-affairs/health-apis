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
    gov.va.api.health.sentinel.UsingMagicPatientCrawlerTest

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
  keytool -printcert -rfc -sslserver $host > $host.pem
  keytool \
    -importcert \
    -file $host.pem \
    -alias $host \
    -keystore $JAVA_HOME/jre/lib/security/cacerts \
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
    | grep -E 'gov/va/api/health/sentinel/categories/.*\.class' \
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
  if [ -z "$SKIP_CRAWLER" ] && [ -n "$SENTINEL_CRAWLER" ]; then
    setupForAutomation

    INCLUDE_CATEGORY=
    doTest $SENTINEL_CRAWLER
  fi
}

checkVariablesForAutomation() {
  # Check out required deployment variables and data query specific variables.
  for param in "K8S_LOAD_BALANCER" "K8S_ENVIRONMENT" "SENTINEL_ENV" "TOKEN" "JARGONAUT" \
    "SENTINEL_SMOKE_TEST_CATEGORY" "SENTINEL_REGRESSION_TEST_CATEGORY" \
    "DATA_QUERY_API_PATH" "DATA_QUERY_REPLACE_URL" "USER_PASSWORD" \
    "CLIENT_ID" "CLIENT_SECRET" "PATIENT_ID"; do
    [ -z ${!param} ] && usage "Variable $param must be specified."
  done
}

setupForAutomation() {
  checkVariablesForAutomation

  trustServer $K8S_LOAD_BALANCER

  SYSTEM_PROPERTIES="$WEB_DRIVER_PROPERTIES \
    -Dsentinel=$SENTINEL_ENV \
    -Daccess-token=$TOKEN \
    -Djargonaut=$JARGONAUT \
    -Dsentinel.argonaut.url=https://$K8S_LOAD_BALANCER \
    -Dsentinel.argonaut.api-path=$DATA_QUERY_API_PATH \
    -Dsentinel.argonaut.url.replace=$DATA_QUERY_REPLACE_URL \
    -D${K8S_ENVIRONMENT}.user-password=$USER_PASSWORD \
    -D${K8S_ENVIRONMENT}.client-id=$CLIENT_ID \
    -D${K8S_ENVIRONMENT}.client-secret=$CLIENT_SECRET \
    -Dpatient-id=$PATIENT_ID"

  # This is an optional, and discouraged flag.
  [ -n "$SENTINEL_CRAWLER_IGNORES" ] \
    && SYSTEM_PROPERTIES+=" -Dsentinel.argonaut.crawler.ignores=$SENTINEL_CRAWLER_IGNORES"
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
    -s|--skip-crawler) SKIP_CRAWLER="TRUE";;
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
