package gov.va.api.health.dataquery.tests.dstu2;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.categories.Local;
import lombok.experimental.Delegate;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class AppointmentIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.dstu2();

  @Category({Local.class
    // , ProdDataQueryClinician.class
  })
  @Test
  public void advanced() {
    verifier.verifyAll(
        test(501, OperationOutcome.class, "Appointment?_id={id}", verifier.ids().appointment()),
        test(501, OperationOutcome.class, "Appointment?_id={id}", verifier.ids().unknown()),
        test(
            501,
            OperationOutcome.class,
            "Appointment?identifier={id}",
            verifier.ids().appointment()),
        test(
            501,
            OperationOutcome.class,
            "Appointment?patient={patient}",
            verifier.ids().patient()));
  }

  @Category({Local.class
    // ,ProdDataQueryPatient.class, ProdDataQueryClinician.class
  })
  @Test
  public void basic() {
    verifier.verifyAll(
        test(501, OperationOutcome.class, "Appointment/{id}", verifier.ids().appointment()),
        test(501, OperationOutcome.class, "Appointment/{id}", verifier.ids().unknown()));
  }
}
