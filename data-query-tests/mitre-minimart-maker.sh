#!/usr/bin/env bash

COMMAND=$1
DIRECTORY=$2
RESOURCE_TYPE=$3

pushToDatabase() {

}

transformFhirToDatamart() {

}

case $COMMAND in
  pushToDb) pushToDatabase ;;
  transformToDatamart) transformFhirToDatamart ;;
  mitreMiniMart) transformFhirToDatamart && pushToDatabase ;;
  *) echo "Invalid Command: $COMMAND" && exit 1;;
esac