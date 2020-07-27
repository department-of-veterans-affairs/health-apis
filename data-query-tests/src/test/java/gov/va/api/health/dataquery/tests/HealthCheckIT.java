package gov.va.api.health.dataquery.tests;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.hamcrest.CoreMatchers.equalTo;

import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.Test;

public class HealthCheckIT {
  @Test
  public void dataQueryIsHealthy() {
    assumeEnvironmentIn(Environment.LOCAL);
    TestClients.dstu2DataQuery()
        .get("/actuator/health")
        .response()
        .then()
        .body("status", equalTo("UP"));
  }
}
