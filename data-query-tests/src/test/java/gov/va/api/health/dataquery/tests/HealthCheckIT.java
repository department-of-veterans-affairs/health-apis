package gov.va.api.health.dataquery.tests;

import static org.hamcrest.CoreMatchers.equalTo;

import gov.va.api.health.sentinel.categories.Local;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class HealthCheckIT {
  @Category(Local.class)
  @Test
  public void dataQueryIsHealthy() {
    TestClients.dataQuery().get("/actuator/health").response().then().body("status", equalTo("UP"));
  }

  @Category(Local.class)
  @Test
  public void idsIsHealthy() {
    TestClients.ids().get("/actuator/health").response().then().body("status", equalTo("UP"));
  }

  @Category(Local.class)
  @Test
  public void mrAndersonIsHealthy() {
    TestClients.mrAnderson()
        .get("/actuator/health")
        .response()
        .then()
        .body("status", equalTo("UP"));
  }
}
