#!/usr/bin/env bash

COMMAND=$1

cd $(dirname $0)

minimartIds() {
  [ "${OPEN_DB:-false}" == 'true' ] && openDatabase '../../heath-apis-ids/ids/minimartIds'
  [ -z "$START$STOP" ] && usage "Missing options for minimartIds" && exit 1
  [ "${START:-false}" == 'true' ] && startMinimartApp
  [ "${STOP:-false}" == 'true' ] && stopMinimartApp
}

minimartDatabase() {
  [ "${OPEN_DB:-false}" == 'true' ] \
    && openDatabase './src/test/resources/minimart' \
    || usage "Missing a valid option for minimartDb command..."
}

startMinimartApp() {
  # Current dir should be the scripts location (./health-apis-data-query/data-query-tests)
  cd ../../health-apis-ids/ids

  # Find id services jar
  local app=ids
  local pid=$(pidOf $app)
  [ -n "$pid" ] && echo "$app appears to already be running ($pid)" && cd - && exit 1
  local jar=$(find target -maxdepth 1 -name "$app-*.jar" | grep -v 'tests')
  [ -z "$jar" ] && echo "Cannot find $app application jar" && exit 1
  local options="-Dapp.name=$app"

  # Startup a new or existing minimart database and load it with goodies
  [ "$(uname)" != "Darwin" ] && [ "$(uname)" != "Linux" ] && echo "Add support for your operating system" && exit 1
  echo "Starting ids for minimart..."
  options+=" -cp $(readlink -f $jar):$(readlink -f ~/.m2/repository/com/h2database/h2/1.4.197/h2-1.4.197.jar)"
  options+=" -Dspring.jpa.generate-ddl=true"
  options+=" -Dspring.jpa.hibernate.ddl-auto=$DDL_AUTO"
  options+=" -Dspring.jpa.hibernate.globally_quoted_identifiers=true"
  options+=" -Dspring.datasource.driver-class-name=org.h2.Driver"
  options+=" -Dspring.datasource.url=jdbc:h2:file:$(pwd)/minimartIds"
  options+=" -Dspring.datasource.username=sa"
  options+=" -Dspring.datasource.password=sa"
  java ${options} org.springframework.boot.loader.PropertiesLauncher &
}

stopMinimartApp() {
  ../src/scripts/dev-app.sh -i stop
}

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
  [ -z "$DIRECTORY" ] && usage "Directory is a Required Option." && exit 1
  [ -z "$RESOURCE_TYPE" ] && usage "Resource Type is a Required Option." && exit 1
  mvn -f ../data-query test-compile && \
    mvn -f ../data-query \
    -P'!standard' \
    -Pmitre-minimart-maker \
    generate-resources \
    -DresourceType="$RESOURCE_TYPE" \
    -DinputDirectory="$DIRECTORY"
}

openDatabase() {
  java -jar ~/.m2/repository/com/h2database/h2/1.4.197/h2-1.4.197.jar -url jdbc:h2:$1 -user sa -password sa
}

usage() {
cat <<EOF
Commands:
  minimartIds <create|start|stop>
    Creates, starts, or stops the local ids
  transformToDatamart <directory-to-read-files-from> <resource-name>
    Takes all files for the given resource in the directory and transforms them to datamart schema
  pushToMinimartDb <directory-to-read-files-from> <resource-name>
    Pushes all files for the given resource and directory to a local h2 repository
  openMinimartDb
    Open the database that was created by the pushToDb command

Options:
  -s|--start) Can be used with minimartIds command to start local minimartIds (db must first be created)
  -k|--stop) Can be used with minimartIds command to stop local minimartIds
  -c|--create) Can be used with minimartIds command to create local minimartIds
  -d|--directory) Use to specify the directory files are located in for a transform or dbPush
  -r|--resource) Use to specify the resource to transform or push to db
  -o|--open) Open the database from the given command
  -h|--help) I need an adult!!!

Examples:
  minimartIds --create|--start|--stop|--open
  transformToDatamart -d "$(pwd)/data-query-tests/target" -r AllergyIntolerance
  pushToMinimartDb -d "$(pwd)/data-query-tests/target/fhir-to-datamart" -r AllergyIntolerance
  minimartDb --create|--start|--stop|--open

$1
EOF
}

ARGS=$(getopt -n $(basename ${0}) \
    -l "help,start,stop,directory:,resource:,create:,open" \
    -o "hskd:r:c:o" -- "$@")
[ $? != 0 ] && usage
eval set -- "$ARGS"
while true
do
  case "$1" in
    -s|--start) START=true && DDL_AUTO="validate";;
    -k|--stop) STOP=true;;
    -c|--create) START=true && DDL_AUTO="create";;
    -d|--directory) DIRECTORY="$2";;
    -r|--resource) RESOURCE_TYPE="$2";;
    -o|--open) OPEN_DB=true;;
    -h|--help) usage;;
    --) shift;break;;
  esac
  shift;
done

case $COMMAND in
  minimartIds) minimartIds;;
  pushToMinimartDb) pushToDatabase;;
  transformToDatamart) transformFhirToDatamart;;
  minimartDb) minimartDatabase;;
  *) usage "Invalid Command: $COMMAND" && exit 1 ;;
esac
