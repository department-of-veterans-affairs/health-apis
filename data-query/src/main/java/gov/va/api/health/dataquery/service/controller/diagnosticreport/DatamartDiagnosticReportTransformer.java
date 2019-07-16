package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class DatamartDiagnosticReportTransformer {
  @NonNull final DatamartDiagnosticReports.DiagnosticReport datamart;

  final String icn;

  final String patientName;

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
    if (allBlank(datamart.accessionInstitutionSid(), datamart.accessionInstitutionName())) {
      return null;
    }
    return Reference.builder()
        .reference("Organization/" + datamart.accessionInstitutionSid())
        .display(datamart.accessionInstitutionName())
        .build();
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
        .build();
  }
}
