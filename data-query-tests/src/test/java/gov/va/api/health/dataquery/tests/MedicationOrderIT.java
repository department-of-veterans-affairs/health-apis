package gov.va.api.health.dataquery.tests;

import gov.va.api.health.dataquery.api.resources.MedicationOrder;
import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.categories.Local;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MedicationOrderIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({Local.class, LabDataQueryClinician.class, ProdDataQueryClinician.class})
  public void advanced() {
    verifier.verifyAll(
        ResourceVerifier.test(
            200,
            MedicationOrder.Bundle.class,
            "MedicationOrder?_id={id}",
            verifier.ids().medicationOrder()),
        ResourceVerifier.test(
            404, OperationOutcome.class, "MedicationOrder?_id={id}", verifier.ids().unknown()),
        ResourceVerifier.test(
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
        ResourceVerifier.test(
            200, MedicationOrder.class, "MedicationOrder/{id}", verifier.ids().medicationOrder()),
        ResourceVerifier.test(
            404, OperationOutcome.class, "MedicationOrder/{id}", verifier.ids().unknown()),
        ResourceVerifier.test(
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
        ResourceVerifier.test(
            403,
            OperationOutcome.class,
            "MedicationOrder?patient={patient}",
            verifier.ids().unknown()));
  }
}
