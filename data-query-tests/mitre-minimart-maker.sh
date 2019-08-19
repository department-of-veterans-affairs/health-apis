#!/usr/bin/env bash

COMMAND=$1
DIRECTORY=$2
RESOURCE_TYPE=$3

cd $(dirname $0)
PROJECT_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version \
  | grep -v -E 'INFO|Download')

pushToDatabase() {
  ouputFile="./src/test/resources/minimart"
  [ -f "$outputFile" ] && rm -v "$ouputFile/*"
  [ ! -f "../data-query/target/data-query-$PROJECT_VERSION-tests.jar" ] \
    && echo "Build project before running." \
    && exit 1
  java -cp ../data-query/target/data-query-$PROJECT_VERSION-tests.jar \
    gov.va.api.health.dataquery.tools.minimart.MitreMinimartMaker \
    -DresourceToSync="$RESOURCE_TYPE" \
    -Ddirectory="$DIRECTORY" \
    -DoutputFile="$outputFile"
}

transformFhirToDatamart() {
  echo "WHY!?!"
}

minimartDatabase() {
  java -jar ~/.m2/repository/com/h2database/h2/1.4.197/h2-1.4.197.jar -url jdbc:h2:./src/test/resources/minimart -user sa -password sa
}

case $COMMAND in
  pushToDb) pushToDatabase ;;
  transformToDatamart) transformFhirToDatamart ;;
#  mitreMinimart) transformFhirToDatamart && pushToDatabase ;;
  openDb) minimartDatabase ;;
  *) echo "Invalid Command: $COMMAND" && exit 1;;
esac