package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class ConditionIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  void read() {
    verifyAll(
        test(200, Condition.class, "Condition/{id}", testIds.condition()),
        test(404, OperationOutcome.class, "Condition/{id}", testIds.unknown()));
  }

  @Test
  public void search() {
    verifyAll(
        test(200, Condition.Bundle.class, "Condition?_id={id}", testIds.condition()),
        test(
            200,
            Condition.Bundle.class,
            r -> r.entry().isEmpty(),
            "Condition?_id={id}",
            testIds.unknown()),
        test(200, Condition.Bundle.class, "Condition?identifier={id}", testIds.condition()),
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&category=problem-list-item",
            testIds.patient()),
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&category=http://terminology.hl7.org/CodeSystem/condition-category|problem-list-item",
            testIds.patient()),
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&clinical-status=active",
            testIds.patient()),
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&clinical-status=http://terminology.hl7.org/CodeSystem/condition-clinical|active,resolved",
            testIds.patient()),
        test(200, Condition.Bundle.class, "Condition?patient={patient}", testIds.patient()));
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(403, OperationOutcome.class, "Condition?patient={patient}", testIds.unknown()));
  }
}
