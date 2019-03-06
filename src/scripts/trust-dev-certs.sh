#!/usr/bin/env bash

#
# Updates cacerts trust store in Java home to trust:
#   The nexus.freedomstream.io certificate.
#   The tools.health.dev-developer.va.gov certificate.
#
# You will probably need root access to modify the trust store.
#

remove_old_certificate () {
  set +e
  OLD=$(keytool \
    -list \
    -keypass "$HEALTH_API_CERTIFICATE_PASSWORD" \
    -keystore "$TRUST_STORE" \
    -storepass "$TRUST_STORE_PASSWORD" \
    | grep "$1")

  if [ -n "$OLD" ]
  then
    echo "Removing old $1 trusted certificate"
    keytool \
      -delete \
      -alias $1 \
      -keypass "$HEALTH_API_CERTIFICATE_PASSWORD" \
      -keystore "$TRUST_STORE" \
      -storepass "$TRUST_STORE_PASSWORD"
  fi
  set -e
}

import_certificate_from_url () {
  remove_old_certificate $1

  echo "Printing certificate for SSL server $1 to $1.pem"
  keytool \
    -printcert \
    -rfc \
    -sslserver $1 \
    > $1.pem

  echo "Importing $1.pem"
  keytool \
    -importcert \
    -file $1.pem \
    -alias $1 \
    -keystore "$TRUST_STORE" \
    -storepass "$TRUST_STORE_PASSWORD" \
    -noprompt
}

cd $(dirname $0)/../..

[ -z "$JAVA_HOME" ] && echo "JAVA_HOME not set" && exit 1
[ -z "$HEALTH_API_CERTIFICATE_PASSWORD" ] && echo "HEALTH_API_CERTIFICATE_PASSWORD not set" && exit 1
[ -z "$TRUST_STORE" ] && TRUST_STORE="$JAVA_HOME/jre/lib/security/cacerts"
[ ! -f "$TRUST_STORE" ] && echo "Trust store not found: $TRUST_STORE" && exit 1
[ -z "$TRUST_STORE_PASSWORD" ] && TRUST_STORE_PASSWORD=changeit

import_certificate_from_url nexus.freedomstream.io

import_certificate_from_url tools.health.dev-developer.va.gov
