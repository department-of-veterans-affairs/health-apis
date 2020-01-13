package gov.va.api.health.dataquery.tests;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.sentinel.BasicTestClient;
import gov.va.api.health.sentinel.FhirTestClient;
import gov.va.api.health.sentinel.TestClient;
import lombok.experimental.UtilityClass;

/**
 * Test clients for interacting with different services (ids, mr-anderson, data-query) in a {@link
 * SystemDefinition}.
 */
@UtilityClass
public final class TestClients {
  /** DSTU2 Data Query test client. */
  public static TestClient dstu2DataQuery() {
    return FhirTestClient.builder()
        .service(SystemDefinitions.systemDefinition().dstu2DataQuery())
        .mapper(JacksonConfig::createMapper)
        .errorResponseEqualityCheck(
            new gov.va.api.health.dataquery.tests.dstu2.OperationOutcomesAreFunctionallyEqual())
        .build();
  }

  /** ID service test client. */
  public static TestClient ids() {
    return BasicTestClient.builder()
        .service(SystemDefinitions.systemDefinition().ids())
        .contentType("application/json")
        .mapper(JacksonConfig::createMapper)
        .build();
  }

  /** Internal Data Query test client. */
  public static TestClient internalDataQuery() {
    return BasicTestClient.builder()
        .service(SystemDefinitions.systemDefinition().internalDataQuery())
        .contentType("application/json")
        .mapper(JacksonConfig::createMapper)
        .build();
  }

  /** STU3 Data Query test client. */
  public static TestClient stu3DataQuery() {
    return FhirTestClient.builder()
        .service(SystemDefinitions.systemDefinition().stu3DataQuery())
        .mapper(JacksonConfig::createMapper)
        .errorResponseEqualityCheck(
            new gov.va.api.health.dataquery.tests.stu3.OperationOutcomesAreFunctionallyEqual())
        .build();
  }
}
