package gov.va.api.health.dataquery.tests.dstu2;

import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

import static gov.va.api.health.dataquery.tests.TestAssumptionUtility.assumeAllButLocal;
import static gov.va.api.health.dataquery.tests.TestAssumptionUtility.assumeLocal;

public class MedicationStatementIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.dstu2();

  @Test
  public void advanced() {
    assumeLocal();
    verifier.verifyAll(
        test(
            200,
            MedicationStatement.Bundle.class,
            "MedicationStatement?_id={id}",
            verifier.ids().medicationStatement()),
        test(404, OperationOutcome.class, "MedicationStatement?_id={id}", verifier.ids().unknown()),
        test(
            200,
            MedicationStatement.Bundle.class,
            "MedicationStatement?identifier={id}",
            verifier.ids().medicationStatement()));
  }

  @Test
  public void basic() {
    assumeAllButLocal();
    verifier.verifyAll(
        test(
            200,
            MedicationStatement.class,
            "MedicationStatement/{id}",
            verifier.ids().medicationStatement()),
        test(404, OperationOutcome.class, "MedicationStatement/{id}", verifier.ids().unknown()),
        test(
            200,
            MedicationStatement.Bundle.class,
            "MedicationStatement?patient={patient}",
            verifier.ids().patient()));
  }

  @Test
  public void searchNotMe() {
    assumeAllButLocal();
    verifier.verifyAll(
        test(
            403,
            OperationOutcome.class,
            "MedicationStatement?patient={patient}",
            verifier.ids().unknown()));
  }
}
