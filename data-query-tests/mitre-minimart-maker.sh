#!/usr/bin/env bash



REPO="$(cd $(dirname $0)/../../ && pwd)"
IDS_H2="$REPO/health-apis-ids/ids/target/minimartIds"
DQ_H2="$REPO/health-apis-data-query/data-query-tests/target/minimart"

minimartIds() {
  [ "${OPEN_DB:-false}" == 'true' ] && openDatabase "$IDS_H2" && exit 0
  [ -z "$START$STOP" ] && usage "Missing options for minimartIds" && exit 1
  [ "${START:-false}" == 'true' ] \
    && startMinimartApp ids "$REPO/health-apis-ids" "$IDS_H2" && exit 0
  [ "${STOP:-false}" == 'true' ] && stopMinimartApp ids && exit 0
  echo "Couldn't determine action to take..."
}

minimartDatabase() {
  [ "${OPEN_DB:-false}" == 'true' ] && openDatabase "$DQ_H2" && exit 0
  [ -z "$START$STOP" ] && usage "Missing options for minimartDb" && exit 1
  [ "${START:-false}" == 'true' ] && [ "$DDL_AUTO" != 'create' ] \
    && startMinimartApp data-query "$REPO/health-apis-data-query" "$DQ_H2" \
    && exit 0
  [ "${STOP:-false}" == 'true' ] && stopMinimartApp dq && exit 0
  echo "Couldn't determine action to take..."
}

pidOf() {
  local app=$1
  jps -v | grep -F -- "-Dapp.name=$app" | cut -d ' ' -f 1
}

startMinimartApp() {
  local app=$1
  local where=$2
  local pid=$(pidOf $app)
  [ -n "$pid" ] && echo "$app appears to already be running ($pid)" && return
  echo "Starting $app"
  [ ! -d "$where" ] && echo "$where does not exist" && exit 1
  cd $where/$app
  local jar=$(find target -maxdepth 1 -name "$app-*.jar" | grep -v 'tests')
  [ -z "$jar" ] && echo "Cannot find $app application jar" && exit 1
  local options="-Dapp.name=$app"

  local pathSeparator=':'
  [ "$(uname)" != "Darwin" ] && [ "$(uname)" != "Linux" ] && echo "Add support for your operating system" && exit 1
  echo "Using local H2 database for $app..."
  options+=" -cp $(readlink -f $jar)${pathSeparator}$(readlink -f ~/.m2/repository/com/h2database/h2/1.4.197/h2-1.4.197.jar)"
  if [ "$app" == 'data-query' ]
  then
    # application-dev.properties
    options+=" -Didentityservice.url=http://localhost:8089"
    options+=" -Dmranderson.url=http://localhost:8088"
    options+=" -Dargonaut.url=http://localhost:8090"
    options+=" -Dwell-known.capabilities=context-standalone-patient,launch-ehr,permission-offline,permission-patient"
    options+=" -Dwell-known.response-type-supported=code,refresh_token"
    options+=" -Dwell-known.scopes-supported=patient/DiagnosticReport.read,patient/Patient.read,offline_access"
    options+=" -Dconformance.statement-type=patient"
    options+=" -Dconformance.contact.name=jhulbert"
    options+=" -Dconformance.contact.email=joshua.hulbert@libertyits.com"
    options+=" -Dconformance.security.token-endpoint=http://fake.com/token"
    options+=" -Dconformance.security.authorize-endpoint=http://fake.com/authorize"
    options+=" -Dhealth-check.medication-id=skip"
  fi
  options+=" -Dspring.jpa.generate-ddl=false"
  options+=" -Dspring.jpa.hibernate.ddl-auto=$DDL_AUTO"
  options+=" -Dspring.jpa.properties.hibernate.globally_quoted_identifiers=false"
  options+=" -Dspring.datasource.driver-class-name=org.h2.Driver"
  options+=" -Dspring.datasource.url=jdbc:h2:$3"
  options+=" -Dspring.datasource.username=sa"
  options+=" -Dspring.datasource.password=sa"
  java ${options} org.springframework.boot.loader.PropertiesLauncher &
}

stopMinimartApp() {
  [ "$1" == 'ids' ] && OPTION="-i"
  [ "$1" == 'dq' ] && OPTION='-d'
  $REPO/health-apis-data-query/src/scripts/dev-app.sh "$OPTION" stop
}

pushToDatabase() {
  COMPILE=${COMPILE:-true}
  local workingDir="$REPO/health-apis-data-query"
  [ -z "$DIRECTORY" ] && usage "Directory is a Required Param." && exit 1
  [ -z "$RESOURCE_TYPE" ] && usage "Resource Type is a Required Param." && exit 1
  [ "$COMPILE" == 'true' ] && mvn -f "$workingDir/data-query" test-compile
  mvn -f "$workingDir/data-query" \
    -P'!standard' \
    -Pmitre-minimart-maker \
    exec:java@pushToDatabase \
    generate-resources \
    -DresourceType="$RESOURCE_TYPE" \
    -DinputDirectory="$DIRECTORY" \
    -DconfigFile="$CONFIG_FILE" \
    -Dorg.jboss.logging.provider=jdk \
    -Djava.util.logging.config.file=nope

}

transformFhirToDatamart() {
  COMPILE=${COMPILE:-true}
  local workingDir="$REPO/health-apis-data-query"
  [ -z "$DIRECTORY" ] && usage "Directory is a Required Option." && exit 1
  [ -z "$RESOURCE_TYPE" ] && usage "Resource Type is a Required Option." && exit 1
  [ -z "$CONFIG_FILE" ] \
    && [ ! -d "$REPO/health-apis-data-query-synthetic-records" ] \
    && usage 'Either `health-apis-data-query-sythetic-records` needs to be cloned in parent directory or config file needs to be defined.' \
    && exit 1
  [ "$COMPILE" == 'true' ] && mvn -f "$workingDir/data-query" test-compile
  mvn -f "$workingDir/data-query" \
    -P'!standard' \
    -Pmitre-minimart-maker \
    generate-resources \
    -DresourceType="$RESOURCE_TYPE" \
    -DinputDirectory="$DIRECTORY" \
    -DidsConfig=${CONFIG_FILE:-"$REPO/health-apis-data-query-synthetic-records/identity-service.properties"}
}

openDatabase() {
  java -jar ~/.m2/repository/com/h2database/h2/1.4.197/h2-1.4.197.jar -url jdbc:h2:$1 -user sa -password sa
}

usage() {
cat <<EOF
---
Commands:
  minimartIds <create|start|stop|open>
    Creates, starts, or stops the local ids
  transformToDatamart <directory-to-read-files-from> <resource-name>
    Takes all files for the given resource in the directory and transforms them to datamart schema
  pushToMinimartDb <directory-to-read-files-from> <resource-name>
    Pushes all files for the given resource and directory to a local h2 repository
  minimartDb <start|stop|open>
    Creates, starts, or stops the local data-query minimart h2 database
---
Options:
  -s|--start) Can be used with minimartIds command to start local minimartIds (db must first be created)
  -k|--stop) Can be used with minimartIds command to stop local minimartIds
  -c|--create) Can be used with minimartIds command to create local minimartIds
  -d|--directory) Use to specify the directory files are located in for a transform or dbPush
  -r|--resource) Use to specify the resource to transform or push to db
  -f|--config) Config file used either specify location of properties for different actions
  -p|--compile) Boolean, Maven will run a test-compile before performing operation
  -o|--open) Open the database from the given command
  -h|--help) I need an adult!!!
---
Examples:
  minimartIds --create|--start|--stop|--open
  transformToDatamart -d "$(pwd)/data-query-tests/target" -r AllergyIntolerance
  sqlServer: pushToMinimartDb -d "$(pwd)/data-query-tests/target/fhir-to-datamart" -r AllergyIntolerance -f "$(pwd)/my-super-awesome-config.properties"
  h2: pushToMinimartDb -d "$(pwd)/data-query-tests/target/fhir-to-datamart" -r AllergyIntolerance
  minimartDb --start|--stop|--open
---
$1
EOF
}

ARGS=$(getopt -n $(basename ${0}) \
    -l "help,start,stop,directory:,resource:,create,open,config:,compile:" \
    -o "hskd:r:cof:p:" -- "$@")
[ $? != 0 ] && usage
eval set -- "$ARGS"
while true
do
  case "$1" in
    -s|--start) START=true && DDL_AUTO="none";;
    -k|--stop) STOP=true;;
    -c|--create) START=true && DDL_AUTO="create";;
    -d|--directory) DIRECTORY="$2";;
    -f|--config) CONFIG_FILE="$2";;
    -r|--resource) RESOURCE_TYPE="$2";;
    -o|--open) OPEN_DB=true;;
    -p|--compile) COMPILE="$2";;
    -h|--help) usage && exit 0;;
    --) shift;break;;
  esac
  shift;
done

COMMAND=$1

echo "Working from directory: $REPO"

case $COMMAND in
  minimartIds) minimartIds;;
  pushToMinimartDb) pushToDatabase;;
  transformToDatamart) transformFhirToDatamart;;
  minimartDb) minimartDatabase;;
  *) usage "Invalid Command: $COMMAND" && exit 1 ;;
esac
