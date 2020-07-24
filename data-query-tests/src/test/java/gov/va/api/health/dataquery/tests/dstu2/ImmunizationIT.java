package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class ImmunizationIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.dstu2();

  @Test
  public void advanced() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(
            200, Immunization.Bundle.class, "Immunization?_id={id}", verifier.ids().immunization()),
        test(404, OperationOutcome.class, "Immunization?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Immunization.Bundle.class,
            "Immunization?identifier={id}",
            verifier.ids().immunization()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Immunization.class, "Immunization/{id}", verifier.ids().immunization()),
        test(404, OperationOutcome.class, "Immunization/{id}", verifier.ids().unknown()),
        test(
            200,
            Immunization.Bundle.class,
            "Immunization?patient={patient}",
            verifier.ids().patient()));
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(
            403,
            OperationOutcome.class,
            "Immunization?patient={patient}",
            verifier.ids().unknown()));
  }
}
