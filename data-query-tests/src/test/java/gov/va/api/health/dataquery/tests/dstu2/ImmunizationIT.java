package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.dstu2.api.resources.Immunization;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class ImmunizationIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.dstu2();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void advanced() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(200, Immunization.Bundle.class, "Immunization?_id={id}", testIds.immunization()),
        test(404, OperationOutcome.class, "Immunization?_id={id}", testIds.unknown()),
        test(
            200,
            Immunization.Bundle.class,
            "Immunization?identifier={id}",
            testIds.immunization()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Immunization.class, "Immunization/{id}", testIds.immunization()),
        test(404, OperationOutcome.class, "Immunization/{id}", testIds.unknown()),
        test(200, Immunization.Bundle.class, "Immunization?patient={patient}", testIds.patient()));
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(403, OperationOutcome.class, "Immunization?patient={patient}", testIds.unknown()));
  }
}
