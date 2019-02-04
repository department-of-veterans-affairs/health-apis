package gov.va.api.health.argonaut.api.swaggerexamples;

import static java.util.Arrays.asList;

import gov.va.api.health.argonaut.api.datatypes.CodeableConcept;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.api.health.argonaut.api.resources.OperationOutcome.Issue;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerOperationOutcome {
  static final OperationOutcome SWAGGER_EXAMPLE_OPERATION_OUTCOME =
      OperationOutcome.builder()
          .resourceType("OperationOutcome")
          .issue(
              asList(
                  Issue.builder()
                      .severity(OperationOutcome.Issue.IssueSeverity.error)
                      .code("forbidden")
                      .details(
                          CodeableConcept.builder()
                              .text("Token not allowed access to this patient.")
                              .build())
                      .build()))
          .build();
}
