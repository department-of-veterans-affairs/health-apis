package gov.va.api.health.dataquery.tests;

import gov.va.api.health.dataquery.api.resources.MedicationDispense;
import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.categories.Local;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MedicationDispenseIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Category({Local.class, ProdDataQueryClinician.class})
  @Test
  public void advanced() {
    verifier.verifyAll(
        ResourceVerifier.test(
            200,
            MedicationDispense.Bundle.class,
            "MedicationDispense?_id={id}",
            verifier.ids().medicationDispense()),
        ResourceVerifier.test(
            200,
            MedicationDispense.Bundle.class,
            "MedicationDispense?identifier={id}",
            verifier.ids().medicationDispense()),
        ResourceVerifier.test(
            404, OperationOutcome.class, "MedicationDispense?_id={id}", verifier.ids().unknown()),
        ResourceVerifier.test(
            200,
            MedicationDispense.Bundle.class,
            "MedicationDispense?patient={patient}&status=stopped,completed",
            verifier.ids().patient()),
        ResourceVerifier.test(
            200,
            MedicationDispense.Bundle.class,
            "MedicationDispense?patient={patient}&type=FF,UD",
            verifier.ids().patient()),
        ResourceVerifier.test(
            200,
            MedicationDispense.Bundle.class,
            "MedicationDispense?patient={patient}",
            verifier.ids().patient()));
  }

  @Category({Local.class, ProdDataQueryPatient.class, ProdDataQueryClinician.class})
  @Test
  public void basic() {
    verifier.verifyAll(
        ResourceVerifier.test(
            200,
            MedicationDispense.class,
            "MedicationDispense/{id}",
            verifier.ids().medicationDispense()),
        ResourceVerifier.test(
            404, OperationOutcome.class, "MedicationDispense/{id}", verifier.ids().unknown()));
  }
}
