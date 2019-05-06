package gov.va.api.health.sentinel;

import static gov.va.api.health.sentinel.ResourceVerifier.test;

import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.dstu2.api.resources.Organization;
import gov.va.api.health.sentinel.categories.Local;
import gov.va.api.health.sentinel.categories.ProdDataQueryClinician;
import gov.va.api.health.sentinel.categories.ProdDataQueryPatient;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class OrganizationIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Category({Local.class, ProdDataQueryClinician.class})
  @Test
  public void advanced() {
    verifier.verifyAll(
        test(
            200, Organization.Bundle.class, "Organization?_id={id}", verifier.ids().organization()),
        test(404, OperationOutcome.class, "Organization?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?identifier={id}",
            verifier.ids().organization()));
  }

  @Category({Local.class, ProdDataQueryPatient.class, ProdDataQueryClinician.class})
  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Organization.class, "Organization/{id}", verifier.ids().organization()),
        test(404, OperationOutcome.class, "Organization/{id}", verifier.ids().unknown()));
  }
}
