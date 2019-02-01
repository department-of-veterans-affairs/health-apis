package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Appointment;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.NotInLab;
import gov.va.health.api.sentinel.categories.NotInProd;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(NotInLab.class)
public class AppointmentIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category(NotInProd.class)
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            Appointment.Bundle.class,
            "/api/Appointment?_id={id}",
            verifier.ids().appointment()),
        test(404, OperationOutcome.class, "/api/Appointment?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Appointment.Bundle.class,
            "/api/Appointment?identifier={id}",
            verifier.ids().appointment()),
        test(
            200,
            Appointment.Bundle.class,
            "/api/Appointment?patient={patient}",
            verifier.ids().patient()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Appointment.class, "/api/Appointment/{id}", verifier.ids().appointment()),
        test(404, OperationOutcome.class, "/api/Appointment/{id}", verifier.ids().unknown()));
  }
}
