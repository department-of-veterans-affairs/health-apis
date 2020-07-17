package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.v1.DatamartDiagnosticReports;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class DatamartDiagnosticReportTest {
  @SneakyThrows
  public void assertReadableV1(String json) {
    DatamartDiagnosticReports dmDr =
        createMapper()
            .readValue(getClass().getResourceAsStream(json), DatamartDiagnosticReports.class);
    assertThat(dmDr).isEqualTo(sampleV1());
  }

  @SneakyThrows
  public void assertReadableV2(String json) {
    DatamartDiagnosticReport dmDr =
        createMapper()
            .readValue(getClass().getResourceAsStream(json), DatamartDiagnosticReport.class);
    assertThat(dmDr).isEqualTo(sampleV2());
  }

  @Test
  public void emptyReports() {
    DatamartDiagnosticReports emptyReports = DatamartDiagnosticReports.builder().build();
    assertThat(emptyReports.reports()).isEmpty();
  }

  private DatamartReference reference(String type, String reference, String display) {
    return DatamartReference.builder()
        .type(Optional.of(type))
        .reference(Optional.of(reference))
        .display(Optional.of(display))
        .build();
  }

  private DatamartDiagnosticReports sampleV1() {
    DatamartDiagnosticReports.DiagnosticReport dr =
        DatamartDiagnosticReports.DiagnosticReport.builder()
            .identifier("111:L")
            .sta3n("111")
            .effectiveDateTime("2019-06-30T10:51:06Z")
            .issuedDateTime("2019-07-01T10:51:06Z")
            .accessionInstitutionSid("999")
            .accessionInstitutionName("ABC-DEF")
            .institutionSid("SURPRISE")
            .institutionName("SURPRISE")
            .verifyingStaffSid("SURPRISE")
            .verifyingStaffName("SURPRISE")
            .topographySid("777")
            .topographyName("PLASMA")
            .orders(
                asList(
                    DatamartDiagnosticReports.Order.builder()
                        .sid("555")
                        .display("RENAL PANEL")
                        .build()))
            .results(
                asList(
                    DatamartDiagnosticReports.Result.builder()
                        .result("111:L")
                        .display("ALBUMIN")
                        .build(),
                    DatamartDiagnosticReports.Result.builder()
                        .result("222:L")
                        .display("ALB/GLOB RATIO")
                        .build(),
                    DatamartDiagnosticReports.Result.builder()
                        .result("333:L")
                        .display("PROTEIN,TOTAL")
                        .build()))
            .build();
    return DatamartDiagnosticReports.builder()
        .objectType("DiagnosticReport")
        .objectVersion(1)
        .fullIcn("666V666")
        .patientName("VETERAN,HERNAM MINAM")
        .reports(asList(dr))
        .build();
  }

  private DatamartDiagnosticReport sampleV2() {
    return DatamartDiagnosticReport.builder()
        .cdwId("111:L")
        .patient(reference("Patient", "666V666", "VETERAN,HERNAM MINAM"))
        .sta3n("111")
        .effectiveDateTime("2019-06-30T10:51:06Z")
        .issuedDateTime("2019-07-01T10:51:06Z")
        .accessionInstitution(Optional.of(reference("Organization", "999", "ABC-DEF")))
        .verifyingStaff(Optional.of(reference("Practitioner", "SURPRISE", "SURPRISE")))
        .topography(Optional.of(reference("Location", "777", "PLASMA")))
        .visit(Optional.of(reference("Encounter", "SURPRISE", "SURPRISE")))
        .orders(List.of(reference("Observation", "555", "RENAL PANEL")))
        .results(
            List.of(
                reference("Observation", "111:L", "ALBUMIN"),
                reference("Observation", "222:L", "ALB/GLOB RATIO"),
                reference("Observation", "333:L", "PROTEIN,TOTAL")))
        .reportStatus("final")
        .build();
  }

  @Test
  @SneakyThrows
  public void unmarshalSample() {
    assertReadableV2("datamart-diagnostic-report.json");
  }

  @Test
  @SneakyThrows
  public void unmarshalSampleV0() {
    assertReadableV1("datamart-diagnostic-report-v0.json");
  }

  @Test
  @SneakyThrows
  public void unmarshalSampleV2() {
    assertReadableV2("datamart-diagnostic-report-v2.json");
  }
}
