package gov.va.api.health.dataquery.tests;

import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import gov.va.api.health.dataquery.api.resources.Patient;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.categories.Local;
import gov.va.api.health.sentinel.categories.Smoke;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class PatientIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({Local.class, LabDataQueryClinician.class, ProdDataQueryClinician.class})
  public void advanced() {
    verifier.verifyAll(
        ResourceVerifier.test(
            200,
            Patient.Bundle.class,
            "Patient?family={family}&gender={gender}",
            verifier.ids().pii().family(),
            verifier.ids().pii().gender()),
        ResourceVerifier.test(
            200,
            Patient.Bundle.class,
            "Patient?given={given}&gender={gender}",
            verifier.ids().pii().given(),
            verifier.ids().pii().gender()),
        ResourceVerifier.test(
            200,
            Patient.Bundle.class,
            "Patient?name={name}&birthdate={birthdate}",
            verifier.ids().pii().name(),
            verifier.ids().pii().birthdate()),
        ResourceVerifier.test(
            200,
            Patient.Bundle.class,
            "Patient?name={name}&gender={gender}",
            verifier.ids().pii().name(),
            verifier.ids().pii().gender()));
  }

  @Test
  @Category({
    Local.class,
    Smoke.class,
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void basic() {
    verifier.verifyAll(
        ResourceVerifier.test(200, Patient.class, "Patient/{id}", verifier.ids().patient()),
        ResourceVerifier.test(
            200, Patient.Bundle.class, "Patient?_id={id}", verifier.ids().patient()));
  }

  /**
   * The CDW database has disabled patient searching by identifier for both PROD/QA. We will test
   * this only in LOCAL mode against the sandbox db.
   */
  @Test
  @Category(Local.class)
  public void patientIdentifierSearching() {
    verifier.verify(
        ResourceVerifier.test(
            200, Patient.Bundle.class, "Patient?identifier={id}", verifier.ids().patient()));
  }

  /**
   * In the PROD/QA environments, patient reading is restricted to your unique access-token. Any IDs
   * but your own are revoked with a 403 Forbidden. In environments where this restriction is
   * lifted, the result of an unknown ID should be 404 Not Found.
   */
  @Test
  @Category({
    Local.class,
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void patientMatching() {
    if (Environment.get() == Environment.LOCAL) {
      verifier.verifyAll(
          ResourceVerifier.test(
              404, OperationOutcome.class, "Patient/{id}", verifier.ids().unknown()),
          ResourceVerifier.test(
              404, OperationOutcome.class, "Patient?_id={id}", verifier.ids().unknown()));
    } else {
      verifier.verifyAll(
          ResourceVerifier.test(
              403, OperationOutcome.class, "Patient/{id}", verifier.ids().unknown()),
          ResourceVerifier.test(
              403, OperationOutcome.class, "Patient?_id={id}", verifier.ids().unknown()));
    }
  }
}
