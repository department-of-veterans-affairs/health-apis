package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import org.junit.Test;

public class MedicationStatementIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            MedicationStatement.class,
            "/api/MedicationStatement/{id}",
            verifier.ids().medicationStatement()),
        test(
            404, OperationOutcome.class, "/api/MedicationStatement/{id}", verifier.ids().unknown()),
        test(
            200,
            MedicationStatement.Bundle.class,
            "/api/MedicationStatement?_id={id}",
            verifier.ids().medicationStatement()),
        test(
            200,
            MedicationStatement.Bundle.class,
            "/api/MedicationStatement?identifier={id}",
            verifier.ids().medicationStatement()),
        test(
            404,
            OperationOutcome.class,
            "/api/MedicationStatement?_id={id}",
            verifier.ids().unknown()),
        test(
            200,
            MedicationStatement.Bundle.class,
            "/api/MedicationStatement?patient={patient}",
            verifier.ids().patient()));
  }
}
