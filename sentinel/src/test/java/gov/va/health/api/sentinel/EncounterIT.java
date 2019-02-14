package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Encounter;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.Local;
import gov.va.health.api.sentinel.categories.ProdArgo;
import gov.va.health.api.sentinel.categories.ProdCargo;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class EncounterIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Category({Local.class, ProdCargo.class})
  @Test
  public void advanced() {
    verifier.verifyAll(
        test(200, Encounter.Bundle.class, "Encounter?_id={id}", verifier.ids().encounter()),
        test(404, OperationOutcome.class, "Encounter?_id={id}", verifier.ids().unknown()),
        test(200, Encounter.Bundle.class, "Encounter?identifier={id}", verifier.ids().encounter()));
  }

  @Category({Local.class, ProdArgo.class, ProdCargo.class})
  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Encounter.class, "Encounter/{id}", verifier.ids().encounter()),
        test(404, OperationOutcome.class, "Encounter/{id}", verifier.ids().unknown()));
  }
}
