package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.NotInLab;
import gov.va.health.api.sentinel.categories.NotInLocal;
import gov.va.health.api.sentinel.categories.NotInProd;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class AllergyIntoleranceIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({NotInProd.class, NotInLab.class})
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
  @Category(NotInLocal.class)
  public void searchNotMe() {
    test(
        403,
        OperationOutcome.class,
        "AllergyIntolerance?patient={patient}",
        verifier.ids().unknown());
  }
}
