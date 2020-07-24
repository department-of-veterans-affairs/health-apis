package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class MedicationOrderIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.dstu2();

  @Test
  public void advanced() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(
            200,
            MedicationOrder.Bundle.class,
            "MedicationOrder?_id={id}",
            verifier.ids().medicationOrder()),
        test(404, OperationOutcome.class, "MedicationOrder?_id={id}", verifier.ids().unknown()),
        test(
            200,
            MedicationOrder.Bundle.class,
            "MedicationOrder?identifier={id}",
            verifier.ids().medicationOrder()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, MedicationOrder.class, "MedicationOrder/{id}", verifier.ids().medicationOrder()),
        test(404, OperationOutcome.class, "MedicationOrder/{id}", verifier.ids().unknown()),
        test(
            200,
            MedicationOrder.Bundle.class,
            "MedicationOrder?patient={patient}",
            verifier.ids().patient()));
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(
            403,
            OperationOutcome.class,
            "MedicationOrder?patient={patient}",
            verifier.ids().unknown()));
  }
}
