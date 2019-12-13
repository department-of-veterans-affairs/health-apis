package gov.va.api.health.dataquery.tests.dstu2;

import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.categories.Local;
import lombok.experimental.Delegate;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MedicationStatementIT {
  @Delegate Dstu2ResourceVerifier verifier = Dstu2ResourceVerifier.get();

  @Test
  @Category({Local.class, LabDataQueryClinician.class, ProdDataQueryClinician.class})
  public void advanced() {
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
  @Category({
    Local.class,
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void basic() {
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
            "MedicationStatement?patient={patient}",
            verifier.ids().unknown()));
  }
}
