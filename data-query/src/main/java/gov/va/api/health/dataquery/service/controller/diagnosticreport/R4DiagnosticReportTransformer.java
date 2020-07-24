package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;

import gov.va.api.health.dataquery.service.controller.R4Transformers;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.uscorer4.api.resources.DiagnosticReport;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class R4DiagnosticReportTransformer {
  @NonNull final DatamartDiagnosticReport datamart;

  String asFhirDateTimeString(String maybeDateTime) {
    return asDateTimeString(parseInstant(maybeDateTime));
  }

  /** Determine the catgory value(s) for the DiagnosticReport. */
  private List<CodeableConcept> category() {
    return List.of(
        CodeableConcept.builder()
            .coding(
                List.of(
                    Coding.builder()
                        .system("http://terminology.hl7.org/CodeSystem/v2-0074")
                        .code("LAB")
                        .display("Laboratory")
                        .build()))
            .build());
  }

  /** Determine the code value for the DiagnosticReport. */
  private CodeableConcept code() {
    return CodeableConcept.builder().text("panel").build();
  }

  /** Determine the results value(s) for the DiagnosticReport. */
  List<Reference> results(List<DatamartReference> maybeResults) {
    if (isBlank(maybeResults)) {
      return null;
    }
    return maybeResults.stream().map(R4Transformers::asReference).collect(Collectors.toList());
  }

  /*
   * Notes:
   *   - dm.visit() could be mapped to f.encounter()
   *   - dm.verifyingStaff() could be mapped to f.resultsInterpreter()
   *   - dm.accessionInstitution() could be mapped to f.performer()
   *   - dm.topography() seems to be the Observation that broadly describes the list
   *     of Observations in dm.results() is there somewhere we should map that?
   * */
  /** Generate the FHIR resource from the datamart payload. */
  DiagnosticReport toFhir() {
    return DiagnosticReport.builder()
        .id(datamart.cdwId())
        .resourceType("DiagnosticReport")
        .status(DiagnosticReport.DiagnosticReportStatus._final)
        .category(category())
        .code(code())
        .subject(asReference(datamart.patient()))
        .effectiveDateTime(asFhirDateTimeString(datamart.effectiveDateTime()))
        .issued(asFhirDateTimeString(datamart.issuedDateTime()))
        .result(results(datamart.results()))
        .build();
  }
}
