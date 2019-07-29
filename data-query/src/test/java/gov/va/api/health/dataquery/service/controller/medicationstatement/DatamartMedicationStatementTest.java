package gov.va.api.health.dataquery.service.controller.medicationstatement;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import java.time.Instant;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Test;

public class DatamartMedicationStatementTest {
  public DatamartMedicationStatement sample() {
    return DatamartMedicationStatement.builder()
        .objectType("MedicationStatement")
        .objectVersion("1")
        .cdwId("800008482786")
        .etlDate(Instant.parse("2017-11-03T01:39:21Z"))
        .patient(
            DatamartReference.of()
                .type("Patient")
                .reference("1004810366V403573")
                .display("BARKER,BOBBIE LEE")
                .build())
        .dateAsserted(Instant.parse("2017-11-03T01:39:21Z"))
        .status(DatamartMedicationStatement.Status.completed)
        .effectiveDateTime(Optional.of(Instant.parse("2017-11-03T01:39:21Z")))
        .note(Optional.empty())
        .medication(
            DatamartReference.of()
                .type("Medication")
                .reference(null)
                .display("SAW PALMETTO")
                .build())
        .dosage(
            DatamartMedicationStatement.Dosage.builder()
                .text(Optional.of("1"))
                .timingCodeText(Optional.of("EVERYDAY"))
                .routeText(Optional.of("MOUTH"))
                .build())
        .build();
  }

  @Test
  @SneakyThrows
  public void unmarshalSample() {
    DatamartMedicationStatement ms =
        createMapper()
            .readValue(
                getClass().getResourceAsStream("datamart-medication-statement.json"),
                DatamartMedicationStatement.class);
    assertThat(ms).isEqualTo(sample());
  }
}
