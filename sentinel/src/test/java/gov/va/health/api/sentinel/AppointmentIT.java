package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Appointment;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.Local;
import gov.va.health.api.sentinel.categories.ProdArgo;
import gov.va.health.api.sentinel.categories.ProdCargo;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class AppointmentIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Category({Local.class, ProdCargo.class})
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

  @Category({Local.class, ProdArgo.class, ProdCargo.class})
  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Appointment.class, "Appointment/{id}", verifier.ids().appointment()),
        test(404, OperationOutcome.class, "Appointment/{id}", verifier.ids().unknown()));
  }
}
