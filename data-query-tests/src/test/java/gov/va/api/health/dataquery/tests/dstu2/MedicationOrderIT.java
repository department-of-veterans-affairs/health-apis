package gov.va.api.health.dataquery.tests.dstu2;

import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.categories.Local;
import lombok.experimental.Delegate;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MedicationOrderIT {
  @Delegate Dstu2ResourceVerifier verifier = Dstu2ResourceVerifier.get();

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
