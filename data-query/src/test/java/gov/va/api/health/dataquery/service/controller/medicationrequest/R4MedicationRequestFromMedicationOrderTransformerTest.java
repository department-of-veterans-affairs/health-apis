package gov.va.api.health.dataquery.service.controller.medicationrequest;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderSamples;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class R4MedicationRequestFromMedicationOrderTransformerTest {
  @Test
  public void codeableConceptNullTest() {
    assertThat(
            R4MedicationRequestFromMedicationOrderTransformer.codeableConceptText(Optional.empty()))
        .isNull();
  }

  @Test
  public void dispenseReportNullTest() {
    // Null Dispense Request
    Optional<DatamartMedicationOrder.DispenseRequest> optionalDispenseRequest = Optional.empty();
    assertThat(
            R4MedicationRequestFromMedicationOrderTransformer.dispenseRequestConverter(
                optionalDispenseRequest))
        .isNull();
  }

  @Test
  public void dosageAndRateConverterNullTest() {
    assertThat(
            R4MedicationRequestFromMedicationOrderTransformer.doseAndRateConverter(
                Optional.empty(), Optional.empty()))
        .isNull();
  }

  @Test
  public void dosageInstructionTests() {
    // Null DosageInstruction
    Instant instant = Instant.parse("2016-11-17T18:02:04Z");
    Optional<Instant> emptyOptionalInstant = Optional.empty();
    Optional<Instant> end = Optional.ofNullable(Instant.parse("2017-02-15T05:00:00Z"));
    assertThat(
            R4MedicationRequestFromMedicationOrderTransformer.dosageInstructionConverter(
                null, instant, emptyOptionalInstant))
        .isNull();
    // Null additional information
    List<DatamartMedicationOrder.DosageInstruction> dosageInstruction =
        List.of(
            DatamartMedicationOrder.DosageInstruction.builder()
                .dosageText(
                    Optional.of(
                        "TAKE ONE TABLET BY MOUTH TWICE A DAY FOR 7 DAYS TO PREVENT BLOOD CLOTS"))
                .timingText(Optional.of("BID"))
                .asNeeded(false)
                .routeText(Optional.of("ORAL"))
                .doseQuantityValue(Optional.of(1.0))
                .doseQuantityUnit(Optional.of("TAB"))
                .build());
    assertThat(
            R4MedicationRequestFromMedicationOrderTransformer.dosageInstructionConverter(
                dosageInstruction, instant, end))
        .isEqualTo(
            MedicationRequestSamples.R4.dosageInstructionFromMedicationOrderNoAdditionalInfo());
  }

  @Test
  public void nullQuantityTest() {
    assertThat(
            R4MedicationRequestFromMedicationOrderTransformer.simpleQuantity(
                Optional.empty(), Optional.empty()))
        .isNull();
  }

  @Test
  public void nullTiming() {
    Instant instant = Instant.parse("2016-11-17T18:02:04Z");
    Optional<Instant> emptyOptionalInstant = Optional.empty();
    assertThat(
            R4MedicationRequestFromMedicationOrderTransformer.timing(
                Optional.empty(), instant, emptyOptionalInstant))
        .isNull();
  }

  @Test
  public void statusTests() {
    assertThat(R4MedicationRequestFromMedicationOrderTransformer.status(null)).isNull();
    assertThat(R4MedicationRequestFromMedicationOrderTransformer.status("GARBAGE")).isNull();
  }

  @Test
  public void toFhir() {
    DatamartMedicationOrder dmo = MedicationOrderSamples.Datamart.create().medicationOrder();
    assertThat(
            R4MedicationRequestFromMedicationOrderTransformer.builder()
                .datamart(dmo)
                .build()
                .toFhir())
        .isEqualTo(MedicationRequestSamples.R4.create().medicationRequestFromMedicationOrder());
  }
}
