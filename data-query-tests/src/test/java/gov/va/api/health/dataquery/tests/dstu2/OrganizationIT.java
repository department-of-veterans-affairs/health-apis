package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.dstu2.api.resources.Organization;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class OrganizationIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.dstu2();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void advanced() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(200, Organization.Bundle.class, "Organization?_id={id}", testIds.organization()),
        test(404, OperationOutcome.class, "Organization?_id={id}", testIds.unknown()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?identifier={id}",
            testIds.organization()));
  }

  @Test
  public void basic() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(200, Organization.class, "Organization/{id}", testIds.organization()),
        test(404, OperationOutcome.class, "Organization/{id}", testIds.unknown()));
  }
}
