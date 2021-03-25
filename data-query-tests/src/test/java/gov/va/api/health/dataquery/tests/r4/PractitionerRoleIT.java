package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.PractitionerRole;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class PractitionerRoleIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, PractitionerRole.class, "PractitionerRole/{id}", testIds.practitioner()),
        test(404, OperationOutcome.class, "PractitionerRole/{id}", testIds.unknown()),
        // search by _id
        test(
            200,
            PractitionerRole.Bundle.class,
            "PractitionerRole?_id={id}",
            testIds.practitioner()));
  }

  @Test
  public void malformed() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(400, OperationOutcome.class, "PractitionerRole/"),
        test(400, OperationOutcome.class, "PractitionerRole?blah=123"));
  }
}
