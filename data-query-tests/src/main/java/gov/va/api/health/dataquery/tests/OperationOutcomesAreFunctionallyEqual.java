package gov.va.api.health.dataquery.tests;

import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.ErrorsAreFunctionallyEqual;
import io.restassured.response.ResponseBody;
import lombok.extern.slf4j.Slf4j;

/**
 * Compares two OperationalOutcomes present in a response after first removing the transient time
 * stamp data.
 */
@Slf4j
public class OperationOutcomesAreFunctionallyEqual implements ErrorsAreFunctionallyEqual {

  /**
   * Remove data from the OO that is unique for each instance. This includes the generated ID and
   * the timestamp.
   */
  private OperationOutcome asOperationOutcomeWithoutDiagnostics(ResponseBody<?> body) {
    try {
      OperationOutcome oo = body.as(OperationOutcome.class);
      oo.id("REMOVED-FOR-COMPARISON");
      oo.issue()
          .forEach(
              i -> {
                if (i.diagnostics() != null) {
                  i.diagnostics(
                      i.diagnostics()
                          .replaceAll("Timestamp:.*(\n|$)", "Timestamp:REMOVED-FOR-COMPARISON"));
                }
              });
      return oo;
    } catch (Exception e) {
      log.error("Failed read response as OperationOutcome: {}", body.prettyPrint());
      throw e;
    }
  }

  @Override
  public boolean equals(ResponseBody<?> responseBody1, ResponseBody<?> responseBody2) {
    OperationOutcome oo1 = asOperationOutcomeWithoutDiagnostics(responseBody1);
    OperationOutcome oo2 = asOperationOutcomeWithoutDiagnostics(responseBody2);
    return oo1.equals(oo2);
  }
}
