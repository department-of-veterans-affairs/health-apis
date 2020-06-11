package gov.va.api.health.dataquery.service.controller.medicationrequest;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.asBigDecimal;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;

import com.google.common.collect.ImmutableMap;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.r4.api.DataAbsentReason;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Duration;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.datatypes.Timing;
import gov.va.api.health.r4.api.elements.Dosage;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.uscorer4.api.resources.MedicationRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Builder
@Slf4j
public class R4MedicationRequestFromMedicationOrderTransformer {
  // todo these need confirmation from KBS? Created backlog story API-1117
  static final ImmutableMap<String, MedicationRequest.Status> STATUS_VALUES =
      ImmutableMap.<String, MedicationRequest.Status>builder()
          .put("ACTIVE", MedicationRequest.Status.active)
          .put("DELETED", MedicationRequest.Status.entered_in_error)
          .put("DISCONTINUED (EDIT)", MedicationRequest.Status.stopped)
          .put("DISCONTINUED BY PROVIDER", MedicationRequest.Status.stopped)
          .put("DISCONTINUED", MedicationRequest.Status.stopped)
          .put("DRUG INTERACTIONS", MedicationRequest.Status.draft)
          .put("EXPIRED", MedicationRequest.Status.completed)
          .put("HOLD", MedicationRequest.Status.on_hold)
          .put("INCOMPLETE", MedicationRequest.Status.draft)
          .put("NEW ORDER", MedicationRequest.Status.draft)
          .put("NON-VERIFIED", MedicationRequest.Status.draft)
          .put("PENDING", MedicationRequest.Status.draft)
          .put("PROVIDER HOLD", MedicationRequest.Status.active)
          .put("REFILL REQUEST", MedicationRequest.Status.active)
          .put("RENEW", MedicationRequest.Status.active)
          .put("RENEWED", MedicationRequest.Status.active)
          .put("SUSPENDED", MedicationRequest.Status.active)
          .put("UNRELEASED", MedicationRequest.Status.draft)
          .put("active", MedicationRequest.Status.active)
          .put("discontinued", MedicationRequest.Status.stopped)
          .put("expired", MedicationRequest.Status.completed)
          .put("hold", MedicationRequest.Status.on_hold)
          .put("nonverified", MedicationRequest.Status.draft)
          .put("on call", MedicationRequest.Status.active)
          .put("renewed", MedicationRequest.Status.active)
          .put("CANCELLED", MedicationRequest.Status.entered_in_error)
          .put("DELAYED", MedicationRequest.Status.draft)
          .put("LAPSED", MedicationRequest.Status.entered_in_error)
          .put("COMPLETE", MedicationRequest.Status.completed)
          .put("DISCONTINUED/EDIT", MedicationRequest.Status.stopped)
          .put("completed", MedicationRequest.Status.completed)
          .put("draft", MedicationRequest.Status.draft)
          .put("entered-in-error", MedicationRequest.Status.entered_in_error)
          .put("on-hold", MedicationRequest.Status.on_hold)
          .put("stopped", MedicationRequest.Status.stopped)
          .build();

  @NonNull final DatamartMedicationOrder datamart;

  static CodeableConcept codeableConceptText(Optional<String> maybeText) {
    if (maybeText.isEmpty()) {
      return null;
    }
    return CodeableConcept.builder().text(maybeText.get()).build();
  }

  static MedicationRequest.DispenseRequest dispenseRequest(
      Optional<DatamartMedicationOrder.DispenseRequest> maybeDispenseRequest) {
    if (allBlank(maybeDispenseRequest)) {
      return null;
    }
    DatamartMedicationOrder.DispenseRequest dispenseRequest = maybeDispenseRequest.get();
    // todo: ignoring supply duration units for now?
    return MedicationRequest.DispenseRequest.builder()
        .numberOfRepeatsAllowed(dispenseRequest.numberOfRepeatsAllowed().orElse(0))
        .quantity(simpleQuantity(dispenseRequest.quantity(), dispenseRequest.unit()))
        .expectedSupplyDuration(duration(dispenseRequest.expectedSupplyDuration()))
        .build();
  }

  static List<Dosage> dosageInstruction(
      List<DatamartMedicationOrder.DosageInstruction> dosageInstructions,
      Instant dateWritten,
      Optional<Instant> dateEnded) {
    if (allBlank(dosageInstructions)) {
      return null;
    }
    List<Dosage> results = new ArrayList<>();
    for (DatamartMedicationOrder.DosageInstruction dosageInstruction : dosageInstructions) {
      results.add(
          Dosage.builder()
              .text(dosageInstruction.dosageText().orElse(null))
              .timing(timing(dosageInstruction.timingText(), dateWritten, dateEnded))
              .additionalInstruction(
                  dosageInstruction.additionalInstructions().isEmpty()
                      ? null
                      : List.of(codeableConceptText(dosageInstruction.additionalInstructions())))
              .asNeededBoolean(dosageInstruction.asNeeded())
              .route(codeableConceptText(dosageInstruction.routeText()))
              .doseAndRate(
                  doseAndRate(
                      dosageInstruction.doseQuantityValue(), dosageInstruction.doseQuantityUnit()))
              .build());
    }
    return results;
  }

  static List<Dosage.DoseAndRate> doseAndRate(Optional<Double> value, Optional<String> unit) {
    if (value.isPresent() || unit.isPresent()) {
      return List.of(
          Dosage.DoseAndRate.builder()
              .doseQuantity(
                  SimpleQuantity.builder()
                      .value(asBigDecimal(value))
                      .unit(unit.orElse(null))
                      .build())
              .build());
    } else {
      return null;
    }
  }

  static Duration duration(Optional<Integer> maybeSupplyDuration) {
    return Duration.builder()
        .value(asBigDecimal(maybeSupplyDuration))
        .unit("days")
        .code("d")
        .system("http://unitsofmeasure.org")
        .build();
  }

  static Extension requesterExtension(DatamartReference requester) {
    if (requester != null && requester.hasTypeAndReference()) {
      return null;
    }
    return DataAbsentReason.of(DataAbsentReason.Reason.unknown);
  }

  static SimpleQuantity simpleQuantity(Optional<Double> maybeValue, Optional<String> maybeUnit) {
    if (maybeValue.isPresent() || maybeUnit.isPresent()) {
      return SimpleQuantity.builder()
          .value(asBigDecimal(maybeValue))
          .unit(maybeUnit.orElse(null))
          .build();
    } else {
      return null;
    }
  }

  /** Convert from datamart.MedicationRequest.Status to MedicationRequest.Status */
  static MedicationRequest.Status status(String status) {
    if (allBlank(status)) {
      return null;
    }
    MedicationRequest.Status mapped = STATUS_VALUES.get(status.trim());
    if (mapped == null) {
      log.warn("Cannot map status value: {}", status);
    }
    return mapped;
  }

  static Timing timing(
      Optional<String> maybeTimingText, Instant dateWritten, Optional<Instant> dateEnded) {
    if (maybeTimingText.isEmpty()) {
      return null;
    }
    CodeableConcept maybeCcText = codeableConceptText(maybeTimingText);
    return Timing.builder()
        .code(maybeCcText)
        .repeat(
            Timing.Repeat.builder()
                .boundsPeriod(
                    Period.builder()
                        .start(asDateTimeString(dateWritten))
                        .end(asDateTimeString(dateEnded))
                        .build())
                .build())
        .build();
  }

  MedicationRequest toFhir() {
    return MedicationRequest.builder()
        .resourceType("MedicationRequest")
        .id(datamart.cdwId())
        .subject(asReference(datamart.patient()))
        .authoredOn(asDateTimeString(datamart.dateWritten()))
        .status(status(datamart.status()))
        .intent(MedicationRequest.Intent.order)
        .requester(asReference(datamart.prescriber()))
        ._requester(requesterExtension(datamart.prescriber()))
        .medicationReference(asReference(datamart.medication()))
        .dosageInstruction(
            dosageInstruction(
                datamart.dosageInstruction(), datamart.dateWritten(), datamart.dateEnded()))
        .dispenseRequest(dispenseRequest(datamart.dispenseRequest()))
        .build();
  }
}
