package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.v1.DatamartDiagnosticReports;
import gov.va.api.health.dstu2.api.DataAbsentReason;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class Dstu2DiagnosticReportTransformerTest {

    @Test
    void _performer() {
        DiagnosticReport dr = tx(DatamartDiagnosticReport.builder().build()).toFhir();
        assertThat(dr.performer()).isNull();
        assertThat(dr._performer()).isEqualTo(DataAbsentReason.of(DataAbsentReason.Reason.unknown));
    }

    @Test
    void performer() {
        DiagnosticReport dr = tx(DatamartDiagnosticReport.builder()
                .accessionInstitution(
                        DiagnosticReportSamples.DatamartV2.create()
                                .accessionInstitution())
                .build())
                .toFhir();
        assertThat(dr._performer()).isNull();
        assertThat(dr.performer()).isEqualTo(DiagnosticReportSamples.Dstu2.create().performer());
    }

    @Test
    void diagnosticReport() {
        assertThat(tx(DiagnosticReportSamples.DatamartV2.create().diagnosticReport()).toFhir())
                .isEqualTo(DiagnosticReportSamples.Dstu2.create().report());
    }

    @Test
    void empty() {
        assertThat(tx(DatamartDiagnosticReport.builder().build()).toFhir())
                .isEqualTo(DiagnosticReport.builder()
                        .resourceType("DiagnosticReport")
                        .status(DiagnosticReport.Code._final)
                        .category(CodeableConcept.builder()
                                .coding(
                                        singletonList(
                                                Coding.builder()
                                                        .system("http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
                                                        .code("LAB")
                                                        .display("Laboratory")
                                                        .build()))
                                .build())
                        .code(CodeableConcept.builder().text("panel").build())
                        ._performer(DataAbsentReason.of(DataAbsentReason.Reason.unknown))
                        .build());
    }

    Dstu2DiagnosticReportTransformer tx(DatamartDiagnosticReport dm) {
        return Dstu2DiagnosticReportTransformer.builder().datamart(dm).build();
    }
}
