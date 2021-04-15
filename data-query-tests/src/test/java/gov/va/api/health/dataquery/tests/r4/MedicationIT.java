package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.Medication;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class MedicationIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void read() {
    verifyAll(
        test(200, Medication.class, "Medication/{id}", testIds.medication()),
        test(404, OperationOutcome.class, "Medication/{id}", testIds.unknown()));
  }

  @Test
  public void search() {
    verifyAll(
        test(200, Medication.Bundle.class, "Medication?_id={id}", testIds.medication()),
        test(
            200,
            Medication.Bundle.class,
            b -> b.entry().isEmpty(),
            "Medication?_id={id}",
            testIds.unknown()),
        test(200, Medication.Bundle.class, "Medication?identifier={id}", testIds.medication()));
  }
}
