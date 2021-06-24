package gov.va.api.health.dataquery.tests.stu3;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class PractitionerRoleIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.stu3();

  TestIds ids = DataQueryResourceVerifier.ids();

  @Test
  void advanced() {
    verifier.verifyAll(
        test(
            200,
            PractitionerRole.Bundle.class,
            "PractitionerRole?_id={id}",
            ids.practitionerRole()),
        test(404, OperationOutcome.class, "PractitionerRole?_id={id}", ids.unknown()),
        test(
            200,
            PractitionerRole.Bundle.class,
            "PractitionerRole?identifier={id}",
            ids.practitionerRole()),
        test(404, OperationOutcome.class, "PractitionerRole?identifier={id}", ids.unknown()),
        test(
            200,
            PractitionerRole.Bundle.class,
            "PractitionerRole?practitioner.family={family}&given={given}",
            ids.practitioners().family(),
            ids.practitioners().given()),
        test(
            200,
            PractitionerRole.Bundle.class,
            "PractitionerRole?practitioner.identifier=http://hl7.org/fhir/sid/us-npi|{npi}",
            ids.practitioners().npi()),
        test(
            501,
            OperationOutcome.class,
            "PractitionerRole?specialty={specialty}",
            ids.practitioners().specialty()));
  }

  @Test
  void basic() {
    verifier.verifyAll(
        test(200, PractitionerRole.class, "PractitionerRole/{id}", ids.practitionerRole()),
        test(404, OperationOutcome.class, "PractitionerRole/{id}", ids.unknown()));
  }
}
