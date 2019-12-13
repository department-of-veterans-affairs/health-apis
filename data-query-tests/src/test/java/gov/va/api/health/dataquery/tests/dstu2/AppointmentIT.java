package gov.va.api.health.dataquery.tests.dstu2;

import gov.va.api.health.dstu2.api.resources.Appointment;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.categories.Local;
import lombok.experimental.Delegate;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class AppointmentIT {
  @Delegate Dstu2ResourceVerifier verifier = Dstu2ResourceVerifier.get();

  @Category({Local.class
    // , ProdDataQueryClinician.class
  })
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

  @Category({Local.class
    // ,ProdDataQueryPatient.class, ProdDataQueryClinician.class
  })
  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Appointment.class, "Appointment/{id}", verifier.ids().appointment()),
        test(404, OperationOutcome.class, "Appointment/{id}", verifier.ids().unknown()));
  }
}
