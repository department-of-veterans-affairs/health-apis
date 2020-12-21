package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.lighthouse.datamart.DatamartReference;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class DatamartDiagnosticReportTest {

  @SneakyThrows
  public void assertReadableV2(String json) {
    DatamartDiagnosticReport dmDr =
        createMapper()
            .readValue(getClass().getResourceAsStream(json), DatamartDiagnosticReport.class);
    assertThat(dmDr).isEqualTo(sampleV2());
  }

  private DatamartReference reference(String type, String reference, String display) {
    return DatamartReference.builder()
        .type(Optional.of(type))
        .reference(Optional.of(reference))
        .display(Optional.of(display))
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
        .verifyingStaff(Optional.of(reference("Practitioner", "000", "Big Boi")))
        .topography(Optional.of(reference("Observation", "777", "PLASMA")))
        .visit(Optional.of(reference("Encounter", "222", "Outpatient")))
        .orders(List.of(reference("DiagnosticOrder", "555", "RENAL PANEL")))
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
  public void unmarshalSampleV2() {
    assertReadableV2("datamart-diagnostic-report-v2.json");
  }
}
