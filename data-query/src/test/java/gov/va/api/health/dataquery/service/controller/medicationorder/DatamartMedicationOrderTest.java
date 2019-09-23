package gov.va.api.health.dataquery.service.controller.medicationorder;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import java.time.Instant;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Test;

public class DatamartMedicationOrderTest {

  public void assertReadable(String json, DatamartMedicationOrder expected)
      throws java.io.IOException {
    DatamartMedicationOrder dm =
        createMapper()
            .readValue(getClass().getResourceAsStream(json), DatamartMedicationOrder.class);
    assertThat(dm).isEqualTo(expected);
  }

  public DatamartMedicationOrder sample() {
    return DatamartMedicationOrder.builder()
        .cdwId("1400181354458:O")
        .patient(
            DatamartReference.of()
                .type("Patient")
                .reference("1012958529V624991")
                .display("VETERAN,FARM ACY")
                .build())
        .dateWritten(Instant.parse("2016-11-17T18:02:04Z"))
        .status("DISCONTINUED")
        .dateEnded(Optional.of(Instant.parse("2017-02-15T05:00:00Z")))
        .prescriber(
            DatamartReference.of()
                .type("Practitioner")
                .reference("1404497883")
                .display("HIPPOCRATES,OATH J")
                .build())
        .medication(
            DatamartReference.of()
                .type("Medication")
                .reference("1400021372")
                .display("RIVAROXABAN 15MG TAB")
                .build())
        .dosageInstruction(
            asList(
                DatamartMedicationOrder.DosageInstruction.builder()
                    .dosageText(
                        Optional.of(
                            "TAKE ONE TABLET BY MOUTH TWICE A DAY FOR 7 DAYS TO PREVENT BLOOD CLOTS"))
                    .timingText(Optional.of("BID"))
                    .additionalInstructions(Optional.of("DO NOT TAKE NSAIDS WITH THIS MEDICATION"))
                    .asNeeded(false)
                    .routeText(Optional.of("ORAL"))
                    .doseQuantityValue(Optional.of(1.0))
                    .doseQuantityUnit(Optional.of("TAB"))
                    .build(),
                DatamartMedicationOrder.DosageInstruction.builder()
                    .dosageText(
                        Optional.of(
                            "THEN TAKE ONE TABLET BY MOUTH ONCE A DAY FOR 7 DAYS TO PREVENT BLOOD CLOTS"))
                    .timingText(Optional.of("QDAILY"))
                    .additionalInstructions(Optional.of("DO NOT TAKE NSAIDS WITH THIS MEDICATION"))
                    .asNeeded(false)
                    .routeText(Optional.of("ORAL"))
                    .doseQuantityValue(Optional.of(1.0))
                    .doseQuantityUnit(Optional.of("TAB"))
                    .build()))
        .dispenseRequest(
            Optional.of(
                DatamartMedicationOrder.DispenseRequest.builder()
                    .numberOfRepeatsAllowed(Optional.of(0))
                    .quantity(Optional.of(42.0))
                    .unit(Optional.of("TAB"))
                    .expectedSupplyDuration(Optional.of(21))
                    .supplyDurationUnits(Optional.of("days"))
                    .build()))
        .build();
  }

  @Test
  @SneakyThrows
  public void unmarshalSample() {
    assertReadable("datamart-medication-order.json", sample());
  }

  @Test
  @SneakyThrows
  public void unmarshalSampleV0() {
    assertReadable("datamart-medication-order-v0.json", sample().status("stopped"));
  }
}
