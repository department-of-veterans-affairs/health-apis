package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Appointment;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.NotInLab;
import gov.va.health.api.sentinel.categories.NotInLocal;
import gov.va.health.api.sentinel.categories.NotInProd;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class AppointmentIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Category({NotInLab.class, NotInProd.class})
  @Test
  public void advanced() {
    verifier.verifyAll(
        test(200, Appointment.Bundle.class, "Appointment?_id={id}", verifier.ids().appointment()),
        test(404, OperationOutcome.class, "Appointment?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?identifier={id}",
            verifier.ids().appointment()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?patient={patient}",
            verifier.ids().patient()));
  }

  @Category(NotInLab.class)
  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Appointment.class, "Appointment/{id}", verifier.ids().appointment()),
        test(404, OperationOutcome.class, "Appointment/{id}", verifier.ids().unknown()));
  }

  @Test
  @Category(NotInLocal.class)
  public void searchNotMe() {
    test(403, OperationOutcome.class, "Appointment?patient={patient}", verifier.ids().unknown());
  }
}
