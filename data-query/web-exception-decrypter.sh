#! /usr/bin/env bash
# Wrapper script for WebExceptionDecrypter.java
SCRIPT_DIR=$(dirname $0)
java ${SCRIPT_DIR}/src/test/java/gov/va/api/health/dataquery/service/controller/WebExceptionDecrypter.java ${@}
