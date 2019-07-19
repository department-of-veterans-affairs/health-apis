#!/usr/bin/env bash

#
# This builds a copy of the datamart index replacing IDs that are available in the local datamary h2 db
#
SOURCE=target/cdw/samples/index.yaml
DESTINATION=src/test/resources/cdw/datamart-index.yaml
sed \
  -e 's|file: |file: ../../../../target/cdw/samples/|' \
  -e 's/\(query:.*\)185601V825290/\1111222333V000999/' \
  -e 's/family=VETERAN/family=Olson653/' \
  $SOURCE > $DESTINATION


cat<<EOF
Rebuilt $DESTINATION
From $SOURCE

diff $SOURCE $DESTINATION
EOF





