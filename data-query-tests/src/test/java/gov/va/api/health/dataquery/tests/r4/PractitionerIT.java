package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Practitioner;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class PractitionerIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Practitioner.class, "Practitioner/{id}", testIds.practitioner()),
        test(200, Practitioner.class, "Practitioner/npi-{npi}", testIds.practitioners().npi()),
        test(404, OperationOutcome.class, "Practitioner/{id}", testIds.unknown()));
  }

  @Test
  public void local() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(200, Practitioner.Bundle.class, "Practitioner?_id={id}", testIds.practitioner()),
        test(
            200,
            Practitioner.Bundle.class,
            p -> p.entry().isEmpty(),
            "Practitioner?_id={id}",
            testIds.unknown()));
  }

  @Test
  public void malformed() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(400, OperationOutcome.class, "Practitioner/"),
        test(400, OperationOutcome.class, "Practitioner?nonsense=abc"));
  }
}
