package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.PractitionerRole;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class PractitionerRoleIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, PractitionerRole.class, "PractitionerRole/{id}", verifier.ids().practitioner()),
        test(404, OperationOutcome.class, "PractitionerRole/{id}", verifier.ids().unknown()),
        // search by _id
        test(
            200,
            PractitionerRole.Bundle.class,
            "PractitionerRole?_id={id}",
            verifier.ids().practitioner()));
  }

  @Test
  public void malformed() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(400, OperationOutcome.class, "PractitionerRole/"),
        test(400, OperationOutcome.class, "PractitionerRole?blah=123"));
  }
}