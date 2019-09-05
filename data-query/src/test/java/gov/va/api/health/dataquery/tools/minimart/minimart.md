# mitre-minimart

## Usage

Minimart Maker shell script is located in: `data-query-tests`

### 1. Generate and Populate the Identity Service
```
# Creates and starts a local H2 ids database 
# NOTE: This will delete any previous data if run more than once.
./mitre-minimart-maker.sh minimartIds --create

# This will register the value, but also create the 
# table so we can register identities by hand with ease.
curl -k -X POST \
    http://localhost:8089/api/v1/ids \
    -H 'Content-Type: application/json' \
    -d '[
          {
                  "identifier": "43000037",
                  "system": "CDW",
                  "resource": "ALLERGY_INTOLERANCE"
          }
  ]'

# Database can not be opened while it is already running.
./mitre-minimart-maker.sh minimartIds --stop

# Open the database to make modifications
# (Add any of the patientIds that don't match using SQL commands)
./mitre-minimart-maker.sh minimartIds --open

# Before continuing, make sure the database has been closed.
```

### 2. Crawl to your heart's content
Use `./mitre-minimart-maker.sh minimartIds --start` to start up a local version of 
the ids that points to the H2 database that was created in the previous step.

Start `mr-anderson` and `data-query` using `health-apis-data-query/src/scripts/dev-app.sh` 
and crawl any number of patients you wish to crawl (with a LOCAL configuration). This will
register the ids for each reference which will be needed in the steps. The crawler also 
writes json files for each response to disk which will also be used.

`mr-anderson` and `data-query` can be stopped once crawling is completed. (Or not, it's really
up to you.)

### 3. Transform to Datamart
```
./mitre-minimart-maker.sh transformToDatamart <directory> <resource-name>
```
- directory: the directory that contains the fhir-formatted json files that were output
    by the crawler (probably `data-query-tests/target`)
- resource-name: name of the resource to transform (only one can be done at a time) (ex. AllergyIntolerance)

This process outputs recursively finds AllergyIntolerance json files in the directory and outputs 
the transformed files to `data-query-tests/target/fhir-to-datamart-samples/`.

### 4. Push to a local Datamart database
```
./mitre-minimart-maker.sh pushToMinimartDb <directory> <resource-name>
```
- directory: the directory containing the transformed files (should be 
    `data-query-tests/target/fhir-to-datamart-samples/`)
- resource-name: again, only one resource can be done at a time (ex. AllergyIntolerance)

This process takes the transformed datamart files and inserts them into the database along with all 
other colums in the table.

### 5. Fin
Now that everything is where it's meant to be, we can do a couple other things:
```
# Open the local datamart database to view the data
./mitre-minimart-maker.sh minimartDb --open

# Run the applications to tests the fhir data is identical to the stored procedure version
# With -HDatamart:true, mr-anderson does not need to be running
# NOTE: To run mr-anderson, use dev-app.sh
./mitre-minimart-maker.sh minimartDb --start && ./mitre-minimart-maker.sh minimartIds --start

curl -HDatamart:true http://localhost:8090/AllergyIntolerance?patient=<some-registered-patient>
```
