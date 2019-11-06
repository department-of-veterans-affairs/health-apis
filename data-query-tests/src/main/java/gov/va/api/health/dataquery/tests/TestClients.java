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

  static TestClient dataQuery() {
    return FhirTestClient.builder()
        .service(SystemDefinitions.systemDefinition().dataQuery())
        .mapper(JacksonConfig::createMapper)
        .errorResponseEqualityCheck(new OperationOutcomesAreFunctionallyEqual())
        .build();
  }

  static TestClient ids() {
    return BasicTestClient.builder()
        .service(SystemDefinitions.systemDefinition().ids())
        .contentType("application/json")
        .mapper(JacksonConfig::createMapper)
        .build();
  }

  static TestClient internalDataQuery() {
    return BasicTestClient.builder()
        .service(SystemDefinitions.systemDefinition().internalDataQuery())
        .contentType("application/json")
        .mapper(JacksonConfig::createMapper)
        .build();
  }

  static TestClient mrAnderson() {
    return BasicTestClient.builder()
        .service(SystemDefinitions.systemDefinition().mrAnderson())
        .contentType("application/xml")
        .mapper(JacksonConfig::createMapper)
        .build();
  }
}
