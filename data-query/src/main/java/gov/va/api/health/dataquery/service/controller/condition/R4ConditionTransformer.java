package gov.va.api.health.dataquery.service.controller.condition;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateString;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.uscorer4.api.resources.Condition;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class R4ConditionTransformer {
  private static final String PROBLEM_AND_DIAGNOSIS_SYSTEM =
      "http://terminology.hl7.org/CodeSystem/condition-category";
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

  List<CodeableConcept> category(DatamartCondition.Category category) {
    if (category == null) {
      return null;
    }
    switch (category) {
      case diagnosis:
        return List.of(
            CodeableConcept.builder()
                .text("Encounter Diagnosis")
                .coding(
                    List.of(
                        Coding.builder()
                            .display("Encounter Diagnosis")
                            .code("encounter-diagnosis")
                            .system(PROBLEM_AND_DIAGNOSIS_SYSTEM)
                            .build()))
                .build());
      case problem:
        return List.of(
            CodeableConcept.builder()
                .text("Problem List Item")
                .coding(
                    List.of(
                        Coding.builder()
                            .display("Problem List Item")
                            .code("problem-list-item")
                            .system(PROBLEM_AND_DIAGNOSIS_SYSTEM)
                            .build()))
                .build());
      default:
        throw new IllegalArgumentException("Unknown category: " + category);
    }
  }

  CodeableConcept clinicalStatus(DatamartCondition.ClinicalStatus clinicalStatus) {
    if (clinicalStatus == null) {
      return null;
    }
    switch (clinicalStatus) {
      case active:
        return CodeableConcept.builder()
            .text("Active")
            .coding(
                List.of(
                    Coding.builder()
                        .code("active")
                        .display("Active")
                        .system("http://hl7.org/fhir/R4/codesystem-condition-clinical.html")
                        .build()))
            .build();
      case resolved:
        return CodeableConcept.builder()
            .text("Resolved")
            .coding(
                List.of(
                    Coding.builder()
                        .code("resolved")
                        .display("Resolved")
                        .system("http://hl7.org/fhir/R4/codesystem-condition-clinical.html")
                        .build()))
            .build();
      default:
        throw new IllegalArgumentException("Unknown clinical-status: " + clinicalStatus);
    }
  }

  CodeableConcept code(DatamartCondition.SnomedCode snomedCode) {
    if (snomedCode == null || allBlank(snomedCode.code(), snomedCode.display())) {
      return null;
    }
    return CodeableConcept.builder()
        .text(snomedCode.display())
        .coding(
            List.of(
                Coding.builder()
                    .system("http://snomed.info/sct")
                    .code(snomedCode.code())
                    .display(snomedCode.display())
                    .build()))
        .text(snomedCode.display())
        .build();
  }

  CodeableConcept code(DatamartCondition.IcdCode icdCode) {
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

  private String systemOf(@NonNull DatamartCondition.IcdCode icdCode) {
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
        .id(datamart.cdwId())
        .subject(asReference(datamart.patient()))
        .asserter(asReference(datamart.asserter()))
        .recordedDate(asDateString(datamart.dateRecorded()))
        .code(bestCode())
        .category(category(datamart.category()))
        .clinicalStatus(clinicalStatus(datamart.clinicalStatus()))
        .onsetDateTime(asDateTimeString(datamart.onsetDateTime()))
        .abatementDateTime(asDateTimeString(datamart.abatementDateTime()))
        .verificationStatus(verificationStatus())
        .build();
  }

  CodeableConcept verificationStatus() {
    return CodeableConcept.builder()
        .text("Unconfirmed")
        .coding(
            List.of(
                Coding.builder()
                    .code("unconfirmed")
                    .system("http://hl7.org/fhir/R4/codesystem-condition-ver-status.html")
                    .display("Unconfirmed")
                    .build()))
        .build();
  }
}
