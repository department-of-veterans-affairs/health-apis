package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.api.health.argonaut.api.resources.Practitioner;
import gov.va.health.api.sentinel.categories.NotInLab;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class PractitionerIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({NotInLab.class})
  public void basic() {
    verifier.verifyAll(
        test(200, Practitioner.class, "/api/Practitioner/{id}", verifier.ids().practitioner()),
        test(404, OperationOutcome.class, "/api/Practitioner/{id}", verifier.ids().unknown()));
  }

  @Test
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            Practitioner.Bundle.class,
            "/api/Practitioner?_id={id}",
            verifier.ids().practitioner()),
        test(404, OperationOutcome.class, "/api/Practitioner?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            "/api/Practitioner?identifier={id}",
            verifier.ids().practitioner()));
  }
}
