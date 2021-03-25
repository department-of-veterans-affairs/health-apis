package gov.va.api.health.dataquery.tests.stu3;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class PractitionerRoleIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.stu3();

  @Test
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            PractitionerRole.Bundle.class,
            "PractitionerRole?_id={id}",
            DataQueryResourceVerifier.ids().practitioner()),
        test(
            404,
            OperationOutcome.class,
            "PractitionerRole?_id={id}",
            DataQueryResourceVerifier.ids().unknown()),
        test(
            200,
            PractitionerRole.Bundle.class,
            "PractitionerRole?identifier={id}",
            DataQueryResourceVerifier.ids().practitioner()),
        test(
            404,
            OperationOutcome.class,
            "PractitionerRole?identifier={id}",
            DataQueryResourceVerifier.ids().unknown()),
        test(
            200,
            PractitionerRole.Bundle.class,
            "PractitionerRole?practitioner.family={family}&given={given}",
            DataQueryResourceVerifier.ids().practitioners().family(),
            DataQueryResourceVerifier.ids().practitioners().given()),
        test(
            200,
            PractitionerRole.Bundle.class,
            "PractitionerRole?practitioner.identifier={npi}",
            DataQueryResourceVerifier.ids().practitioners().npi()),
        test(
            501,
            OperationOutcome.class,
            "PractitionerRole?specialty={specialty}",
            DataQueryResourceVerifier.ids().practitioners().specialty()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            PractitionerRole.class,
            "PractitionerRole/{id}",
            DataQueryResourceVerifier.ids().practitioner()),
        test(
            404,
            OperationOutcome.class,
            "PractitionerRole/{id}",
            DataQueryResourceVerifier.ids().unknown()));
  }
}
