package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.dataquery.tests.TestAssumptionUtility.assumeAllButLocal;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.tests.TestClients;
import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.FhirTestClient;
import gov.va.api.health.sentinel.ServiceDefinition;
import gov.va.api.health.sentinel.TestClient;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class AuthorizationIT {
  private final String apiPath() {
    return TestClients.dstu2DataQuery().service().apiPath();
  }

  @Test
  public void invalidTokenIsUnauthorized() {
    assumeAllButLocal();
    TestClient unauthorizedDqClient =
        FhirTestClient.builder()
            .service(unauthorizedServiceDefinition(TestClients.dstu2DataQuery().service()))
            .mapper(JacksonConfig::createMapper)
            .errorResponseEqualityCheck(new OperationOutcomesAreFunctionallyEqual())
            .build();
    ExpectedResponse response = unauthorizedDqClient.get(apiPath() + "Patient/1011537977V693883");
    response.expect(401);
  }

  private ServiceDefinition unauthorizedServiceDefinition(ServiceDefinition serviceDefinition) {
    return ServiceDefinition.builder()
        .url(serviceDefinition.url())
        .port(serviceDefinition.port())
        .accessToken(() -> Optional.of("123TheWizardOfOz4567"))
        .apiPath(serviceDefinition.apiPath())
        .build();
  }
}
