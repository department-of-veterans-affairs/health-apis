# Mr. Anderson Mock Database

Data-query integration tests use a Type 4 JDBC driver implementation
that mocks the CDW `prc_Entity_Return` stored procedure.

* Driver class: `gov.va.api.health.sentinel.tests.categories.mockcdw.MockEntityReturnDriver`
* JDBC Url: `jdbc:mockcdw://path/to/index.yaml,path/to/another/index.yaml`

> ⚠ This driver supports just enough functionality for Mr. Anderson to function. It is not a
> general purpose JDBC driver and is not suitable for any other purpose. For Spring Boot
> applications, the health check must be disabled: `-Dmanagement.health.db.enabled=false`

#### Index file

The index is YAML file provides a mapping between stored procedure parameters and XML responses
in the following format:

```
entries:
- file: AllInt103i1000001782544.xml
  query: /AllergyIntolerance:1.03?identifier=1000001782544
  page: 1
  count: 15
- file: DiaRep102p185601V825290.xml
  query: /DiagnosticReport:1.02?patient=185601V825290
  page: 1
  count: 15
  ...
```

 Files are specified relative to the index.yaml file.

 #### Data
 The build will download `cdw-schemas` _sample_ archive and expand it in
 `target/cdw/samples`. This allows the same queries used to test the CDW database for XSD
 compliance to be used for local testing.
