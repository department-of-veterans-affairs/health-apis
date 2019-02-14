package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.health.api.sentinel.Sentinel.Environment;
import gov.va.health.api.sentinel.categories.LabArgo;
import gov.va.health.api.sentinel.categories.LabCargo;
import gov.va.health.api.sentinel.categories.Local;
import gov.va.health.api.sentinel.categories.ProdArgo;
import gov.va.health.api.sentinel.categories.ProdCargo;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class PatientIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({Local.class, LabCargo.class, ProdCargo.class})
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            Patient.Bundle.class,
            "Patient?family={family}&gender={gender}",
            verifier.ids().pii().family(),
            verifier.ids().pii().gender()),
        test(
            200,
            Patient.Bundle.class,
            "Patient?given={given}&gender={gender}",
            verifier.ids().pii().given(),
            verifier.ids().pii().gender()),
        test(
            200,
            Patient.Bundle.class,
            "Patient?name={name}&birthdate={birthdate}",
            verifier.ids().pii().name(),
            verifier.ids().pii().birthdate()),
        test(
            200,
            Patient.Bundle.class,
            "Patient?name={name}&gender={gender}",
            verifier.ids().pii().name(),
            verifier.ids().pii().gender()));
  }

  @Test
  @Category({Local.class, LabArgo.class, LabCargo.class, ProdArgo.class, ProdCargo.class})
  public void basic() {
    verifier.verifyAll(
        test(200, Patient.class, "Patient/{id}", verifier.ids().patient()),
        test(200, Patient.Bundle.class, "Patient?_id={id}", verifier.ids().patient()));
  }

  /**
   * The CDW database has disabled patient searching by identifier for both PROD/QA. We will test
   * this only in LOCAL mode against the sandbox db.
   */
  @Test
  @Category(Local.class)
  public void patientIdentifierSearching() {
    verifier.verify(
        test(200, Patient.Bundle.class, "Patient?identifier={id}", verifier.ids().patient()));
  }

  /**
   * In the PROD/QA environments, patient reading is restricted to your unique access-token. Any IDs
   * but your own are revoked with a 403 Forbidden. In environments where this restriction is
   * lifted, the result of an unknown ID should be 404 Not Found.
   */
  @Test
  @Category({Local.class, LabArgo.class, LabCargo.class, ProdArgo.class, ProdCargo.class})
  public void patientMatching() {
    if (Sentinel.environment() == Environment.LOCAL) {
      verifier.verifyAll(
          test(404, OperationOutcome.class, "Patient/{id}", verifier.ids().unknown()),
          test(404, OperationOutcome.class, "Patient?_id={id}", verifier.ids().unknown()));
    } else {
      verifier.verifyAll(
          test(403, OperationOutcome.class, "Patient/{id}", verifier.ids().unknown()),
          test(403, OperationOutcome.class, "Patient?_id={id}", verifier.ids().unknown()));
    }
  }
}
