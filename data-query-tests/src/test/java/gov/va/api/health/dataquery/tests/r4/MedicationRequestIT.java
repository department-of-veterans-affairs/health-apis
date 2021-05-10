package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.MedicationRequest;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import java.util.function.Predicate;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class MedicationRequestIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  private Predicate<MedicationRequest.Bundle> bundleHasResults() {
    return bundle -> !bundle.entry().isEmpty();
  }

  @Test
  public void read() {
    verifyAll(
        // MedicationRequest Public Id
        test(200, MedicationRequest.class, "MedicationRequest/{id}", testIds.medicationOrder()),
        test(200, MedicationRequest.class, "MedicationRequest/{id}", testIds.medicationStatement()),
        test(404, OperationOutcome.class, "MedicationRequest/{id}", testIds.unknown()));
  }

  @Test
  public void search() {
    verifyAll(
        test(
            200,
            MedicationRequest.Bundle.class,
            bundleHasResults(),
            "MedicationRequest?_id={id}",
            testIds.medicationOrder()),
        test(
            200,
            MedicationRequest.Bundle.class,
            bundleHasResults(),
            "MedicationRequest?_id={id}",
            testIds.medicationStatement()),
        test(
            200,
            MedicationRequest.Bundle.class,
            bundleHasResults().negate(),
            "MedicationRequest?_id={id}",
            testIds.unknown()),
        test(
            200,
            MedicationRequest.Bundle.class,
            bundleHasResults(),
            "MedicationRequest?identifier={id}",
            testIds.medicationOrder()),
        test(
            200,
            MedicationRequest.Bundle.class,
            bundleHasResults(),
            "MedicationRequest?identifier={id}",
            testIds.medicationStatement()),
        // Patient And Intent
        test(
            200,
            MedicationRequest.Bundle.class,
            bundleHasResults(),
            "MedicationRequest?patient={patient}&intent=order",
            testIds.patient()),
        test(
            200,
            MedicationRequest.Bundle.class,
            bundleHasResults(),
            "MedicationRequest?patient={patient}&intent=plan",
            testIds.patient()),
        // Patient Icn
        test(
            200,
            MedicationRequest.Bundle.class,
            bundleHasResults(),
            "MedicationRequest?patient={patient}",
            testIds.patient()));
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(
            403, OperationOutcome.class, "MedicationRequest?patient={patient}", testIds.unknown()));
  }
}
