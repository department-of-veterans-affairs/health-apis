#!/usr/bin/env bash

[ -z "$SENTINEL_BASE_DIR" ] && SENTINEL_BASE_DIR=/sentinel
cd $SENTINEL_BASE_DIR
SENTINEL_JAR=$(find -maxdepth 1 -name "sentinel-*.jar" -a -not -name "sentinel-*-tests.jar")
SENTINEL_TEST_JAR=$(find -maxdepth 1 -name "sentinel-*-tests.jar")
SYSTEM_PROPERTIES="-Dwebdriver.chrome.driver=/usr/local/bin/chromedriver -Dwebdriver.chrome.headless=true"
CATEGORY=

usage() {
cat <<EOF
Commands
  list-tests
  list-categories
  test [--category <category>] [-Dkey=value] <name> [name] [...]

Example
  test \
    --category gov.va.health.api.sentinel.categories.Lab \
    -Dlab.client-id=12345 \
    -Dlab.client-secret=ABCDEF \
    -Dlab.user-password=secret \
    gov.va.health.api.sentinel.LabTest

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
  [ -n "$CATEGORY" ] && filter="--filter=org.junit.experimental.categories.IncludeCategories=$CATEGORY"
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
  jar -tf $SENTINEL_TEST_JAR \
    | grep -E '(IT|Test)\.class' \
    | sed 's/\.class//' \
    | tr / . \
    | sort
}

doListCategories() {
  jar -tf $SENTINEL_JAR \
    | grep -E 'gov/va/health/api/sentinel/categories/.*\.class' \
    | sed 's/\.class//' \
    | tr / . \
    | sort
}


ARGS=$(getopt -n $(basename ${0}) \
    -l "category:,debug,help" \
    -o "c:D:h" -- "$@")
[ $? != 0 ] && usage
eval set -- "$ARGS"
while true
do
  case "$1" in
    -c|--category) CATEGORY=$2;;
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
