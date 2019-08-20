#!/usr/bin/env bash

COMMAND=$1
DIRECTORY=$2
RESOURCE_TYPE=$3

cd $(dirname $0)
PROJECT_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version \
  | grep -v -E 'INFO|Download')

pushToDatabase() {
  [ -z "$DIRECTORY" ] && echo "Directory is a Required Param." && exit 1
  [ -z "$RESOURCE_TYPE" ] && echo "Resource Type is a Required Param." && exit 1
  outputFile="./src/test/resources/minimart"
  [ -f "$outputFile" ] && rm -v "$ouputFile/*"
  mvn -f ../data-query test-compile && \
  mvn -f ../data-query \
    -P'!standard' \
    -Pmitre-minimart-maker \
    exec:java@pushToDatabase \
    generate-resources \
    -DresourceType="$RESOURCE_TYPE" \
    -DinputDirectory="$DIRECTORY" \
    -DoutputFile="$outputFile" \
  -Dorg.jboss.logging.provider=jdk \
  -Djava.util.logging.config.file=nope

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