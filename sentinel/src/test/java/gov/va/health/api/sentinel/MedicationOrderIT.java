package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.NotInLab;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MedicationOrderIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({NotInLab.class})
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            MedicationOrder.class,
            "/api/MedicationOrder/{id}",
            verifier.ids().medicationOrder()),
        test(404, OperationOutcome.class, "/api/MedicationOrder/{id}", verifier.ids().unknown()),
        test(
            200,
            MedicationOrder.Bundle.class,
            "/api/MedicationOrder?patient={patient}",
            verifier.ids().patient()));
  }

  @Test
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            MedicationOrder.Bundle.class,
            "/api/MedicationOrder?_id={id}",
            verifier.ids().medicationOrder()),
        test(
            404, OperationOutcome.class, "/api/MedicationOrder?_id={id}", verifier.ids().unknown()),
        test(
            200,
            MedicationOrder.Bundle.class,
            "/api/MedicationOrder?identifier={id}",
            verifier.ids().medicationOrder()));
  }
}
