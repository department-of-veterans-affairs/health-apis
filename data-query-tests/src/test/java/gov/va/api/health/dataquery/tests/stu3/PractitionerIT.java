package gov.va.api.health.dataquery.tests.stu3;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.categories.Local;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import gov.va.api.health.stu3.api.resources.Practitioner;
import lombok.experimental.Delegate;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class PractitionerIT {
  @Delegate private final ResourceVerifier verifier = ResourceVerifier.stu3();

  @Category({Local.class,
  //ProdDataQueryPatient.class, LabDataQueryPatient.class, ProdDataQueryClinician.class
  })
  @Test
  public void advanced() {
    verifyAll(
        test(
            200, Practitioner.Bundle.class, "Practitioner?_id={id}", verifier.ids().practitioner()),
        test(404, OperationOutcome.class, "Practitioner?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?family={family}&given={given}",
            verifier.ids().practitioners().family(),
            verifier.ids().practitioners().given()),
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?identifier={npi}",
            verifier.ids().practitioners().npi()));
  }

  @Test
  @Category({Local.class, ProdDataQueryPatient.class, LabDataQueryPatient.class
    // , ProdDataQueryClinician.class
  })
  public void basic() {
    verifier.verifyAll(
        test(200, Practitioner.class, "Practitioner/{id}", verifier.ids().practitioner()),
        test(404, OperationOutcome.class, "Practitioner/{id}", verifier.ids().unknown()));
  }
}
