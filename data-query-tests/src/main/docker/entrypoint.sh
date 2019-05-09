#!/usr/bin/env bash

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
  test [--include-category <category>] [--exclude-category <category>] [-Dkey=value] <name> [name] [...]
  smoke-test
  regression-test
  crawler-test


Example
  test \
    --exclude-category gov.va.api.health.sentinel.categories.Local \
    --include-category gov.va.api.health.sentinel.categories.Manual \
    --trust example.us-gov-west-1.elb.amazonaws.com
    -Dlab.client-id=12345 \
    -Dlab.client-secret=ABCDEF \
    -Dlab.user-password=secret \
    gov.va.api.health.sentinel.CrawlerUsingOAuthTest

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
  exit $?
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

# TODO get running on the environments
#
# TODO Test         lab                   qa            production          staging
# TODO SMOKE          x                    x
# TODO Regression   2-3f/21              11f/27 run
# TODO Crawler      x w/ magic pat    fails pat id?

doSmokeTest() {
  setupForAutomation

  INCLUDE_CATEGORY=$SENTINEL_SMOKE_TEST_CATEGORY
  doTest
}

doRegressionTest() {
  setupForAutomation

  INCLUDE_CATEGORY=$SENTINEL_REGRESSION_TEST_CATEGORY
  doTest

  #doCrawlerTest
}

doCrawlerTest() {
  setupForAutomation

  INCLUDE_CATEGORY=$SENTINEL_CRAWLER_TEST_CATEGORY
  doTest $SENTINEL_CRAWLER
}

checkVariablesForAutomation() {
  # Check out both the deployment contract variables and data query specific variables.
  for i in "KBS_LOAD_BALANCER" "K8S_ENVIRONMENT" "SENTINEL_ENV" "TOKEN" "FORCE_JARGONAUT" \
    "SENTINEL_SMOKE_TEST_CATEGORY" "SENTINEL_REGRESSION_TEST_CATEGORY" \
    "SENTINEL_CRAWLER_TEST_CATEGORY" "SENTINEL_CRAWLER" "DATA_QUERY_API_PATH" \
    "DATA_QUERY_REPLACE_URL" "USER_PASSWORD" "CLIENT_ID" "CLIENT_SECRET" "PATIENT_ID"; do
    [ -z ${!i} ] && usage "Variable $i must be specified."
  done
}

setupForAutomation() {
  checkVariablesForAutomation

  trustServer $KBS_LOAD_BALANCER

  SYSTEM_PROPERTIES="$WEB_DRIVER_PROPERTIES \
    -Dsentinel=$SENTINEL_ENV \
    -Daccess-token=$TOKEN \
    -Djargonaut=$FORCE_JARGONAUT \
    -Dsentinel.argonaut.url=https://$KBS_LOAD_BALANCER \
    -Dsentinel.argonaut.api-path=$DATA_QUERY_API_PATH \
    -Dsentinel.argonaut.url.replace=$DATA_QUERY_REPLACE_URL \
    -D${K8S_ENVIRONMENT}.user-password=$USER_PASSWORD \
    -D${K8S_ENVIRONMENT}.client-id=$CLIENT_ID \
    -D${K8S_ENVIRONMENT}.client-secret=$CLIENT_SECRET \
    -Dpatient-id=$PATIENT_ID"
}

ARGS=$(getopt -n $(basename ${0}) \
    -l "exclude-category:,include-category:,debug,help,trust:" \
    -o "e:i:D:h" -- "$@")
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
