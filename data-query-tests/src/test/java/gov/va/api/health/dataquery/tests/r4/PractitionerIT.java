package gov.va.api.health.dataquery.tests.r4;


import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Practitioner;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class PractitionerIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void read() {
    verifyAll(
        test(200, Practitioner.class, "Practitioner/{id}", testIds.practitioner()),
        test(200, Practitioner.class, "Practitioner/npi-{npi}", testIds.practitioners().npi()),
        test(404, OperationOutcome.class, "Practitioner/{id}", testIds.unknown()));
  }

  @Test
  public void search() {
    verifyAll(
        test(200, Practitioner.Bundle.class, "Practitioner?_id={id}", testIds.practitioner()),
        test(
            200,
            Practitioner.Bundle.class,
            p -> p.entry().isEmpty(),
            "Practitioner?_id={id}",
            testIds.unknown()));
  }
}
