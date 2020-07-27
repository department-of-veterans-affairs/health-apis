package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.uscorer4.api.resources.Condition;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class ConditionIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  public void advanced() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(200, Condition.Bundle.class, "Condition?_id={id}", verifier.ids().condition()),
        test(404, OperationOutcome.class, "Condition?_id={id}", verifier.ids().unknown()),
        test(200, Condition.Bundle.class, "Condition?identifier={id}", verifier.ids().condition()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&category=problem-list-item",
            verifier.ids().patient()),
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&category=http://terminology.hl7.org/CodeSystem/condition-category|problem-list-item",
            verifier.ids().patient()),
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&clinical-status=active",
            verifier.ids().patient()),
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&clinical-status=http://terminology.hl7.org/CodeSystem/condition-clinical|active,resolved",
            verifier.ids().patient()),
        test(200, Condition.class, "Condition/{id}", verifier.ids().condition()),
        test(404, OperationOutcome.class, "Condition/{id}", verifier.ids().unknown()),
        test(200, Condition.Bundle.class, "Condition?patient={patient}", verifier.ids().patient()));
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(403, OperationOutcome.class, "Condition?patient={patient}", verifier.ids().unknown()));
  }
}
