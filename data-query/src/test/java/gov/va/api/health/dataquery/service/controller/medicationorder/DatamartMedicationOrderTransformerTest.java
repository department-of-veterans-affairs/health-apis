package gov.va.api.health.dataquery.service.controller.medicationorder;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.argonaut.api.resources.MedicationOrder.Status;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.DataAbsentReason;
import gov.va.api.health.dstu2.api.DataAbsentReason.Reason;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Duration;
import gov.va.api.health.dstu2.api.datatypes.SimpleQuantity;
import gov.va.api.health.dstu2.api.datatypes.Timing;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.junit.Test;

public class DatamartMedicationOrderTransformerTest {

  @Test
  public void dispenseRequest() {
    DatamartMedicationOrderTransformer tx =
        DatamartMedicationOrderTransformer.builder()
            .datamart(Datamart.create().medicationOrder())
            .build();
    assertThat(tx.dispenseRequest(Optional.empty())).isNull();
    assertThat(tx.dispenseRequest(Optional.of(Datamart.create().dispenseRequest())))
        .isEqualTo(Fhir.create().dispenseRequest());
    assertThat(
            tx.dispenseRequest(
                Optional.of(
                    Datamart.create().dispenseRequest().numberOfRepeatsAllowed(Optional.of(0)))))
        .isEqualTo(Fhir.create().dispenseRequest().numberOfRepeatsAllowed(null));
  }

  @Test
  public void dosageInstruction() {
    DatamartMedicationOrderTransformer tx =
        DatamartMedicationOrderTransformer.builder()
            .datamart(Datamart.create().medicationOrder())
            .build();
    assertThat(tx.dosageInstructions(new ArrayList<>())).isNull();
    assertThat(tx.dosageInstructions(Datamart.create().dosageInstruction()))
        .isEqualTo(Fhir.create().dosageInstruction());
  }

  @Test
  public void medicationOrder() {
    assertThat(tx(Datamart.create().medicationOrder())).isEqualTo(Fhir.create().medicationOrder());
  }

  @Test
  public void prescriberExtension() {
    DatamartMedicationOrderTransformer tx =
        DatamartMedicationOrderTransformer.builder()
            .datamart(Datamart.create().medicationOrder())
            .build();
    assertThat(tx.prescriberExtension(Datamart.create().medicationOrderNoPrescriber().prescriber()))
        .isEqualTo(DataAbsentReason.of(Reason.unknown));
    assertThat(tx.prescriberExtension(Datamart.create().medicationOrder().prescriber()))
        .isEqualTo(null);
  }

  @Test
  public void status() {
    DatamartMedicationOrderTransformer tx =
        DatamartMedicationOrderTransformer.builder()
            .datamart(Datamart.create().medicationOrder())
            .build();
    /* Unknown values */
    assertThat(tx.status(null)).isNull();
    assertThat(tx.status("whatever")).isNull();
    assertThat(tx.status("")).isNull();
    /* VistA values */
    assertThat(tx.status("*Unknown at this time*")).isNull();
    assertThat(tx.status("*Missing*")).isNull();
    assertThat(tx.status("1234")).isNull();
    assertThat(tx.status("NULL")).isNull();

    /*
     * Values per KBS document VADP_Aggregate_190924.xls (2019 Sept 24)
     */
    assertThat(tx.status("ACTIVE")).isEqualTo(MedicationOrder.Status.active);
    assertThat(tx.status("DELETED")).isEqualTo(MedicationOrder.Status.entered_in_error);
    assertThat(tx.status("DISCONTINUED (EDIT)")).isEqualTo(MedicationOrder.Status.stopped);
    assertThat(tx.status("DISCONTINUED (RENEWAL)")).isNull();
    assertThat(tx.status("DISCONTINUED BY PROVIDER")).isEqualTo(MedicationOrder.Status.stopped);
    assertThat(tx.status("DISCONTINUED")).isEqualTo(MedicationOrder.Status.stopped);
    assertThat(tx.status("DONE")).isNull();
    assertThat(tx.status("DRUG INTERACTIONS")).isEqualTo(MedicationOrder.Status.draft);
    assertThat(tx.status("EXPIRED")).isEqualTo(MedicationOrder.Status.completed);
    assertThat(tx.status("HOLD")).isEqualTo(MedicationOrder.Status.on_hold);
    assertThat(tx.status("INCOMPLETE")).isEqualTo(MedicationOrder.Status.draft);
    assertThat(tx.status("NEW ORDER")).isEqualTo(MedicationOrder.Status.draft);
    assertThat(tx.status("NON-VERIFIED")).isEqualTo(MedicationOrder.Status.draft);
    assertThat(tx.status("PENDING")).isEqualTo(MedicationOrder.Status.draft);
    assertThat(tx.status("PROVIDER HOLD")).isEqualTo(MedicationOrder.Status.on_hold);
    assertThat(tx.status("REFILL REQUEST")).isEqualTo(MedicationOrder.Status.active);
    assertThat(tx.status("REFILL")).isNull();
    assertThat(tx.status("REINSTATED")).isNull();
    assertThat(tx.status("RENEW")).isEqualTo(MedicationOrder.Status.active);
    assertThat(tx.status("RENEWED")).isEqualTo(MedicationOrder.Status.active);
    assertThat(tx.status("SUSPENDED")).isEqualTo(MedicationOrder.Status.on_hold);
    assertThat(tx.status("UNRELEASED")).isEqualTo(MedicationOrder.Status.draft);
    assertThat(tx.status("active")).isEqualTo(MedicationOrder.Status.active);
    assertThat(tx.status("discontinued")).isEqualTo(MedicationOrder.Status.stopped);
    assertThat(tx.status("expired")).isEqualTo(MedicationOrder.Status.completed);
    assertThat(tx.status("hold")).isEqualTo(MedicationOrder.Status.on_hold);
    assertThat(tx.status("nonverified")).isEqualTo(MedicationOrder.Status.draft);
    assertThat(tx.status("on call")).isEqualTo(MedicationOrder.Status.active);
    assertThat(tx.status("purge")).isNull();
    assertThat(tx.status("renewed")).isEqualTo(MedicationOrder.Status.active);

    /*
     * Values via KBS team as of 09/26/2019. See ADQ-296.
     */
    assertThat(tx.status("DELAYED")).isEqualTo(MedicationOrder.Status.draft);
    assertThat(tx.status("CANCELLED")).isEqualTo(MedicationOrder.Status.entered_in_error);
    assertThat(tx.status("LAPSED")).isEqualTo(MedicationOrder.Status.entered_in_error);

    /*
     * Values provided by James Harris based on CDW queries not in the list provided by KBS
     */
    assertThat(tx.status("COMPLETE")).isEqualTo(MedicationOrder.Status.completed);
    assertThat(tx.status("DISCONTINUED/EDIT")).isEqualTo(MedicationOrder.Status.stopped);
    assertThat(tx.status("NON-VERIFIED")).isEqualTo(MedicationOrder.Status.draft);

    /* FHIR values */
    assertThat(tx.status("active")).isEqualTo(MedicationOrder.Status.active);
    assertThat(tx.status("completed")).isEqualTo(MedicationOrder.Status.completed);
    assertThat(tx.status("draft")).isEqualTo(MedicationOrder.Status.draft);
    assertThat(tx.status("entered-in-error")).isEqualTo(MedicationOrder.Status.entered_in_error);
    assertThat(tx.status("on-hold")).isEqualTo(MedicationOrder.Status.on_hold);
    assertThat(tx.status("stopped")).isEqualTo(MedicationOrder.Status.stopped);
  }

  MedicationOrder tx(DatamartMedicationOrder datamart) {
    return DatamartMedicationOrderTransformer.builder().datamart(datamart).build().toFhir();
  }

  @AllArgsConstructor(staticName = "create")
  static class Datamart {

    public DatamartMedicationOrder.DispenseRequest dispenseRequest() {
      return DatamartMedicationOrder.DispenseRequest.builder()
          .numberOfRepeatsAllowed(Optional.of(1))
          .quantity(Optional.of(42.0))
          .unit(Optional.of("TAB"))
          .expectedSupplyDuration(Optional.of(21))
          .supplyDurationUnits(Optional.of("days"))
          .build();
    }

    public List<DatamartMedicationOrder.DosageInstruction> dosageInstruction() {
      return asList(
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
              .build());
    }

    public DatamartMedicationOrder medicationOrder() {
      return DatamartMedicationOrder.builder()
          .cdwId("1400181354458:O")
          .patient(
              DatamartReference.of()
                  .type("Patient")
                  .reference("1012958529V624991")
                  .display("VETERAN,FARM ACY")
                  .build())
          .dateWritten(Instant.parse("2016-11-17T18:02:04Z"))
          .status("ACTIVE")
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
          .dosageInstruction(dosageInstruction())
          .dispenseRequest(Optional.of(dispenseRequest()))
          .build();
    }

    public DatamartMedicationOrder medicationOrderNoPrescriber() {
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
          .prescriber(null)
          .medication(
              DatamartReference.of()
                  .type("Medication")
                  .reference("1400021372")
                  .display("RIVAROXABAN 15MG TAB")
                  .build())
          .dosageInstruction(dosageInstruction())
          .dispenseRequest(Optional.of(dispenseRequest()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static class Fhir {

    public MedicationOrder.DispenseRequest dispenseRequest() {
      return MedicationOrder.DispenseRequest.builder()
          .numberOfRepeatsAllowed(1)
          .quantity(SimpleQuantity.builder().value(42.0).unit("TAB").build())
          .expectedSupplyDuration(
              Duration.builder()
                  .value((double) 21)
                  .unit("days")
                  .code("d")
                  .system("http://unitsofmeasure.org")
                  .build())
          .build();
    }

    public List<MedicationOrder.DosageInstruction> dosageInstruction() {
      return asList(
          MedicationOrder.DosageInstruction.builder()
              .text("TAKE ONE TABLET BY MOUTH TWICE A DAY FOR 7 DAYS TO PREVENT BLOOD CLOTS")
              .timing(Timing.builder().code(CodeableConcept.builder().text("BID").build()).build())
              .additionalInstructions(
                  CodeableConcept.builder().text("DO NOT TAKE NSAIDS WITH THIS MEDICATION").build())
              .asNeededBoolean(false)
              .route(CodeableConcept.builder().text("ORAL").build())
              .doseQuantity(SimpleQuantity.builder().value(1.0).unit("TAB").build())
              .build(),
          MedicationOrder.DosageInstruction.builder()
              .text("THEN TAKE ONE TABLET BY MOUTH ONCE A DAY FOR 7 DAYS TO PREVENT BLOOD CLOTS")
              .timing(
                  Timing.builder().code(CodeableConcept.builder().text("QDAILY").build()).build())
              .additionalInstructions(
                  CodeableConcept.builder().text("DO NOT TAKE NSAIDS WITH THIS MEDICATION").build())
              .asNeededBoolean(false)
              .route(CodeableConcept.builder().text("ORAL").build())
              .doseQuantity(SimpleQuantity.builder().value(1.0).unit("TAB").build())
              .build());
    }

    public MedicationOrder medicationOrder() {
      return MedicationOrder.builder()
          .resourceType("MedicationOrder")
          .id("1400181354458:O")
          .patient(
              Reference.builder()
                  .reference("Patient/1012958529V624991")
                  .display("VETERAN,FARM ACY")
                  .build())
          .dateWritten("2016-11-17T18:02:04Z")
          .status(Status.active)
          .dateEnded("2017-02-15T05:00:00Z")
          .prescriber(
              Reference.builder()
                  .reference("Practitioner/1404497883")
                  .display("HIPPOCRATES,OATH J")
                  .build())
          .medicationReference(
              Reference.builder()
                  .reference("Medication/1400021372")
                  .display("RIVAROXABAN 15MG TAB")
                  .build())
          .dosageInstruction(dosageInstruction())
          .dispenseRequest(dispenseRequest())
          .build();
    }
  }
}
