package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.LabArgo;
import gov.va.health.api.sentinel.categories.Local;
import gov.va.health.api.sentinel.categories.ProdArgo;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MedicationIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category(Local.class)
  public void advanced() {
    verifier.verifyAll(
        test(200, Medication.Bundle.class, "Medication?_id={id}", verifier.ids().medication()),
        test(404, OperationOutcome.class, "Medication?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Medication.Bundle.class,
            "Medication?identifier={id}",
            verifier.ids().medication()));
  }

  @Test
  @Category({Local.class, LabArgo.class, ProdArgo.class})
  public void basic() {
    verifier.verifyAll(
        test(200, Medication.class, "Medication/{id}", verifier.ids().medication()),
        test(404, OperationOutcome.class, "Medication/{id}", verifier.ids().unknown()));
  }
}
