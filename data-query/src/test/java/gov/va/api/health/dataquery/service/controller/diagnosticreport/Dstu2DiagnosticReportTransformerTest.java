package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.dstu2.api.DataAbsentReason;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class Dstu2DiagnosticReportTransformerTest {
  @Test
  public void edtIsEmpty() {
    DatamartDiagnosticReports.DiagnosticReport dm =
        DatamartDiagnosticReports.DiagnosticReport.builder().effectiveDateTime("").build();
    assertThat(tx(dm).toFhir().effectiveDateTime()).isNull();
  }

  @Test
  public void edtIsUnparseable() {
    DatamartDiagnosticReports.DiagnosticReport dm =
        DatamartDiagnosticReports.DiagnosticReport.builder().effectiveDateTime("aDateTime").build();
    assertThat(tx(dm).toFhir().effectiveDateTime()).isNull();
  }

  @Test
  public void empty() {
    assertThat(tx(DatamartDiagnosticReports.DiagnosticReport.builder().build()).toFhir())
        .isEqualTo(
            DiagnosticReport.builder()
                .resourceType("DiagnosticReport")
                .status(DiagnosticReport.Code._final)
                .category(
                    CodeableConcept.builder()
                        .coding(
                            asList(
                                Coding.builder()
                                    .system(
                                        "http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
                                    .code("LAB")
                                    .display("Laboratory")
                                    .build()))
                        .build())
                .code(CodeableConcept.builder().text("panel").build())
                ._performer(DataAbsentReason.of(DataAbsentReason.Reason.unknown))
                .build());
  }

  @Test
  public void idtIsEmpty() {
    DatamartDiagnosticReports.DiagnosticReport dm =
        DatamartDiagnosticReports.DiagnosticReport.builder().issuedDateTime("").build();
    assertThat(tx(dm).toFhir().issued()).isNull();
  }

  @Test
  public void idtIsUnparseable() {
    DatamartDiagnosticReports.DiagnosticReport dm =
        DatamartDiagnosticReports.DiagnosticReport.builder().issuedDateTime("aDateTime").build();
    assertThat(tx(dm).toFhir().issued()).isNull();
  }

  @Test
  public void resultWithNullResult() {
    assertThat(Dstu2DiagnosticReportTransformer.result(null)).isNull();
  }

  @Test
  public void results() {
    assertThat(Dstu2DiagnosticReportTransformer.results(null)).isEqualTo(null);
    var expected =
        Arrays.asList(Reference.builder().reference("Observation/sample").display("test").build());
    var sample =
        Arrays.asList(
            DatamartDiagnosticReports.Result.builder().result("sample").display("test").build());
    assertThat(Dstu2DiagnosticReportTransformer.results(sample)).isEqualTo(expected);
    var emptySample = Arrays.asList(DatamartDiagnosticReports.Result.builder().build());
    assertThat(Dstu2DiagnosticReportTransformer.results(emptySample)).isEqualTo(null);
  }

  @Test
  public void subjectIsEmpty() {
    DiagnosticReport dm =
        Dstu2DiagnosticReportTransformer.builder()
            .icn("")
            .patientName("")
            .datamart(DatamartDiagnosticReports.DiagnosticReport.builder().build())
            .build()
            .toFhir();
    assertThat(dm.subject()).isNull();
  }

  private Dstu2DiagnosticReportTransformer tx(DatamartDiagnosticReports.DiagnosticReport dmDr) {
    return Dstu2DiagnosticReportTransformer.builder().datamart(dmDr).build();
  }
}
