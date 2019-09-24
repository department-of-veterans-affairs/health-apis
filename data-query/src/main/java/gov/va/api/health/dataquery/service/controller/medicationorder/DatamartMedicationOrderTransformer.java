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
          /* FHIR Values */
          .put("active", Status.active)
          .put("on-hold", Status.on_hold)
          .put("completed", Status.completed)
          .put("entered-in-error", Status.entered_in_error)
          .put("stopped", Status.stopped)
          .put("draft", Status.draft)
          /* VistA Values */
          .put("ACTIVE", Status.active)
          .put("CANCELLED", Status.stopped)
          .put("COMPLETE", Status.completed)
          .put("DELAYED", Status.on_hold)
          .put("DELETED", Status.entered_in_error)
          .put("DISCONTINUED (EDIT)", Status.stopped)
          .put("DISCONTINUED BY PROVIDER", Status.stopped)
          .put("DISCONTINUED", Status.completed)
          .put("DISCONTINUED/EDIT", Status.stopped)
          .put("DRUG INTERACTIONS", Status.stopped)
          .put("EXPIRED", Status.completed)
          .put("HOLD", Status.on_hold)
          .put("LAPSED", Status.on_hold)
          .put("NON-VERIFIED", Status.draft)
          .put("PENDING", Status.draft)
          .put("PROVIDER HOLD", Status.on_hold)
          .put("RENEWED", Status.active)
          .put("SUSPENDED", Status.on_hold)
          .put("UNRELEASED", Status.draft)
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
