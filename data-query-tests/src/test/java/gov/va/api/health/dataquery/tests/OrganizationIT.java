package gov.va.api.health.dataquery.tests;

import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import gov.va.api.health.dataquery.api.resources.Organization;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.categories.Local;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class OrganizationIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Category({Local.class, ProdDataQueryClinician.class})
  @Test
  public void advanced() {
    verifier.verifyAll(
        ResourceVerifier.test(
            200, Organization.Bundle.class, "Organization?_id={id}", verifier.ids().organization()),
        ResourceVerifier.test(
            404, OperationOutcome.class, "Organization?_id={id}", verifier.ids().unknown()),
        ResourceVerifier.test(
            200,
            Organization.Bundle.class,
            "Organization?identifier={id}",
            verifier.ids().organization()));
  }

  @Category({Local.class, ProdDataQueryPatient.class, ProdDataQueryClinician.class})
  @Test
  public void basic() {
    verifier.verifyAll(
        ResourceVerifier.test(
            200, Organization.class, "Organization/{id}", verifier.ids().organization()),
        ResourceVerifier.test(
            404, OperationOutcome.class, "Organization/{id}", verifier.ids().unknown()));
  }
}
