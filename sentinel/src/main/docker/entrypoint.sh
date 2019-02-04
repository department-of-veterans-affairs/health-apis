#!/usr/bin/env bash

[ -z "$SENTINEL_BASE_DIR" ] && SENTINEL_BASE_DIR=/sentinel
cd $SENTINEL_BASE_DIR
SENTINEL_JAR=$(find -maxdepth 1 -name "sentinel-*.jar" -a -not -name "sentinel-*-tests.jar")
SENTINEL_TEST_JAR=$(find -maxdepth 1 -name "sentinel-*-tests.jar")
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
    --exclude-category gov.va.health.api.sentinel.categories.NotInProd \
    --include-category gov.va.health.api.sentinel.categories.NotInLocal \
    -Dlab.client-id=12345 \
    -Dlab.client-secret=ABCDEF \
    -Dlab.user-password=secret \
    gov.va.health.api.sentinel.LabCrawlerTest

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
  [ -n "$EXCLUDE_CATEGORY" ] && filter="--filter=org.junit.experimental.categories.ExcludeCategories=$EXCLUDE_CATEGORY"
  [ -n "$INCLUDE_CATEGORY" ] && filter+=" org.junit.experimental.categories.IncludeCategories=$INCLUDE_CATEGORY"
  java -cp "$(pwd)/*" $SYSTEM_PROPERTIES org.junit.runner.JUnitCore $filter $tests
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
