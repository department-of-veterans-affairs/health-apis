package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Location;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.Local;
import gov.va.health.api.sentinel.categories.ProdArgo;
import gov.va.health.api.sentinel.categories.ProdCargo;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class LocationIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Category({Local.class, ProdCargo.class})
  @Test
  public void advanced() {
    verifier.verifyAll(
        test(200, Location.Bundle.class, "Location?_id={id}", verifier.ids().location()),
        test(404, OperationOutcome.class, "Location?_id={id}", verifier.ids().unknown()),
        test(200, Location.Bundle.class, "Location?identifier={id}", verifier.ids().location()));
  }

  @Category({Local.class, ProdArgo.class, ProdCargo.class})
  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Location.class, "Location/{id}", verifier.ids().location()),
        test(404, OperationOutcome.class, "Location/{id}", verifier.ids().unknown()));
  }
}
