package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.categories.Local;
import gov.va.api.health.uscorer4.api.resources.MedicationRequest;
import lombok.experimental.Delegate;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MedicationRequestIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  @Category({Local.class, LabDataQueryClinician.class, ProdDataQueryClinician.class})
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            MedicationRequest.Bundle.class,
            "MedicationRequest?_id={id}",
            verifier.ids().medicationOrder()),
        test(
            200,
            MedicationRequest.Bundle.class,
            "MedicationRequest?_id={id}",
            verifier.ids().medicationStatement()),
        test(404, OperationOutcome.class, "MedicationRequest?_id={id}", verifier.ids().unknown()),
        test(
            200,
            MedicationRequest.Bundle.class,
            "MedicationRequest?identifier={id}",
            verifier.ids().medicationOrder()),
        test(
            200,
            MedicationRequest.Bundle.class,
            "MedicationRequest?identifier={id}",
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
        // Patient And Intent
        test(
            200,
            MedicationRequest.Bundle.class,
            "MedicationRequest?patient={patient}&intent=order",
            verifier.ids().patient()),
        test(
            200,
            MedicationRequest.Bundle.class,
            "MedicationRequest?patient={patient}&intent=plan",
            verifier.ids().patient()),
        // MedicationRequest Public Id
        test(
            200,
            MedicationRequest.class,
            "MedicationRequest/{id}",
            verifier.ids().medicationOrder()),
        test(
            200,
            MedicationRequest.class,
            "MedicationRequest/{id}",
            verifier.ids().medicationStatement()),
        test(404, OperationOutcome.class, "MedicationRequest/{id}", verifier.ids().unknown()),
        // Patient Icn
        test(
            200,
            MedicationRequest.Bundle.class,
            "MedicationRequest?patient={patient}",
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
            "MedicationRequest?patient={patient}",
            verifier.ids().unknown()));
  }
}
