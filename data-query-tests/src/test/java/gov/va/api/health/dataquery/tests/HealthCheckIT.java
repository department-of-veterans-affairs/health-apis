package gov.va.api.health.dataquery.tests;

import static gov.va.api.health.dataquery.tests.TestAssumptionUtility.assumeLocal;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.jupiter.api.Test;

public class HealthCheckIT {
  @Test
  public void dataQueryIsHealthy() {
    assumeLocal();
    TestClients.dstu2DataQuery()
        .get("/actuator/health")
        .response()
        .then()
        .body("status", equalTo("UP"));
  }
}
