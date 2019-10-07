# Configuration Guide

Health APIs run in Docker containers in AWS and are configured via properties files in AWS S3 
buckets. Application properties, certificates, and Kerberos configuration files are copied from
the S3 bucket into containers during the start up process. 

Docker containers must be bootstrapped with environment variables that enable access
to the S3 buckets.

## S3
Buckets are organized as follows:

```
S3
 ├ ${app-name}/application.properties
 ├ ...
 ├ krb5/krb5.conf
 └ system_certs/
   ├ <any>.jks
   ├ <any>-truststore.jks
   └ ...
```

- Application names can be anything. This will be configured per container using the `AWS_APP_NAME`
  environment variable.
- Each application has has an `application.properties` which contain Spring Boot configuration.
- `system_certs` may contain any number or keystore and truststore files.

##### On container start
- Based on the configured application name, the `application.properties` is copied to `/opt/va/` 
  to be loaded by the Spring Boot application.
- `krb5/krb5.conf` is copied to `/etc` where it will be for any Kerberos enabled connections.
- Files and directories are recursively copied from `system_certs` to `/opt/va/certs`.
  When configuring application properties, note the path will be `/opt/va/certs/<any>.jks`


## Docker Configuration

The following environment variables need to be set:

```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_DEFAULT_REGION
AWS_BUCKET_NAME
AWS_APP_NAME
```

## Application Properties

Applications _must_ define the following properties. 

> Standard Spring Boot configuration properties as well as other application-specific advanced
> configuration properties are available.
> See `src/main/resources/application.properties` in each application. 

### Data Query
```
# HTTPS Server
server.ssl.key-store ...................... Path to keystore, e.g. /opt/va/certs/<any>.jks
server.ssl.key-store-password ............. Password for the keystore
server.ssl.key-alias ...................... Key alias in the cert within the keystore to use

# HTTPS Client
ssl.key-store ............................. Path to keystore to use as a client
ssl.key-store-password .................... Password for the keystore
ssl.client-key-password ................... Password for the client key
ssl.use-trust-store ....................... <true|false> If enabled, mutual TLS is configured 
ssl.trust-store ........................... Path to truststore
ssl.trust-store-password .................. Password for the truststore

# Services
mranderson.url ............................ URL of the Mr. Anderson Service
                                            E.g. https://mr-anderson.example.com:8088

# Public Information
argonaut.url .............................. The public URL for Argonaut Data Query
                                            E.g. https://api.va.gov/services/argonaut/v0
conformance.contact.name .................. The public liaison
conformance.contact.email ................. Email address of the public liaison
conformance.security.token-endpoint ....... Public URL of the OAuth 2.0 token endpoint
conformance.security.authorize-endpoint ... Public URL of the OAuth 2.0 authorization endpoint

# Options
included-references.appointment ........... <true|false> Enable references to Appointments
included-references.encounter ............. <true|false> Enable references to Encounters
included-references.location .............. <true|false> Enable references to Locations
included-references.organization .......... <true|false> Enable references to Organizations
included-references.practitioner .......... <true|false> Enable references to Practitioners


```
### Mr. Anderson
```
# HTTPS Server
server.ssl.key-store ...................... Path to keystore, e.g. /opt/va/certs/<any>.jks
server.ssl.key-store-password ............. Password for the keystore
server.ssl.key-alias ...................... Key alias in the cert within the keystore to use

# HTTPS Client
ssl.key-store ............................. Path to keystore to use as a client
ssl.key-store-password .................... Password for the keystore
ssl.client-key-password ................... Password for the client key
ssl.use-trust-store ....................... <true|false> If enabled, mutual TLS is configured 
ssl.trust-store ........................... Path to truststore
ssl.trust-store-password .................. Password for the truststore

# Services
identityservice.url ....................... URL of the Identity Service
                                            E.g. https://ids.awesome.com:8089

# Database
spring.datasource.url ..................... JDBC URL to CDW
spring.datasource.username ................ Database user name
spring.datasource.password ................ Database password
```
