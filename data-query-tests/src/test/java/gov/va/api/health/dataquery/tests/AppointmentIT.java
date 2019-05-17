package gov.va.api.health.dataquery.tests;

import gov.va.api.health.dataquery.api.resources.Appointment;
import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.categories.Local;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class AppointmentIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Category({Local.class, ProdDataQueryClinician.class})
  @Test
  public void advanced() {
    verifier.verifyAll(
        ResourceVerifier.test(
            200, Appointment.Bundle.class, "Appointment?_id={id}", verifier.ids().appointment()),
        ResourceVerifier.test(
            404, OperationOutcome.class, "Appointment?_id={id}", verifier.ids().unknown()),
        ResourceVerifier.test(
            200,
            Appointment.Bundle.class,
            "Appointment?identifier={id}",
            verifier.ids().appointment()),
        ResourceVerifier.test(
            200,
            Appointment.Bundle.class,
            "Appointment?patient={patient}",
            verifier.ids().patient()));
  }

  @Category({Local.class, ProdDataQueryPatient.class, ProdDataQueryClinician.class})
  @Test
  public void basic() {
    verifier.verifyAll(
        ResourceVerifier.test(
            200, Appointment.class, "Appointment/{id}", verifier.ids().appointment()),
        ResourceVerifier.test(
            404, OperationOutcome.class, "Appointment/{id}", verifier.ids().unknown()));
  }
}
