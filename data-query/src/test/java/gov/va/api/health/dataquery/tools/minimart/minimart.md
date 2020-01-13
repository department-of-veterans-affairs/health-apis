# mitre-minimart

## Usage

Minimart Maker shell script is located in: `data-query-tests`

Prerequisites:
* `health-apis-data-query-synthetic-records` must be cloned in the same parent directory as
`health-apis-data-query`

### 1. Transform to Datamart
```
./mitre-minimart-maker.sh transformToDatamart -d <directory> -r <resource-name>
```
* directory: the directory that contains the fhir-formatted json files
(`health-apis-data-query-synthetic-records/fhir`)
* resource-name: name of the resource to transform (only one can be done at a time) (ex. AllergyIntolerance)

This process outputs recursively finds AllergyIntolerance json files in the directory and outputs
the transformed files to `data-query-tests/target/fhir-to-datamart-samples/`.

### 2. Push to a Datamart database

This process takes the transformed datamart files and inserts them into a database along with all
other columns in the table.

#### Local H2 Database:
```
./mitre-minimart-maker.sh pushToMinimartDb -d <directory> -r <resource-name>
```
* directory: the directory containing the transformed files (should be
    `data-query-tests/target/fhir-to-datamart-samples/`)
* resource-name: again, only one resource can be done at a time (ex. AllergyIntolerance)

The database created by the shell script here is located in `data-query-tests/target/minimart.mv.db`

#### External SQL Server Database:
```
./mitre-minimart-maker.sh pushToMinimartDb -d <directory> -r <resource-name> -f <config-file-location>
```
* directory: the directory containing the transformed files (should be
    `data-query-tests/target/fhir-to-datamart-samples/`)
* resource-name: again, only one resource can be done at a time (ex. AllergyIntolerance)
* config-file-location: location of the config file used to connect to sql server db

Example Config File:

`minimart.properties`
```
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.datasource.url=jdbc:sqlserver://mitre.minimart.maker:443;database=the-coolest-database;user=secret;password=secret
spring.datasource.username=secret
spring.datasource.password=secret
```

### 3. Fin
Now that everything is where it's meant to be, we can do a couple other things:
```
# NOTE: Commands below assume user is in the top level directory of health-apis-data-query

# Open a local h2 datamart database to view the data
./data-query-tests/mitre-minimart-maker.sh minimartDb --open

# Run the applications to test the fhir data is identical to the stored procedure version
# NOTE: The identity-service will also need to be running

# ===== LOCAL H2 =====
# To run the application using the local h2 database, use the minimart script to start up
# data-query and the dev-app.sh script to start up the ids
./data-query-tests/mitre-minimart-maker.sh minimartDb --start
./src/scripts/dev-app.sh -mi start

# ===== External SQL Server DB =====
# To run the application with the external sql server db, update dev-application.properties
# in data-query to point to the new Sql Server database and run the apps using dev-app.sh
./src/scripts/dev-app.sh -mid start

curl -HDatamart:true http://localhost:8090/AllergyIntolerance?patient=<some-registered-patient>
```
