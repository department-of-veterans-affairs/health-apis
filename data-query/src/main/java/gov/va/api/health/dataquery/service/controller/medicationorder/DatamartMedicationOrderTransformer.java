package gov.va.api.health.dataquery.service.controller.medicationorder;

import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.asReference;

import com.google.common.collect.ImmutableMap;
import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.argonaut.api.resources.MedicationOrder.Status;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.DataAbsentReason;
import gov.va.api.health.dstu2.api.DataAbsentReason.Reason;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Duration;
import gov.va.api.health.dstu2.api.datatypes.SimpleQuantity;
import gov.va.api.health.dstu2.api.datatypes.Timing;
import gov.va.api.health.dstu2.api.elements.Extension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Builder
@Slf4j
public class DatamartMedicationOrderTransformer {

  private static Map<String, Status> STATUS_VALUES =
      ImmutableMap.<String, Status>builder()
          /*
           * Values per KBS document VADP_Aggregate_190924.xls (2019 Sept 24)
           */
          // .put("DISCONTINUED (RENEWAL)",null) // Explicitly marked as <not-used> by KBS
          // .put("DONE",null) // Explicitly marked as <not-used> by KBS
          // .put("REFILL",null) // Explicitly marked as <not-used> by KBS
          // .put("REINSTATED",null) // Explicitly marked as <not-used> by KBS
          // .put("purge",null) // Explicitly marked as <not-used> by KBS
          .put("ACTIVE", MedicationOrder.Status.active)
          .put("DELETED", MedicationOrder.Status.entered_in_error)
          .put("DISCONTINUED (EDIT)", MedicationOrder.Status.stopped)
          .put("DISCONTINUED BY PROVIDER", MedicationOrder.Status.stopped)
          .put("DISCONTINUED", MedicationOrder.Status.stopped)
          .put("DRUG INTERACTIONS", MedicationOrder.Status.draft)
          .put("EXPIRED", MedicationOrder.Status.completed)
          .put("HOLD", MedicationOrder.Status.on_hold)
          .put("INCOMPLETE", MedicationOrder.Status.draft)
          .put("NEW ORDER", MedicationOrder.Status.draft)
          .put("NON-VERIFIED", MedicationOrder.Status.draft)
          .put("PENDING", MedicationOrder.Status.draft)
          .put("PROVIDER HOLD", MedicationOrder.Status.active)
          .put("REFILL REQUEST", MedicationOrder.Status.active)
          .put("RENEW", MedicationOrder.Status.active)
          .put("RENEWED", MedicationOrder.Status.active)
          .put("SUSPENDED", MedicationOrder.Status.active)
          .put("UNRELEASED", MedicationOrder.Status.draft)
          .put("active", MedicationOrder.Status.active)
          .put("discontinued", MedicationOrder.Status.stopped)
          .put("expired", MedicationOrder.Status.completed)
          .put("hold", MedicationOrder.Status.on_hold)
          .put("nonverified", MedicationOrder.Status.draft)
          .put("on call", MedicationOrder.Status.active)
          .put("renewed", MedicationOrder.Status.active)
          /*
           * Values via KBS team as of 09/26/2019. See ADQ-296.
           */
          .put("CANCELLED", MedicationOrder.Status.entered_in_error)
          .put("DELAYED", MedicationOrder.Status.draft)
          .put("LAPSED", MedicationOrder.Status.entered_in_error)
          /*
           * Values provided by James Harris based on CDW queries not in the list provided by KBS
           */
          .put("COMPLETE", MedicationOrder.Status.completed)
          .put("DISCONTINUED/EDIT", MedicationOrder.Status.stopped)
          /* FHIR values */
          // .put("active", MedicationOrder.Status.active) // Duplicated in KBS
          .put("completed", MedicationOrder.Status.completed)
          .put("draft", MedicationOrder.Status.draft)
          .put("entered-in-error", MedicationOrder.Status.entered_in_error)
          .put("on-hold", MedicationOrder.Status.on_hold)
          .put("stopped", MedicationOrder.Status.stopped)
          .build();

  @NonNull private final DatamartMedicationOrder datamart;

  private CodeableConcept codeableConceptText(Optional<String> maybeText) {
    if (maybeText.isPresent()) {
      return CodeableConcept.builder().text(maybeText.get()).build();
    }
    return null;
  }

  /** Convert datamart.MedicationOrder.DispenseRequest to a FHIR MedicationOrder.DispenseRequest */
  MedicationOrder.DispenseRequest dispenseRequest(
      Optional<DatamartMedicationOrder.DispenseRequest> maybeDispenseRequest) {
    if (!maybeDispenseRequest.isPresent()) {
      return null;
    }
    DatamartMedicationOrder.DispenseRequest dispenseRequest = maybeDispenseRequest.get();
    Integer numberOfRepeatsAllowed = dispenseRequest.numberOfRepeatsAllowed().orElse(0);
    return MedicationOrder.DispenseRequest.builder()
        .numberOfRepeatsAllowed(numberOfRepeatsAllowed < 1 ? null : numberOfRepeatsAllowed)
        .quantity(simpleQuantity(dispenseRequest.quantity(), dispenseRequest.unit()))
        .expectedSupplyDuration(duration(dispenseRequest.expectedSupplyDuration()))
        .build();
  }

  /**
   * Convert datamart.MedicationOrder.DosageInstruction to a FHIR MedicationOrder.DosageIntruction
   */
  List<MedicationOrder.DosageInstruction> dosageInstructions(
      List<DatamartMedicationOrder.DosageInstruction> dosageInstructions) {
    if (dosageInstructions.isEmpty()) {
      return null;
    }
    List<MedicationOrder.DosageInstruction> results = new ArrayList<>();
    for (DatamartMedicationOrder.DosageInstruction dosageInstruction : dosageInstructions) {
      results.add(
          MedicationOrder.DosageInstruction.builder()
              .text(dosageInstruction.dosageText().orElse(null))
              .timing(timing(dosageInstruction.timingText()))
              .additionalInstructions(
                  codeableConceptText(dosageInstruction.additionalInstructions()))
              .asNeededBoolean(dosageInstruction.asNeeded())
              .route(codeableConceptText(dosageInstruction.routeText()))
              .doseQuantity(
                  simpleQuantity(
                      dosageInstruction.doseQuantityValue(), dosageInstruction.doseQuantityUnit()))
              .build());
    }
    return results;
  }

  private Duration duration(Optional<Integer> maybeValue) {
    if (maybeValue.isPresent()) {
      return Duration.builder()
          .value(Double.valueOf(maybeValue.get()))
          .unit("days")
          .system("http://unitsofmeasure.org")
          .code("d")
          .build();
    }
    return null;
  }

  Extension prescriberExtension(DatamartReference prescriber) {
    if (prescriber != null && prescriber.hasTypeAndReference()) {
      return null;
    }
    return DataAbsentReason.of(Reason.unknown);
  }

  private SimpleQuantity simpleQuantity(Optional<Double> maybeValue, Optional<String> maybeUnit) {
    if (maybeValue.isPresent() || maybeUnit.isPresent()) {
      return SimpleQuantity.builder()
          .value(maybeValue.orElse(null))
          .unit(maybeUnit.orElse(null))
          .build();
    }
    return null;
  }

  /** Convert from datamart.MedicationOrder.Status to MedicationOrder.Status */
  MedicationOrder.Status status(String status) {
    if (status == null) {
      return null;
    }
    Status mapped = STATUS_VALUES.get(status.trim());
    if (mapped == null) {
      log.warn("Cannot map status value: {}", status);
    }
    return mapped;
  }

  private Timing timing(Optional<String> maybeTimingText) {
    CodeableConcept maybeCcText = codeableConceptText(maybeTimingText);
    if (maybeCcText != null) {
      return Timing.builder().code(maybeCcText).build();
    }
    return null;
  }

  /** Convert from datamart to FHIR compliant resource. */
  public MedicationOrder toFhir() {
    return MedicationOrder.builder()
        .resourceType("MedicationOrder")
        .id(datamart.cdwId())
        .patient(asReference(datamart.patient()))
        .dateWritten(asDateTimeString(datamart.dateWritten()))
        .status(status(datamart.status()))
        .dateEnded(asDateTimeString(datamart.dateEnded()))
        .prescriber(asReference(datamart.prescriber()))
        ._prescriber(prescriberExtension(datamart.prescriber()))
        .medicationReference(asReference(datamart.medication()))
        .dosageInstruction(dosageInstructions(datamart.dosageInstruction()))
        .dispenseRequest(dispenseRequest(datamart.dispenseRequest()))
        .build();
  }
}
