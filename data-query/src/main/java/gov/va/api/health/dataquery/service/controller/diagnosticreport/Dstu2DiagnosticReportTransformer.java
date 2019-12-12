package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.parseInstant;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.service.controller.Transformers;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReports.Result;
import gov.va.api.health.dstu2.api.DataAbsentReason;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class Dstu2DiagnosticReportTransformer {

  @NonNull final DatamartDiagnosticReports.DiagnosticReport datamart;

  final String icn;

  final String patientName;

  static Reference result(Result r) {
    if (r == null) {
      return null;
    }
    if (Transformers.isBlank(r.display()) || Transformers.isBlank(r.result())) {
      return null;
    }
    return Reference.builder().display(r.display()).reference("Observation/" + r.result()).build();
  }

  static List<Reference> results(List<Result> results) {
    if (isEmpty(results)) {
      return null;
    }
    var newResults = results.stream().map(r -> result(r)).collect(Collectors.toList());
    return emptyToNull(newResults);
  }

  private CodeableConcept category() {
    return CodeableConcept.builder()
        .coding(
            asList(
                Coding.builder()
                    .system("http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
                    .code("LAB")
                    .display("Laboratory")
                    .build()))
        .build();
  }

  private CodeableConcept code() {
    return CodeableConcept.builder().text("panel").build();
  }

  private String effectiveDateTime() {
    if (isBlank(datamart.effectiveDateTime())) {
      return null;
    }
    Instant instant = parseInstant(datamart.effectiveDateTime());
    if (instant == null) {
      return null;
    }
    return instant.toString();
  }

  private boolean isValidPerformer() {
    return !allBlank(datamart.accessionInstitutionSid(), datamart.accessionInstitutionName());
  }

  private String issued() {
    if (isBlank(datamart.issuedDateTime())) {
      return null;
    }
    Instant instant = parseInstant(datamart.issuedDateTime());
    if (instant == null) {
      return null;
    }
    return instant.toString();
  }

  private Reference performer() {
    if (!isValidPerformer()) {
      return null;
    }
    return Reference.builder()
        .reference("Organization/" + datamart.accessionInstitutionSid())
        .display(datamart.accessionInstitutionName())
        .build();
  }

  private Extension performerExtension() {
    if (isValidPerformer()) {
      return null;
    }
    return DataAbsentReason.of(DataAbsentReason.Reason.unknown);
  }

  private Reference subject() {
    if (allBlank(icn, patientName)) {
      return null;
    }
    return Reference.builder().reference("Patient/" + icn).display(patientName).build();
  }

  DiagnosticReport toFhir() {
    /*
     * While we have reference data for requests, specimens, and results, we do not
     * have full support for these resources and therefore must be omitted from this
     * resource.
     */
    return DiagnosticReport.builder()
        .id(datamart.identifier())
        .resourceType("DiagnosticReport")
        .status(DiagnosticReport.Code._final)
        .category(category())
        .code(code())
        .subject(subject())
        .effectiveDateTime(effectiveDateTime())
        .issued(issued())
        .performer(performer())
        ._performer(performerExtension())
        .result(results(datamart.results()))
        .build();
  }
}
