package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.NotInProd;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ConditionIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category(NotInProd.class)
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            Condition.Bundle.class,
            "/api/Condition?patient={patient}&category=problem",
            verifier.ids().patient()),
        test(
            200,
            Condition.Bundle.class,
            "/api/Condition?patient={patient}&category=health-concern",
            verifier.ids().patient()),
        test(
            200,
            Condition.Bundle.class,
            "/api/Condition?patient={patient}&clinicalstatus=active",
            verifier.ids().patient()),
        test(200, Condition.Bundle.class, "/api/Condition?_id={id}", verifier.ids().condition()),
        test(404, OperationOutcome.class, "/api/Condition?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Condition.Bundle.class,
            "/api/Condition?identifier={id}",
            verifier.ids().condition()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Condition.class, "/api/Condition/{id}", verifier.ids().condition()),
        test(404, OperationOutcome.class, "/api/Condition/{id}", verifier.ids().unknown()),
        test(
            200,
            Condition.Bundle.class,
            "/api/Condition?patient={patient}",
            verifier.ids().patient()));
  }
}
