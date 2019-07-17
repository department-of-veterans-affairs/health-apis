#!/usr/bin/env bash

#
# This script will export data from Mitre and replace the contents of the local
# ./src/test/resources/mitre database. It expects config/lab.properties to contain
# standard spring database configuration the source Mitre database.
#

cd $(dirname $0)

LAB_PROPERTIES=config/lab.properties
[ ! -f "$LAB_PROPERTIES" ] && echo "Missing $LAB_PROPERTIES" && exit 1

for p in spring.datasource.username spring.datasource.password spring.datasource.url
do
  ! grep -q "^$p=" $LAB_PROPERTIES && echo "Missing $p in $LAB_PROPERTIES" && exit 1
done

mvn -f ../data-query \
  -P'!standard' \
  -Pmitre-export \
  generate-resources \
  -DconfigFile=config/lab.properties \
  -DoutputFile=./src/test/resources/mitre \
  -Dorg.jboss.logging.provider=jdk \
  -Djava.util.logging.config.file=nope
