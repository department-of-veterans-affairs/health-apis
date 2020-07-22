package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.uscorer4.api.resources.DiagnosticReport;
import org.junit.jupiter.api.Test;

public class R4DiagnosticReportTransformerTest {
  @Test
  void diagnosticReport() {
    assertThat(tx(DiagnosticReportSamples.DatamartV2.create().diagnosticReport()).toFhir())
        .isEqualTo(DiagnosticReportSamples.R4.create().diagnosticReport());
  }

  @Test
  void empty() {
    assertThat(tx(DatamartDiagnosticReport.builder().build()).toFhir())
        .isEqualTo(
            DiagnosticReport.builder()
                .resourceType("DiagnosticReport")
                .status(DiagnosticReport.DiagnosticReportStatus._final)
                .category(
                    singletonList(
                        CodeableConcept.builder()
                            .coding(
                                singletonList(
                                    Coding.builder()
                                        .system("http://terminology.hl7.org/CodeSystem/v2-0074")
                                        .code("LAB")
                                        .display("Laboratory")
                                        .build()))
                            .build()))
                .code(CodeableConcept.builder().text("panel").build())
                .build());
  }

  @Test
  void results() {
    assertThat(tx(DatamartDiagnosticReport.builder().build()).results(emptyList())).isNull();
    assertThat(
            tx(DatamartDiagnosticReport.builder().build())
                .results(DiagnosticReportSamples.DatamartV2.create().results()))
        .isEqualTo(DiagnosticReportSamples.R4.create().results());
  }

  R4DiagnosticReportTransformer tx(DatamartDiagnosticReport dm) {
    return R4DiagnosticReportTransformer.builder().datamart(dm).build();
  }
}
