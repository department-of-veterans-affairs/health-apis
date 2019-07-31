package gov.va.api.health.dataquery.service.controller.condition;

import static gov.va.api.health.dataquery.service.controller.Transformers.asDateString;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.ifPresent;

import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.argonaut.api.resources.Condition.VerificationStatusCode;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.Category;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.IcdCode;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.SnomedCode;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import java.util.List;
import lombok.Builder;

@Builder
public class DatamartConditionTransformer {

  private final DatamartCondition datamart;

  /**
   * Return snomed code if available, otherwise icd code if available. However, null will be
   * returned if neither are available.
   */
  CodeableConcept bestCode() {
    if (datamart.snomed().isPresent()) {
      return code(datamart.snomed().get());
    }
    if (datamart.icd().isPresent()) {
      return code(datamart.icd().get());
    }
    return null;
  }

  CodeableConcept category(Category category) {
    if (category == null) {
      return null;
    }
    switch (category) {
      case diagnosis:
        return CodeableConcept.builder()
            .text("Diagnosis")
            .coding(
                List.of(
                    Coding.builder()
                        .display("Diagnosis")
                        .code("diagnosis")
                        .system("http://hl7.org/fhir/condition-category")
                        .build()))
            .build();
      case problem:
        return CodeableConcept.builder()
            .text("Problem")
            .coding(
                List.of(
                    Coding.builder()
                        .display("Problem")
                        .code("problem")
                        .system("http://argonaut.hl7.org")
                        .build()))
            .build();
      default:
        throw new IllegalArgumentException("Unknown category:" + category);
    }
  }

  Condition.ClinicalStatusCode clinicalStatusCode(DatamartCondition.ClinicalStatus source) {
    return ifPresent(
        source,
        status -> EnumSearcher.of(Condition.ClinicalStatusCode.class).find(status.toString()));
  }

  CodeableConcept code(SnomedCode snomedCode) {
    if (snomedCode == null) {
      return null;
    }
    return CodeableConcept.builder()
        .text(snomedCode.display())
        .coding(
            List.of(
                Coding.builder()
                    .system("https://snomed.info/sct")
                    .code(snomedCode.code())
                    .display(snomedCode.display())
                    .build()))
        .build();
  }

  CodeableConcept code(IcdCode icdCode) {
    if (icdCode == null) {
      return null;
    }
    return CodeableConcept.builder()
        .text(icdCode.display())
        .coding(
            List.of(
                Coding.builder()
                    .system(systemOf(icdCode))
                    .code(icdCode.code())
                    .display(icdCode.display())
                    .build()))
        .build();
  }

  private String systemOf(IcdCode icdCode) {
    if ("10".equals(icdCode.version())) {
      return "http://hl7.org/fhir/sid/icd-10";
    }
    if ("9".equals(icdCode.version())) {
      return "http://hl7.org/fhir/sid/icd-9-cm";
    }
    throw new IllegalArgumentException("Unsupported ICD code version: " + icdCode.version());
  }

  /** Convert the datamart structure to FHIR compliant structure. */
  public Condition toFhir() {
    return Condition.builder()
        .resourceType("Condition")
        .abatementDateTime(asDateTimeString(datamart.abatementDateTime()))
        .asserter(asReference(datamart.asserter().orElse(null)))
        .category(category(datamart.category()))
        .id(datamart.cdwId())
        .clinicalStatus(clinicalStatusCode(datamart.clinicalStatus()))
        .code(bestCode())
        .dateRecorded(asDateString(datamart.dateRecorded()))
        .encounter(asReference(datamart.encounter().orElse(null)))
        .onsetDateTime(asDateTimeString(datamart.onsetDateTime()))
        .patient(asReference(datamart.patient()))
        .verificationStatus(VerificationStatusCode.unknown)
        .build();
  }
}
