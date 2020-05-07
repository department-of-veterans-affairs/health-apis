package gov.va.api.health.dataquery.service.controller.condition;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateString;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
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
import lombok.NonNull;

@Builder
public class Dstu2ConditionTransformer {

  @NonNull private final DatamartCondition datamart;

  /**
   * Return snomed code if available, otherwise icd code if available. However, null will be
   * returned if neither are available.
   */
  CodeableConcept bestCode() {
    if (datamart.hasSnomedCode()) {
      return code(datamart.snomed().get());
    }
    if (datamart.hasIcdCode()) {
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
    if (snomedCode == null || allBlank(snomedCode.code(), snomedCode.display())) {
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
        .text(snomedCode.display())
        .build();
  }

  CodeableConcept code(IcdCode icdCode) {
    if (icdCode == null || allBlank(icdCode.code(), icdCode.display())) {
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
        .text(icdCode.display())
        .build();
  }

  private String systemOf(@NonNull IcdCode icdCode) {
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
        .asserter(asReference(datamart.asserter()))
        .category(category(datamart.category()))
        .id(datamart.cdwId())
        .clinicalStatus(clinicalStatusCode(datamart.clinicalStatus()))
        .code(bestCode())
        .dateRecorded(asDateString(datamart.dateRecorded()))
        .onsetDateTime(asDateTimeString(datamart.onsetDateTime()))
        .patient(asReference(datamart.patient()))
        .verificationStatus(VerificationStatusCode.unknown)
        .build();
  }
}
