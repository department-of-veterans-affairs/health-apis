package gov.va.api.health.dataquery.service.controller.medicationorder;

import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.asReference;

import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Duration;
import gov.va.api.health.dstu2.api.datatypes.SimpleQuantity;
import gov.va.api.health.dstu2.api.datatypes.Timing;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class DatamartMedicationOrderTransformer {

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
    return MedicationOrder.DispenseRequest.builder()
        .numberOfRepeatsAllowed(dispenseRequest.numberOfRepeatsAllowed().orElse(null))
        .quantity(simpleQuantity(dispenseRequest.quantity(), dispenseRequest.unit()))
        .expectedSupplyDuration(
            duration(
                dispenseRequest.expectedSupplyDuration(), dispenseRequest.supplyDurationUnits()))
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

  private Duration duration(Optional<Integer> maybeValue, Optional<String> maybeUnit) {
    if (maybeValue.isPresent() && maybeUnit.isPresent()) {
      return Duration.builder()
          .value(Double.valueOf(maybeValue.get()))
          .unit(maybeUnit.get())
          .build();
    }
    return null;
  }

  private SimpleQuantity simpleQuantity(Optional<Double> maybeValue, Optional<String> maybeUnit) {
    if (maybeValue.isPresent() && maybeUnit.isPresent()) {
      return SimpleQuantity.builder().value(maybeValue.get()).unit(maybeUnit.get()).build();
    }
    return null;
  }

  /** Convert from datamart.MedicationOrder.Status to MedicationOrder.Status */
  MedicationOrder.Status status(DatamartMedicationOrder.Status status) {
    if (status == null) {
      return null;
    }
    switch (status) {
      case completed:
        return MedicationOrder.Status.completed;
      case stopped:
        return MedicationOrder.Status.stopped;
      case on_hold:
        return MedicationOrder.Status.on_hold;
      case active:
        return MedicationOrder.Status.active;
      case draft:
        return MedicationOrder.Status.draft;
      case entered_in_error:
        return MedicationOrder.Status.entered_in_error;
      default:
        throw new IllegalArgumentException("Unsupported Status: " + status);
    }
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
        .medicationReference(asReference(datamart.medication()))
        .dosageInstruction(dosageInstructions(datamart.dosageInstruction()))
        .dispenseRequest(dispenseRequest(datamart.dispenseRequest()))
        .build();
  }
}
