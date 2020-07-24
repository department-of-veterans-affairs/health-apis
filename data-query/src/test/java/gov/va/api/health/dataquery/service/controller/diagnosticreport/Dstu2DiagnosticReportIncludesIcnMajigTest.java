package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import java.util.List;

import org.junit.jupiter.api.Test;

public class Dstu2DiagnosticReportIncludesIcnMajigTest {
  @Test
  void dstu2() {
    ExtractIcnValidator.builder()
        .majig(new Dstu2DiagnosticReportIncludesIcnMajig())
        .body(
                gov.va.api.health.argonaut.api.resources.DiagnosticReport.builder()
                .id("123")
                .subject(gov.va.api.health.dstu2.api.elements.Reference.builder().reference("Patient/1010101010V666666").build())
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }

  @Test
  void r4() {
    ExtractIcnValidator.builder()
            .majig(new R4DiagnosticReportIncludesIcnMajig())
            .body(gov.va.api.health.uscorer4.api.resources.DiagnosticReport.builder()
                    .id("123")
                    .subject(gov.va.api.health.r4.api.elements.Reference.builder().reference("Patient/1010101010V666666").build())
                    .build())
            .expectedIcns(List.of("1010101010V666666"))
            .build()
            .assertIcn();
  }
}
