package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.MedicationDispense;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.NotInLab;
import gov.va.health.api.sentinel.categories.NotInProd;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MedicationDispenseIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Category({NotInLab.class, NotInProd.class})
  @Test
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            MedicationDispense.Bundle.class,
            "MedicationDispense?_id={id}",
            verifier.ids().medicationDispense()),
        test(
            200,
            MedicationDispense.Bundle.class,
            "MedicationDispense?identifier={id}",
            verifier.ids().medicationDispense()),
        test(404, OperationOutcome.class, "MedicationDispense?_id={id}", verifier.ids().unknown()));
  }

  @Category(NotInLab.class)
  @Test
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            MedicationDispense.Bundle.class,
            "MedicationDispense?patient={patient}&status=stopped,completed",
            verifier.ids().patient()),
        test(
            200,
            MedicationDispense.Bundle.class,
            "MedicationDispense?patient={patient}&type=FF,UD",
            verifier.ids().patient()),
        test(
            200,
            MedicationDispense.class,
            "MedicationDispense/{id}",
            verifier.ids().medicationDispense()),
        test(404, OperationOutcome.class, "MedicationDispense/{id}", verifier.ids().unknown()),
        test(
            200,
            MedicationDispense.Bundle.class,
            "MedicationDispense?patient={patient}",
            verifier.ids().patient()));
  }
}
