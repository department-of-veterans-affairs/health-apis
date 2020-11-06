package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.PractitionerRole;
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
    verifier.verifyAll(
        test(400, OperationOutcome.class, "PractitionerRole/"),
        test(400, OperationOutcome.class, "PractitionerRole?blah=123"));
  }

  @Test
  public void notImplementedParameters() {
    verifier.verifyAll(
        // Throws NotImplemented exception
        test(501, OperationOutcome.class, "PractitionerRole?identifier=123"),
        test(501, OperationOutcome.class, "PractitionerRole?specialty=system|code"),
        test(501, OperationOutcome.class, "PractitionerRole?practitioner.identifier=system|code"),
        test(501, OperationOutcome.class, "PractitionerRole?practitioner.name=Doe"));
  }
}
