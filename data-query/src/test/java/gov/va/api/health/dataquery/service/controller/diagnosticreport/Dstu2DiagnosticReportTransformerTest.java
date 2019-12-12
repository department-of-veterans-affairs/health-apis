package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.Arrays;
import org.junit.Test;

public class Dstu2DiagnosticReportTransformerTest {

  @Test
  public void edtIsEmpty() {
    DatamartDiagnosticReports.DiagnosticReport dm =
        DatamartDiagnosticReports.DiagnosticReport.builder().effectiveDateTime("").build();
    assertThat(tx(dm).effectiveDateTime()).isNull();
  }

  @Test
  public void edtIsUnparseable() {
    DatamartDiagnosticReports.DiagnosticReport dm =
        DatamartDiagnosticReports.DiagnosticReport.builder().effectiveDateTime("aDateTime").build();
    assertThat(tx(dm).effectiveDateTime()).isNull();
  }

  @Test
  public void idtIsEmpty() {
    DatamartDiagnosticReports.DiagnosticReport dm =
        DatamartDiagnosticReports.DiagnosticReport.builder().issuedDateTime("").build();
    assertThat(tx(dm).issued()).isNull();
  }

  @Test
  public void idtIsUnparseable() {
    DatamartDiagnosticReports.DiagnosticReport dm =
        DatamartDiagnosticReports.DiagnosticReport.builder().issuedDateTime("aDateTime").build();
    assertThat(tx(dm).issued()).isNull();
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

  private DiagnosticReport tx(DatamartDiagnosticReports.DiagnosticReport dmDr) {
    return Dstu2DiagnosticReportTransformer.builder().datamart(dmDr).build().toFhir();
  }
}
