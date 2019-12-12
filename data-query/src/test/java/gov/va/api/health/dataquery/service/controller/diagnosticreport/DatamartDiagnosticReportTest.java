package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.SneakyThrows;
import org.junit.Test;

public class DatamartDiagnosticReportTest {

  public void assertReadable(String json) throws java.io.IOException {
    DatamartDiagnosticReports dmDr =
        createMapper()
            .readValue(getClass().getResourceAsStream(json), DatamartDiagnosticReports.class);
    assertThat(dmDr).isEqualTo(sample());
  }

  @Test
  public void emptyReports() {
    DatamartDiagnosticReports emptyReports = DatamartDiagnosticReports.builder().build();
    assertThat(emptyReports.reports()).isEmpty();
  }

  private DatamartDiagnosticReports sample() {
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

  @Test
  @SneakyThrows
  public void unmarshalSample() {
    assertReadable("datamart-diagnostic-report.json");
  }

  @Test
  @SneakyThrows
  public void unmarshalSampleV0() {
    assertReadable("datamart-diagnostic-report-v0.json");
  }
}
