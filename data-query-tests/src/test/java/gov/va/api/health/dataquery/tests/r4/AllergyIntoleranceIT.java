package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.uscorer4.api.resources.AllergyIntolerance;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assumptions.assumeThat;

public class AllergyIntoleranceIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  @BeforeAll
  static void assumeEnvironment() {
    // These tests invent data that will not be cleaned up
    // To avoid polluting the database, they should only run locally
    assumeThat(Environment.get())
            .overridingErrorMessage("Skipping AllergyIntoleranceIT in " + Environment.get())
            .isEqualTo(Environment.LOCAL);
  }

  @Test
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            AllergyIntolerance.Bundle.class,
            "AllergyIntolerance?_id={id}",
            verifier.ids().allergyIntolerance()),
        test(404, OperationOutcome.class, "AllergyIntolerance?_id={id}", verifier.ids().unknown()),
        test(
            200,
            AllergyIntolerance.Bundle.class,
            "AllergyIntolerance?identifier={id}",
            verifier.ids().allergyIntolerance()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            AllergyIntolerance.class,
            "AllergyIntolerance/{id}",
            verifier.ids().allergyIntolerance()),
        test(404, OperationOutcome.class, "AllergyIntolerance/{id}", verifier.ids().unknown()),
        test(
            200,
            AllergyIntolerance.Bundle.class,
            "AllergyIntolerance?patient={patient}",
            verifier.ids().patient()));
  }

  @Test
  public void searchNotMe() {
    verifier.verifyAll(
        test(
            403,
            OperationOutcome.class,
            "AllergyIntolerance?patient={patient}",
            verifier.ids().unknown()));
  }
}
