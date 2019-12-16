package gov.va.api.health.dataquery.tests.dstu2;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dstu2.api.resources.Location;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.categories.Local;
import lombok.experimental.Delegate;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class LocationIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.dstu2();

  @Category({Local.class
    // , ProdDataQueryClinician.class
  })
  @Test
  public void advanced() {
    verifier.verifyAll(
        test(200, Location.Bundle.class, "Location?_id={id}", verifier.ids().location()),
        test(404, OperationOutcome.class, "Location?_id={id}", verifier.ids().unknown()),
        test(200, Location.Bundle.class, "Location?identifier={id}", verifier.ids().location()));
  }

  @Category({Local.class
    // , ProdDataQueryPatient.class, ProdDataQueryClinician.class
  })
  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Location.class, "Location/{id}", verifier.ids().location()),
        test(404, OperationOutcome.class, "Location/{id}", verifier.ids().unknown()));
  }
}
