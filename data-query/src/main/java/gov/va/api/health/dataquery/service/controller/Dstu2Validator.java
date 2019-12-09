package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.dstu2.api.bundle.AbstractBundle;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.elements.Narrative;
import gov.va.api.health.dstu2.api.elements.Narrative.NarrativeStatus;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.dstu2.api.resources.OperationOutcome.Issue;
import gov.va.api.health.dstu2.api.resources.OperationOutcome.Issue.IssueSeverity;
import java.util.Collections;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import lombok.NoArgsConstructor;

/** This support utility provides the mechanism need to for Argonaut `$validate` endpoint. */
@NoArgsConstructor(staticName = "create")
public class Dstu2Validator {
  /**
   * Return a new "all ok" validation response. This is the payload that indicates the validated
   * bundle is valid.
   */
  public static OperationOutcome ok() {
    return OperationOutcome.builder()
        .resourceType("OperationOutcome")
        .id("allok")
        .text(
            Narrative.builder()
                .status(NarrativeStatus.additional)
                .div("<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>ALL OK</p></div>")
                .build())
        .issue(
            Collections.singletonList(
                Issue.builder()
                    .severity(IssueSeverity.information)
                    .code("informational")
                    .details(CodeableConcept.builder().text("ALL OK").build())
                    .build()))
        .build();
  }

  /**
   * Return an operation outcome if bundle is valid, otherwise throw a constraint violation
   * exception.
   */
  public <B extends AbstractBundle<?>> OperationOutcome validate(B bundle) {
    Set<ConstraintViolation<B>> violations =
        Validation.buildDefaultValidatorFactory().getValidator().validate(bundle);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException("Bundle is not valid", violations);
    }
    return ok();
  }
}
