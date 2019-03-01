# data-query-tests

Data-query integration tests.

##### Usage

The tests requires a partial security settings file to be defined for `LOCAL` environment
The `config/secrets.properties` file must be created and define passwords used with the
_DVP-DVP-NONPROD_ keystore and truststores. 
See the template [secrets.properties](src/test/config/secrets.properties) for requried values.

###### Interactive local system
The applications can be started for ad-hoc, interactive testing.

```
mvn pre-integration-test -Dsentinel=LOCAL -Dexec.waitForInterrupt=true
```

###### Local databases

When ran in `LOCAL` mode, `ids` is started with an empty in-memory H2 database and `mr-anderson`
is started with a [mock database](mock-database.md).


###### Lab

To support testing the Lab environment, Sentinel includes a Selenium based robot for working with
OAuth and `id.me` authentication. 

1. The `id.me` robot requires the Selenium Chrome Driver to be installed locally. You'll need to 
   install it. See http://chromedriver.chromium.org/home

2. Lab configuration is needed to interact with the lab. Sentinel will expect either System 
   properties are specified or you've created a `sentinel/config/lab.properties` file.
   It will complain about missing values if you've omitted. Please see a team member 
   for sensitive information.
