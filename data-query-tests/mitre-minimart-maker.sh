#!/usr/bin/env bash

COMMAND=$1
DIRECTORY=$2
RESOURCE_TYPE=$3

cd $(dirname $0)

pushToDatabase() {
  [ -z "$DIRECTORY" ] && usage "Directory is a Required Param." && exit 1
  [ -z "$RESOURCE_TYPE" ] && usage "Resource Type is a Required Param." && exit 1
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
  [ -z "$DIRECTORY" ] && usage "Directory is a Required Param." && exit 1
  [ -z "$RESOURCE_TYPE" ] && usage "Resource Type is a Required Param." && exit 1
  mvn -f ../data-query test-compile && \
    mvn -f ../data-query \
    -P'!standard' \
    -Pmitre-minimart-maker \
    generate-resources \
    -DresourceType="$RESOURCE_TYPE" \
    -DinputDirectory="$DIRECTORY"
}

minimartDatabase() {
  java -jar ~/.m2/repository/com/h2database/h2/1.4.197/h2-1.4.197.jar -url jdbc:h2:./src/test/resources/minimart -user sa -password sa
}

usage() {
cat <<EOF
Commands:
  transformToDatamart <directory-to-read-files-from> <resource-name>
    Takes all files for the given resource in the directory and transforms them to datamart schema
  pushToDb <directory-to-read-files-from> <resource-name>
    Pushes all files for the given resource and directory to a local h2 repository
  openDb
    Open the database that was created by the pushToDb command

Examples:
  transformToDatamart "$(pwd)/data-query-tests/target" AllergyIntolerance
  pushToDb "$(pwd)/data-query-tests/target/fhir-to-datamart" AllergyIntolerance

$1
EOF
}

case $COMMAND in
  pushToDb) pushToDatabase ;;
  transformToDatamart) transformFhirToDatamart ;;
  openDb) minimartDatabase ;;
  -h|--help) usage ;;
  *) usage "Invalid Command: $COMMAND" && exit 1 ;;
esac
