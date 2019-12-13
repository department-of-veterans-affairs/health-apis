package gov.va.api.health.dataquery.tests.stu3;

import gov.va.api.health.sentinel.categories.Local;
import gov.va.api.health.stu3.api.resources.Location;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import lombok.experimental.Delegate;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class LocationIT {
  @Delegate private final Stu3ResourceVerifier verifier = Stu3ResourceVerifier.get();

  @Category({Local.class
    // , ProdDataQueryClinician.class
  })
  @Test
  public void advanced() {
    verifyAll(
        test(200, Location.Bundle.class, "Location?_id={id}", verifier.ids().location()),
        test(404, OperationOutcome.class, "Location?_id={id}", verifier.ids().unknown()),
        test(200, Location.Bundle.class, "Location?identifier={id}", verifier.ids().location()),
        test(404, OperationOutcome.class, "Location?identifier={id}", verifier.ids().unknown()));
  }

  @Category({Local.class
    // , ProdDataQueryPatient.class, ProdDataQueryClinician.class
  })
  @Test
  public void basic() {
    verifyAll(
        test(200, Location.class, "Location/{id}", verifier.ids().location()),
        test(404, OperationOutcome.class, "Location/{id}", verifier.ids().unknown()));
  }
}
