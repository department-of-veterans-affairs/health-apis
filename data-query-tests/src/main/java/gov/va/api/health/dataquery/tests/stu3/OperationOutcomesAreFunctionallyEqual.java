package gov.va.api.health.dataquery.tests.stu3;

import gov.va.api.health.sentinel.ErrorsAreFunctionallyEqual;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import io.restassured.response.ResponseBody;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OperationOutcomesAreFunctionallyEqual implements ErrorsAreFunctionallyEqual {
  /** Remove fields unique to each instance: generated ID, timestamp, and encrypted data. */
  private OperationOutcome asOperationOutcomeWithoutDiagnostics(ResponseBody<?> body) {
    try {
      OperationOutcome oo = body.as(OperationOutcome.class);
      oo.id("REMOVED-FOR-COMPARISON");
      oo.extension().stream()
          .filter(e -> e.url().equals("timestamp"))
          .forEach(e -> e.valueInstant("REMOVED-FOR-COMPARISON"));
      oo.extension().stream()
          .filter(e -> List.of("message", "cause", "request").contains(e.url()))
          .forEach(e -> e.valueString("REMOVED-FOR-COMPARISON"));
      return oo;
    } catch (Exception e) {
      log.error("Failed read response as OperationOutcome: {}", body.prettyPrint());
      throw e;
    }
  }

  @Override
  public boolean equals(ResponseBody<?> left, ResponseBody<?> right) {
    OperationOutcome ooLeft = asOperationOutcomeWithoutDiagnostics(left);
    OperationOutcome ooRight = asOperationOutcomeWithoutDiagnostics(right);
    return ooLeft.equals(ooRight);
  }
}
