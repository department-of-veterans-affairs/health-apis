package gov.va.api.health.sentinel;

import static gov.va.api.health.sentinel.ResourceVerifier.test;

import gov.va.api.health.dataquery.api.resources.MedicationOrder;
import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.categories.LabDataQueryClinician;
import gov.va.api.health.sentinel.categories.LabDataQueryPatient;
import gov.va.api.health.sentinel.categories.Local;
import gov.va.api.health.sentinel.categories.ProdDataQueryClinician;
import gov.va.api.health.sentinel.categories.ProdDataQueryPatient;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MedicationOrderIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({Local.class, LabDataQueryClinician.class, ProdDataQueryClinician.class})
  public void advanced() {
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
  @Category({
    Local.class,
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
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
  @Category({
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void searchNotMe() {
    verifier.verifyAll(
        test(
            403,
            OperationOutcome.class,
            "MedicationOrder?patient={patient}",
            verifier.ids().unknown()));
  }
}
