package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.NotInLab;
import gov.va.health.api.sentinel.categories.NotInLocal;
import gov.va.health.api.sentinel.categories.NotInProd;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ConditionIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({NotInProd.class, NotInLab.class})
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&category=problem",
            verifier.ids().patient()),
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&category=health-concern",
            verifier.ids().patient()),
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&clinicalstatus=active",
            verifier.ids().patient()),
        test(200, Condition.Bundle.class, "Condition?_id={id}", verifier.ids().condition()),
        test(404, OperationOutcome.class, "Condition?_id={id}", verifier.ids().unknown()),
        test(200, Condition.Bundle.class, "Condition?identifier={id}", verifier.ids().condition()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Condition.class, "Condition/{id}", verifier.ids().condition()),
        test(404, OperationOutcome.class, "Condition/{id}", verifier.ids().unknown()),
        test(200, Condition.Bundle.class, "Condition?patient={patient}", verifier.ids().patient()));
  }

  @Test
  @Category(NotInLocal.class)
  public void searchNotMe() {
    test(403, OperationOutcome.class, "Condition?patient={patient}", verifier.ids().unknown());
  }
}
