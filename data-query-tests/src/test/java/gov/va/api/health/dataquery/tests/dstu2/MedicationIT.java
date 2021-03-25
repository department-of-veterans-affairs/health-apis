package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.dstu2.api.resources.Medication;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class MedicationIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.dstu2();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void advanced() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(200, Medication.Bundle.class, "Medication?_id={id}", testIds.medication()),
        test(404, OperationOutcome.class, "Medication?_id={id}", testIds.unknown()),
        test(200, Medication.Bundle.class, "Medication?identifier={id}", testIds.medication()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Medication.class, "Medication/{id}", testIds.medication()),
        test(404, OperationOutcome.class, "Medication/{id}", testIds.unknown()));
  }
}
