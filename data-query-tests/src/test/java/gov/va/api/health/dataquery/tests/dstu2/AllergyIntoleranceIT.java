package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.dataquery.tests.TestAssumptionUtility.assumeAllButLocal;
import static gov.va.api.health.dataquery.tests.TestAssumptionUtility.assumeLocal;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class AllergyIntoleranceIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.dstu2();

  @Test
  public void advanced() {
    assumeLocal();
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
    assumeAllButLocal();
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
    assumeAllButLocal();
    verifier.verifyAll(
        test(
            403,
            OperationOutcome.class,
            "AllergyIntolerance?patient={patient}",
            verifier.ids().unknown()));
  }
}
