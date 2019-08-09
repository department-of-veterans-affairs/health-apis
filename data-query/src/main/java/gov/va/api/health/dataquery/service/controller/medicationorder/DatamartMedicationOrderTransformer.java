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

  private CodeableConcept codeableConceptText(String text) {
    return CodeableConcept.builder().text(text).build();
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
        .quantity(
            SimpleQuantity.builder()
                .value(dispenseRequest.quantity().orElse(null))
                .unit(dispenseRequest.unit().orElse(null))
                .build())
        .expectedSupplyDuration(
            Duration.builder()
                .value((double) dispenseRequest.expectedSupplyDuration().orElse(null))
                .unit(dispenseRequest.supplyDurationUnits().orElse(null))
                .build())
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
              .timing(
                  Timing.builder()
                      .code(codeableConceptText(dosageInstruction.timingText().orElse(null)))
                      .build())
              .additionalInstructions(
                  codeableConceptText(dosageInstruction.additionalInstructions().orElse(null)))
              .asNeededBoolean(dosageInstruction.asNeeded())
              .route(codeableConceptText(dosageInstruction.routeText().orElse(null)))
              .doseQuantity(
                  SimpleQuantity.builder()
                      .value(dosageInstruction.doseQuantityValue().orElse(null))
                      .unit(dosageInstruction.doseQuantityUnit().orElse(null))
                      .build())
              .build());
    }
    return results;
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
