package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.tests.TestClients;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import org.junit.jupiter.api.Test;

public class WellKnownIT {
  private final String apiPath() {
    return TestClients.internalDataQuery().service().apiPath();
  }

  /**
   * She's not the prettiest, but local tests dont remove the dstu2 from the apiPath (done by the
   * ingress otherwise).
   */
  @Test
  public void localWellKnownIsValid() {
    assumeEnvironmentIn(Environment.LOCAL);
    requestWellKnown("/");
  }

  public void requestWellKnown(String apiPath) {
    ExpectedResponse response =
        TestClients.internalDataQuery().get(apiPath + ".well-known/smart-configuration");
    response.expect(200);
    String rawJson = response.response().asString();
    assertThat(rawJson)
        .withFailMessage("Tabs and newlines break our customer.")
        .doesNotContain("\n", "\t");
  }

  @Test
  public void wellKnownIsValid() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    requestWellKnown(apiPath());
  }
}
