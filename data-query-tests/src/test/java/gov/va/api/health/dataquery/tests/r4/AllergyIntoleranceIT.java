package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.AllergyIntolerance;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import java.util.function.Predicate;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class AllergyIntoleranceIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  private Predicate<AllergyIntolerance.Bundle> bundleIsNotEmpty() {
    return bundle -> !bundle.entry().isEmpty();
  }

  @Test
  public void read() {
    verifyAll(
        test(
            200, AllergyIntolerance.class, "AllergyIntolerance/{id}", testIds.allergyIntolerance()),
        test(404, OperationOutcome.class, "AllergyIntolerance/{id}", testIds.unknown()));
  }

  @Test
  public void search() {
    verifyAll(
        test(
            200,
            AllergyIntolerance.Bundle.class,
            bundleIsNotEmpty(),
            "AllergyIntolerance?_id={id}",
            testIds.allergyIntolerance()),
        test(
            200,
            AllergyIntolerance.Bundle.class,
            bundleIsNotEmpty().negate(),
            "AllergyIntolerance?_id={id}",
            testIds.unknown()),
        test(
            200,
            AllergyIntolerance.Bundle.class,
            bundleIsNotEmpty(),
            "AllergyIntolerance?identifier={id}",
            testIds.allergyIntolerance()),
        test(
            200,
            AllergyIntolerance.Bundle.class,
            bundleIsNotEmpty(),
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
