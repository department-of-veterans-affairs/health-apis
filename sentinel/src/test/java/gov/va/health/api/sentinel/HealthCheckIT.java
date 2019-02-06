package gov.va.health.api.sentinel;

import static org.hamcrest.CoreMatchers.equalTo;

import gov.va.health.api.sentinel.categories.NotInLab;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class HealthCheckIT {

  @Category(NotInLab.class)
  @Test
  public void argonautIsHealthy() {
    Sentinel.get()
        .clients()
        .argonaut()
        .get("/actuator/health")
        .response()
        .then()
        .body("status", equalTo("UP"));
  }

  @Category(NotInLab.class)
  @Test
  public void idsIsHealthy() {
    Sentinel.get()
        .clients()
        .ids()
        .get("/actuator/health")
        .response()
        .then()
        .body("status", equalTo("UP"));
  }

  @Category(NotInLab.class)
  @Test
  public void mrAndersonIsHealthy() {
    Sentinel.get()
        .clients()
        .mrAnderson()
        .get("/actuator/health")
        .response()
        .then()
        .body("status", equalTo("UP"));
  }
}
