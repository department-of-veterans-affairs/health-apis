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
        // search by identifier
        test(
            200,
            PractitionerRole.Bundle.class,
            "PractitionerRole?identifier={npi}",
            verifier.ids().practitioners().npi()),
        // search by practitioner.identifier
        test(
            200,
            PractitionerRole.Bundle.class,
            "PractitionerRole?practitioner.identifier={npi}",
            verifier.ids().practitioners().npi()),
        // search by practitioner.name Family name
        test(
            200,
            PractitionerRole.Bundle.class,
            "PractitionerRole?practitioner.name={name}",
            verifier.ids().practitioners().family()));
  }

  @Test
  public void malformed() {
    verifier.verifyAll(
        test(400, OperationOutcome.class, "PractitionerRole/"),
        test(400, OperationOutcome.class, "PractitionerRole?blah=123"),
        // parameters not specified together
        test(
            400,
            OperationOutcome.class,
            "PractitionerRole?practitioner.name=Somename&practitioner.identifier=123"));
  }

  @Test
  public void searchBySpecialty() {
    verifier.verifyAll(
        // Throws NotImplemented exception
        test(501, OperationOutcome.class, "PractitionerRole?specialty=system|code"));
  }
}
