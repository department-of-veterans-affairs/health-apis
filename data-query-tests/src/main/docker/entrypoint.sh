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
  test [--include-category <category>] [--exclude-category <category>] [-Dkey=value] <name> [name] [...]


Example
  test \
    --exclude-category gov.va.api.health.sentinel.categories.Manual \
    --include-category gov.va.api.health.sentinel.categories.Local \
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
  trustServer qa-argonaut.lighthouse.va.gov
  trustServer staging-argonaut.lighthouse.va.gov
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


ARGS=$(getopt -n $(basename ${0}) \
    -l "exclude-category:,include-category:,debug,help" \
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
  
  *) usage "Unknown command: $COMMAND";;
esac

exit 0
