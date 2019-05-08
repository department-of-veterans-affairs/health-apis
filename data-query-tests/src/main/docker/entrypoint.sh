#!/usr/bin/env bash

[ -z "$SENTINEL_BASE_DIR" ] && SENTINEL_BASE_DIR=/sentinel
cd $SENTINEL_BASE_DIR
MAIN_JAR=$(find -maxdepth 1 -name "data-query-tests-*.jar" -a -not -name "data-query-tests-*-tests.jar")
TESTS_JAR=$(find -maxdepth 1 -name "data-query-tests-*-tests.jar")
SYSTEM_PROPERTIES="-Dwebdriver.chrome.driver=/usr/local/bin/chromedriver -Dwebdriver.chrome.headless=true"
EXCLUDE_CATEGORY=
INCLUDE_CATEGORY=

usage() {
cat <<EOF
Commands
  list-tests
  list-categories
  test [--include-category <category>] [--exclude-category <category>] [--trust <host>] [-Dkey=value] <name> [name] [...]
  test [--include-category <category>] [--exclude-category <category>] [-Dkey=value] <name> [name] [...]
  regression-test
  smoke-test


Example
  test \
    --exclude-category gov.va.api.health.sentinel.categories.Local \
    --include-category gov.va.api.health.sentinel.categories.Manual \
    --trust example.us-gov-west-1.elb.amazonaws.com
    -Dlab.client-id=12345 \
    -Dlab.client-secret=ABCDEF \
    -Dlab.user-password=secret \
    gov.va.api.health.sentinel.LabCrawlerTest

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

checkVariablesForAutomation() {
  # The deployment test contract variables
  [ -z "$KBS_LOAD_BALANCER" ] && usage "Variable KBS_LOAD_BALANCER must be specified."

  # DataQuery test specific variables
  [ -z "$TRUST_SERVER" ] && usage "Variable TRUST_SERVER must be specified."
  [ -z "$TOKEN" ] && usage "Variable TOKEN must be specified."
  [ -z "$DATA_QUERY_API_PATH" ] && usage "Variable DATA_QUERY_API_PATH must be specified."
  [ -z "$DATA_QUERY_REPLACE_URL" ] && usage "Variable DATA_QUERY_REPLACE_URL must be specified."
  [ -z "$SENTINEL_ENV" ] && usage "Variable SENTINEL_ENV must be specified."
  [ -z "$SENTINEL_SMOKE_TEST_CATEGORY" ] && \
    usage "Variable SENTINEL_SMOKE_TEST_CATEGORY must be specified."
  [ -z "$SENTINEL_REGRESSION_TEST_CATEGORY" ] && \
    usage "Variable SENTINEL_REGRESSION_TEST_CATEGORY must be specified."
  [ -z "$SENTINEL_CRAWLER_TEST_CATEGORY" ] && \
    usage "Variable SENTINEL_CRAWLER_TEST_CATEGORY must be specified."
  [ -z "$SENTINEL_CRAWLER" ] && usage "Variable SENTINEL_CRAWLER must be specified."
  [ -z "$USER_PASSWORD" ] && usage "Variable USER_PASSWORD must be specified."
  [ -z "$CLIENT_ID" ] && usage "Variable CLIENT_ID must be specified."
  [ -z "$CLIENT_SECRET" ] && usage "Variable CLIENT_SECRET must be specified."
}

setupForAutomation() {
  checkVariablesForAutomation

  trustServer $TRUST_SERVER

  SYSTEM_PROPERTIES+=" -Dsentinel=$SENTINEL_ENV \
    -Daccess-token=$TOKEN \
    -Dsentinel.argonaut.url=$KBS_LOAD_BALANCER \
    -Dlab.user-password=$USER_PASSWORD \
    -Dlab.client-id=$CLIENT_ID \
    -Dlab.client-secret=$CLIENT_SECRET \
    -Dpatient-id=$PATIENT_ID \
    -Dsentinel.argonaut.api-path=$DATA_QUERY_API_PATH \
    -Dsentinel.argonaut.url.replace=$DATA_QUERY_REPLACE_URL"
}

# TODO all regressions and crawler get running
# TODO get running on the two environments or three environments?
# TODO bring in joshes docker changes
# TODO update the usage for required vars
# TODO split parent...
# TODO figure out what goes up to base...
# TODO does parent need some care and feeding
# TODO set crawler as a type?

doRegressionTest() {
  setupForAutomation

  # Run IT tests for the specific environment
  INCLUDE_CATEGORY=$SENTINEL_REGRESSION_TEST_CATEGORY
  doTest

  # Run the crawler if there is one for the specific environment
  INCLUDE_CATEGORY=$SENTINEL_CRAWLER_TEST_CATEGORY
  doTest $SENTINEL_CRAWLER
}

doSmokeTest() {
  setupForAutomation

  INCLUDE_CATEGORY=$SENTINEL_SMOKE_TEST_CATEGORY
  doTest
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
  r|regression-test) doRegressionTest;;
  s|smoke-test) doSmokeTest;;
  *) usage "Unknown command: $COMMAND";;
esac

exit 0
