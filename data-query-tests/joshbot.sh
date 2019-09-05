#!/usr/bin/env bash

CONF=$(readlink -f $(dirname $0))/config/josh-bot.properties
usage() {
  cat<<EOF
$0

Run a basic test against the production Data Query server using a real user.

The following actions are performed:
- Log a real user into the system using OAuth flow.
  Two factor authentication will require input from the
  user.
- Request a series of resources to verify the proper scopes
  were applied to the user.

CONFIGURATION
The following configuration values should be set in the configuration file:
$CONF

va-oauth-robot.user-id
va-oauth-robot.user-password
  The user ID and password for a real user ID, i.e. your ID.me or My HealtheVet ID.

va-oauth-robot.credentials-type
  A value of ID_ME will use ID.me during the log in process.
  A value of MY_HEALTHE_VET will use My HealtheVet during the log in process.

va-oauth-robot.skip-two-factor-authentication=false
  For real users this value must be false. Two factor authentication is required and you
  will be prompted for text or authenticator application code.
  Test users do not have two factor authentication. A value of true is required for test
  user.

va-oauth-robot.client-id
va-oauth-robot.client-secret
  The production OAuth assigned client ID and secret required to access the APIs.
  These value can be provided by a team member.

va-oauth-robot.base-url=https://api.va.gov/services/fhir/v0/dstu2
  The base URL for smart-on-fhir data query application. Metadata will be extracted
  from the endpoint to determine the OAuth endpoints.

va-oauth-robot.credentials-mode
  A value of HEADER will use an HTTP request header to pass credentials
  A value of REQUEST_BODY will use the request payload to pass credentials
  Either is acceptable.

va-oauth-robot.redirect-url=https://app/after-auth
va-oauth-robot.state=unused
va-oauth-robot.aud=josh
  OAuth token request values. The above values will work for 99% of scenarios.

webdriver.chrome.driver
  The file location of the chrome driver, e.g. /Users/bryanschofield/Downloads/chromedriver
webdriver.chrome.headless=true
  A value of true will hide the chrome web browser during login. A value of false will 
  allow you to see the browser interactions.

$1
EOF
  exit 1
}

#
# This tool runs the JoshBot test.
#
[ -n "$1" ] && usage
[ ! -f "$CONF" ] && usage "Missing: $CONF"

mvn -q test -Pjoshbot -P'!standard'
