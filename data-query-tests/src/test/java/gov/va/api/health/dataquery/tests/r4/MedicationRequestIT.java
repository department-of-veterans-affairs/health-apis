package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.uscorer4.api.resources.MedicationRequest;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class MedicationRequestIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  public void advanced() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(
            200,
            MedicationRequest.Bundle.class,
            "MedicationRequest?_id={id}",
            verifier.ids().medicationOrder()),
        test(
            200,
            MedicationRequest.Bundle.class,
            "MedicationRequest?_id={id}",
            verifier.ids().medicationStatement()),
        test(404, OperationOutcome.class, "MedicationRequest?_id={id}", verifier.ids().unknown()),
        test(
            200,
            MedicationRequest.Bundle.class,
            "MedicationRequest?identifier={id}",
            verifier.ids().medicationOrder()),
        test(
            200,
            MedicationRequest.Bundle.class,
            "MedicationRequest?identifier={id}",
            verifier.ids().medicationStatement()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        // Patient And Intent
        test(
            200,
            MedicationRequest.Bundle.class,
            "MedicationRequest?patient={patient}&intent=order",
            verifier.ids().patient()),
        test(
            200,
            MedicationRequest.Bundle.class,
            "MedicationRequest?patient={patient}&intent=plan",
            verifier.ids().patient()),
        // MedicationRequest Public Id
        test(
            200,
            MedicationRequest.class,
            "MedicationRequest/{id}",
            verifier.ids().medicationOrder()),
        test(
            200,
            MedicationRequest.class,
            "MedicationRequest/{id}",
            verifier.ids().medicationStatement()),
        test(404, OperationOutcome.class, "MedicationRequest/{id}", verifier.ids().unknown()),
        // Patient Icn
        test(
            200,
            MedicationRequest.Bundle.class,
            "MedicationRequest?patient={patient}",
            verifier.ids().patient()));
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(
            403,
            OperationOutcome.class,
            "MedicationRequest?patient={patient}",
            verifier.ids().unknown()));
  }
}
