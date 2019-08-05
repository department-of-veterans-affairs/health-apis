#!/usr/bin/env bash

#
# This builds a copy of the datamart index replacing IDs that are available in the local datamary h2 db
#
SOURCES="target/cdw/samples/index.yaml src/test/resources/cdw/index.yaml"
DESTINATION=src/test/resources/cdw/datamart-index.yaml
cat <<EOF > $DESTINATION
# GENERATED USING rebuild-datamart-index.sh
entries:
EOF

for SOURCE in $SOURCES
do
  sed \
    -e 's/^    /  /' \
    -e 's/^  - /- /' \
    -e '/entries:/d' \
    -e 's|file: \([A-Za-z0-9]\+.xml\)|file: ../../../../target/cdw/samples/\1|' \
    -e 's/\(query:.*\)185601V825290/\1111222333V000999/' \
    -e 's/family=VETERAN/family=Olson653/' \
    -e 's/given=JOHN Q/given=Conrad619/' \
    -e 's/name=VETERAN,JOHN/name=Olson653, Conrad619/' \
    -e 's/birthdate=1970-01-01/birthdate=1948-06-28/' \
    -e 's/1000000031384:L/1400096309217:L/' \
    -e 's/\(DiagnosticReport.*\)eq1970-01-01$/\1eq2013-03-21/' \
    -e 's/1400007575530:P/1234567:D/' \
    -e 's/1000001782544/10000020559/' \
    $SOURCE >> $DESTINATION
done

#git diff -- $DESTINATION

cat<<EOF
Rebuilt $DESTINATION
From $SOURCES
EOF





