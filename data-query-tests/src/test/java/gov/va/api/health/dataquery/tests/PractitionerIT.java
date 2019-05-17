package gov.va.api.health.dataquery.tests;

import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import gov.va.api.health.dataquery.api.resources.Practitioner;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.categories.Local;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class PractitionerIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Category({Local.class, ProdDataQueryClinician.class})
  @Test
  public void advanced() {
    verifier.verifyAll(
        ResourceVerifier.test(
            200, Practitioner.Bundle.class, "Practitioner?_id={id}", verifier.ids().practitioner()),
        ResourceVerifier.test(
            404, OperationOutcome.class, "Practitioner?_id={id}", verifier.ids().unknown()),
        ResourceVerifier.test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?identifier={id}",
            verifier.ids().practitioner()));
  }

  @Category({Local.class, ProdDataQueryPatient.class, ProdDataQueryClinician.class})
  @Test
  public void basic() {
    verifier.verifyAll(
        ResourceVerifier.test(
            200, Practitioner.class, "Practitioner/{id}", verifier.ids().practitioner()),
        ResourceVerifier.test(
            404, OperationOutcome.class, "Practitioner/{id}", verifier.ids().unknown()));
  }
}
