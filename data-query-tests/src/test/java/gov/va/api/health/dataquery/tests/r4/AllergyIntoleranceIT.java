package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.AllergyIntolerance;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class AllergyIntoleranceIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void advanced() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(
            200,
            AllergyIntolerance.Bundle.class,
            "AllergyIntolerance?_id={id}",
            testIds.allergyIntolerance()),
        test(404, OperationOutcome.class, "AllergyIntolerance?_id={id}", testIds.unknown()),
        test(
            200,
            AllergyIntolerance.Bundle.class,
            "AllergyIntolerance?identifier={id}",
            testIds.allergyIntolerance()));
  }

  @Test
  public void basic() {

    verifier.verifyAll(
        test(
            200, AllergyIntolerance.class, "AllergyIntolerance/{id}", testIds.allergyIntolerance()),
        test(404, OperationOutcome.class, "AllergyIntolerance/{id}", testIds.unknown()),
        test(
            200,
            AllergyIntolerance.Bundle.class,
            "AllergyIntolerance?patient={patient}",
            testIds.patient()));
  }

  @Test
  public void searchNotMe() {

    assumeEnvironmentNotIn(Environment.LOCAL);

    verifier.verifyAll(
        test(
            403,
            OperationOutcome.class,
            "AllergyIntolerance?patient={patient}",
            testIds.unknown()));
  }
}
