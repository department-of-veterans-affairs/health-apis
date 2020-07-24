package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.dstu2.api.resources.Organization;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class OrganizationIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.dstu2();

  @Test
  public void advanced() {
    assumeEnvironmentIn(Environment.LOCAL);
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

  @Test
  public void basic() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(200, Organization.class, "Organization/{id}", verifier.ids().organization()),
        test(404, OperationOutcome.class, "Organization/{id}", verifier.ids().unknown()));
  }
}
