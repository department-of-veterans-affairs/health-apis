package gov.va.api.health.dataquery.tests.dstu2;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dstu2.api.resources.Encounter;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.categories.Local;
import lombok.experimental.Delegate;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class EncounterIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.dstu2();

  @Category({Local.class
    // , ProdDataQueryClinician.class
  })
  @Test
  public void advanced() {
    verifier.verifyAll(
        test(200, Encounter.Bundle.class, "Encounter?_id={id}", verifier.ids().encounter()),
        test(404, OperationOutcome.class, "Encounter?_id={id}", verifier.ids().unknown()),
        test(200, Encounter.Bundle.class, "Encounter?identifier={id}", verifier.ids().encounter()));
  }

  @Category({Local.class
    // , ProdDataQueryPatient.class, ProdDataQueryClinician.class
  })
  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Encounter.class, "Encounter/{id}", verifier.ids().encounter()),
        test(404, OperationOutcome.class, "Encounter/{id}", verifier.ids().unknown()));
  }
}
