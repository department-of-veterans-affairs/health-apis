package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Encounter;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.NotInLab;
import gov.va.health.api.sentinel.categories.NotInProd;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(NotInLab.class)
public class EncounterIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category(NotInProd.class)
  public void advanced() {
    verifier.verifyAll(
        test(200, Encounter.Bundle.class, "/api/Encounter?_id={id}", verifier.ids().encounter()),
        test(404, OperationOutcome.class, "/api/Encounter?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Encounter.Bundle.class,
            "/api/Encounter?identifier={id}",
            verifier.ids().encounter()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Encounter.class, "/api/Encounter/{id}", verifier.ids().encounter()),
        test(404, OperationOutcome.class, "/api/Encounter/{id}", verifier.ids().unknown()));
  }
}
