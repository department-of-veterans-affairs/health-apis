package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Location;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.NotInLab;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class LocationIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({NotInLab.class})
  public void basic() {
    verifier.verifyAll(
        test(200, Location.class, "/api/Location/{id}", verifier.ids().location()),
        test(404, OperationOutcome.class, "/api/Location/{id}", verifier.ids().unknown()));
  }

  @Test
  public void advanced() {
    verifier.verifyAll(
        test(200, Location.Bundle.class, "/api/Location?_id={id}", verifier.ids().location()),
        test(404, OperationOutcome.class, "/api/Location?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Location.Bundle.class,
            "/api/Location?identifier={id}",
            verifier.ids().location()));
  }
}
