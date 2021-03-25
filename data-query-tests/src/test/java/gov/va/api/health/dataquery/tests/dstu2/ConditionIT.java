package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.dstu2.api.resources.Condition;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class ConditionIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.dstu2();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void advanced() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(200, Condition.Bundle.class, "Condition?_id={id}", testIds.condition()),
        test(404, OperationOutcome.class, "Condition?_id={id}", testIds.unknown()),
        test(200, Condition.Bundle.class, "Condition?identifier={id}", testIds.condition()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&category=problem",
            testIds.patient()),
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&category=health-concern",
            testIds.patient()),
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&clinicalstatus=active",
            testIds.patient()),
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&clinicalstatus=active,resolved",
            testIds.patient()),
        test(200, Condition.class, "Condition/{id}", testIds.condition()),
        test(404, OperationOutcome.class, "Condition/{id}", testIds.unknown()),
        test(200, Condition.Bundle.class, "Condition?patient={patient}", testIds.patient()));
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(403, OperationOutcome.class, "Condition?patient={patient}", testIds.unknown()));
  }
}
