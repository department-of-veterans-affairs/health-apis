package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.dataquery.tests.dstu2.ResourceVerifier.test;

import gov.va.api.health.dstu2.api.resources.MedicationDispense;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.categories.Local;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MedicationDispenseIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Category({Local.class
    // , ProdDataQueryClinician.class
  })
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
        test(404, OperationOutcome.class, "MedicationDispense?_id={id}", verifier.ids().unknown()),
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
            MedicationDispense.Bundle.class,
            "MedicationDispense?patient={patient}",
            verifier.ids().patient()));
  }

  @Category({Local.class
    // , ProdDataQueryPatient.class, ProdDataQueryClinician.class
  })
  @Test
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            MedicationDispense.class,
            "MedicationDispense/{id}",
            verifier.ids().medicationDispense()),
        test(404, OperationOutcome.class, "MedicationDispense/{id}", verifier.ids().unknown()));
  }
}
