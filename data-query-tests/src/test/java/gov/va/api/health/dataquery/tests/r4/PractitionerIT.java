package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Practitioner;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class PractitionerIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  public void basic() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(200, Practitioner.class, "Practitioner/{id}", verifier.ids().practitioner()),
        test(404, OperationOutcome.class, "Practitioner/{id}", verifier.ids().unknown()),
        test(
            200, Practitioner.Bundle.class, "Practitioner?_id={id}", verifier.ids().practitioner()),
        test(
            200,
            Practitioner.Bundle.class,
            p -> p.entry().isEmpty(),
            "Practitioner?_id={id}",
            verifier.ids().unknown()));
  }

  @Test
  public void malformed() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(400, OperationOutcome.class, "Practitioner/"),
        test(400, OperationOutcome.class, "Practitioner?nonsense=abc"));
  }
}
