package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.api.health.argonaut.api.resources.Organization;
import gov.va.health.api.sentinel.categories.Local;
import gov.va.health.api.sentinel.categories.ProdArgo;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class OrganizationIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Category(Local.class)
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

  @Category({Local.class, ProdArgo.class})
  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Organization.class, "Organization/{id}", verifier.ids().organization()),
        test(404, OperationOutcome.class, "Organization/{id}", verifier.ids().unknown()));
  }
}
