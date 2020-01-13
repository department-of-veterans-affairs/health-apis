package gov.va.api.health.dataquery.tests.stu3;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.sentinel.categories.Local;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import lombok.experimental.Delegate;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class PractitionerRoleIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.stu3();

  @Category({Local.class
    // , ProdDataQueryClinician.class
  })
  @Test
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            PractitionerRole.Bundle.class,
            "PractitionerRole?_id={id}",
            verifier.ids().practitioner()),
        test(404, OperationOutcome.class, "PractitionerRole?_id={id}", verifier.ids().unknown()),
        test(
            200,
            PractitionerRole.Bundle.class,
            "PractitionerRole?identifier={id}",
            verifier.ids().practitioner()));
  }

  @Category({Local.class
    // , ProdDataQueryPatient.class, ProdDataQueryClinician.class
  })
  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, PractitionerRole.class, "PractitionerRole/{id}", verifier.ids().practitioner()),
        test(404, OperationOutcome.class, "PractitionerRole/{id}", verifier.ids().unknown()));
  }
}
