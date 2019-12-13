package gov.va.api.health.dataquery.tests.dstu2;

import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import gov.va.api.health.sentinel.categories.Local;
import lombok.experimental.Delegate;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class PractitionerIT {
  @Delegate Dstu2ResourceVerifier verifier = Dstu2ResourceVerifier.get();

  @Category({Local.class
    // , ProdDataQueryClinician.class
  })
  @Test
  public void advanced() {
    verifier.verifyAll(
        test(
            200, Practitioner.Bundle.class, "Practitioner?_id={id}", verifier.ids().practitioner()),
        test(404, OperationOutcome.class, "Practitioner?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?identifier={id}",
            verifier.ids().practitioner()));
  }

  @Category({Local.class
    // , ProdDataQueryPatient.class, ProdDataQueryClinician.class
  })
  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Practitioner.class, "Practitioner/{id}", verifier.ids().practitioner()),
        test(404, OperationOutcome.class, "Practitioner/{id}", verifier.ids().unknown()));
  }
}