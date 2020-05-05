package gov.va.api.health.dataquery.service.controller.condition;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import lombok.Builder;
import gov.va.api.health.uscorer4.api.resources.Condition;

import java.util.List;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateString;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;

@Builder
public class R4ConditionTransformer {
    private final DatamartCondition datamart;

    private Meta meta() {
        return Meta.builder()
                .lastUpdated("Placeholder")
                .build();
    }

    CodeableConcept clinicalStatus(DatamartCondition.ClinicalStatus clinicalStatus) {
        if (clinicalStatus == null) {
            return null;
        }

        /* Must be one of:
         * active | recurrence | relapse | inactive | remission | resolved
         */
        switch (clinicalStatus) {
            case active:
                return CodeableConcept.builder()
                        .text("Active")
                        .coding(List.of(
                                Coding.builder()
                                        .code("active")
                                        .display("Active")
                                        .system("http://hl7.org/fhir/R4/codesystem-condition-clinical.html")
                                        .build()
                        ))
                        .build();
            case resolved:
                return CodeableConcept.builder()
                        .text("Resolved")
                        .coding(List.of(
                                Coding.builder()
                                        .code("resolved")
                                        .display("Resolved")
                                        .system("http://hl7.org/fhir/R4/codesystem-condition-clinical.html")
                                        .build()
                        ))
                        .build();
            default:
                return null;
        }
    }

    List<CodeableConcept> category(DatamartCondition.Category category) {
        if (category == null) {
            return null;
        }

        /*
         * Must be one of:
         * problem-list-item | encounter-diagnosis | health-concern
         */
        switch (category) {
            case diagnosis:
                return List.of(CodeableConcept.builder()
                        .text("Encounter Diagnosis")
                        .coding(
                                List.of(
                                        Coding.builder()
                                                .display("Encounter Diagnosis")
                                                .code("encounter-diagnosis")
                                                .system("http://hl7.org/fhir/R4/codesystem-condition-category.html")
                                                .build()))
                        .build());
            case problem:
                return List.of(CodeableConcept.builder()
                        .text("Problem List Item")
                        .coding(
                                List.of(
                                        Coding.builder()
                                                .display("Problem List Item")
                                                .code("problem-list-item")
                                                .system("http://hl7.org/fhir/R4/codesystem-condition-category.html")
                                                .build()))
                        .build());
            default:
                throw new IllegalArgumentException("Unknown category:" + category);
        }
    }

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

    CodeableConcept code(DatamartCondition.SnomedCode snomedCode) {
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
                .text(snomedCode.display())
                .build();
    }

    CodeableConcept code(DatamartCondition.IcdCode icdCode) {
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
                .text(icdCode.display())
                .build();
    }

    private String systemOf(DatamartCondition.IcdCode icdCode) {
        if ("10".equals(icdCode.version())) {
            return "http://hl7.org/fhir/sid/icd-10";
        }
        if ("9".equals(icdCode.version())) {
            return "http://hl7.org/fhir/sid/icd-9-cm";
        }
        throw new IllegalArgumentException("Unsupported ICD code version: " + icdCode.version());
    }

    /**
     * Convert the datamart structure to FHIR compliant structure.
     */
    public Condition toFhir() {
        return Condition.builder()
                .resourceType("Condition")
                .id(datamart.cdwId())
                .meta(meta())
                .subject(asReference(datamart.patient()))
                // Skip Encounter
                .asserter(asReference(datamart.asserter()))
                .recordedDate(asDateString(datamart.dateRecorded()))
                .code(bestCode())
                .category(category(datamart.category()))
                .clinicalStatus(clinicalStatus(datamart.clinicalStatus()))
                .onsetDateTime(asDateTimeString(datamart.onsetDateTime()))
                .abatementDateTime(asDateTimeString(datamart.abatementDateTime()))
                .build();
    }
}
