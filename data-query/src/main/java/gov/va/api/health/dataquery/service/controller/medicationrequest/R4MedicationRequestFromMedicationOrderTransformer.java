package gov.va.api.health.dataquery.service.controller.medicationrequest;

import com.google.common.collect.ImmutableMap;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.health.r4.api.DataAbsentReason;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Duration;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.datatypes.Timing;
import gov.va.api.health.r4.api.elements.Dosage;
import gov.va.api.health.uscorer4.api.resources.MedicationRequest;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;

@Builder
@Slf4j
public class R4MedicationRequestFromMedicationOrderTransformer {

    @NonNull
    final DatamartMedicationOrder datamart;

    // todo these need confirmation from KBS? how do we do this?
    private static final Map<String, MedicationRequest.Status> STATUS_VALUES =
        ImmutableMap.<String, MedicationRequest.Status>builder()
            /*
             * Values per KBS document VADP_Aggregate_190924.xls (2019 Sept 24)
             */
            // .put("DISCONTINUED (RENEWAL)",null) // Explicitly marked as <not-used> by KBS
            // .put("DONE",null) // Explicitly marked as <not-used> by KBS
            // .put("REFILL",null) // Explicitly marked as <not-used> by KBS
            // .put("REINSTATED",null) // Explicitly marked as <not-used> by KBS
            // .put("purge",null) // Explicitly marked as <not-used> by KBS
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
            /*
             * Values via KBS team as of 09/26/2019. See ADQ-296.
             */
            .put("CANCELLED", MedicationRequest.Status.entered_in_error)
            .put("DELAYED", MedicationRequest.Status.draft)
            .put("LAPSED", MedicationRequest.Status.entered_in_error)
            /*
             * Values provided by James Harris based on CDW queries not in the list provided by KBS
             */
            .put("COMPLETE", MedicationRequest.Status.completed)
            .put("DISCONTINUED/EDIT", MedicationRequest.Status.stopped)
            /* FHIR values */
            // .put("active", MedicationRequest.Status.active) // Duplicated in KBS
            .put("completed", MedicationRequest.Status.completed)
            .put("draft", MedicationRequest.Status.draft)
            .put("entered-in-error", MedicationRequest.Status.entered_in_error)
            .put("on-hold", MedicationRequest.Status.on_hold)
            .put("stopped", MedicationRequest.Status.stopped)
            .build();

    /** Convert from datamart.MedicationRequest.Status to MedicationRequest.Status */
    MedicationRequest.Status status(String status) {
        if (status == null) {
            return null;
        }
        MedicationRequest.Status mapped = STATUS_VALUES.get(status.trim());
        if (mapped == null) {
            log.warn("Cannot map status value: {}", status);
        }
        return mapped;
    }

    private CodeableConcept codeableConceptText(Optional<String> maybeText) {
        if (maybeText.isEmpty()) {
            return null;
        }

        return CodeableConcept.builder().text(maybeText.get()).build();
    }

    private Timing timing(Optional<String> maybeTimingText,
                                 Instant dateWritten,
                                 Optional<Instant> dateEnded) {

        if (maybeTimingText.isEmpty()) {
            return null;
        }

        CodeableConcept maybeCcText = codeableConceptText(maybeTimingText);

        return Timing.builder()
                .code(maybeCcText)
                .repeat(Timing.Repeat.builder()
                        .boundsPeriod(
                                Period.builder()
                                    .start(asDateTimeString(dateWritten))
                                    .end(asDateTimeString(dateEnded))
                                    .build())
                        .build())
                .build();
    }

    private List<Dosage.DoseAndRate> doseAndRateConverter(Optional<Double> value, Optional<String> unit) {
        if (value.isPresent() || unit.isPresent()) {
            return List.of(
                    Dosage.DoseAndRate.builder()
                    .doseQuantity(
                            SimpleQuantity.builder()
                                    .value(value.isEmpty() ? null : BigDecimal.valueOf(value.get()))
                                    .unit(unit.orElse(null))
                                    .build())
                    .build());
        } else {
            return null;
        }
    }

    private List<Dosage> dosageInstructionConverter(
            List<DatamartMedicationOrder.DosageInstruction> dosageInstructions,
            Instant dateWritten,
            Optional<Instant> dateEnded) {

        if (dosageInstructions == null) {
            return null;
        }

        List<Dosage> results = new ArrayList<>();

        for (DatamartMedicationOrder.DosageInstruction dosageInstruction : dosageInstructions) {

            results.add(
                    Dosage.builder()
                            .text(dosageInstruction.dosageText().orElse(null))
                            .timing(timing(dosageInstruction.timingText(), dateWritten, dateEnded))
                            .additionalInstruction(dosageInstruction.additionalInstructions().isEmpty() ? null :
                                    List.of(codeableConceptText(dosageInstruction.additionalInstructions())))
                            .asNeededBoolean(dosageInstruction.asNeeded())
                            .route(codeableConceptText(dosageInstruction.routeText()))
                            .doseAndRate(doseAndRateConverter(
                                            dosageInstruction.doseQuantityValue(),
                                            dosageInstruction.doseQuantityUnit()))
                            .build());
        }
        return results;
    }

    private SimpleQuantity simpleQuantity(Optional<Double> maybeValue, Optional<String> maybeUnit) {

        if (maybeValue.isPresent() || maybeUnit.isPresent()) {
            return SimpleQuantity.builder()
                    .value(maybeValue.isEmpty() ? null : BigDecimal.valueOf(maybeValue.get()))
                    .unit(maybeUnit.orElse(null))
                    .build();
        } else {
            return null;
        }
    }

    private Duration durationConverter(Optional<Integer> maybeSupplyDuration) {
        return Duration.builder()
                .value(maybeSupplyDuration.isEmpty() ? null : BigDecimal.valueOf(maybeSupplyDuration.get()))
                .unit("days")
                .code("d")
                .system("http://unitsofmeasure.org")
                .build();
    }

    private MedicationRequest.DispenseRequest dispenseRequestConverter(
            Optional<DatamartMedicationOrder.DispenseRequest> maybeDispenseRequest) {

        if (maybeDispenseRequest.isEmpty()) {
            return null;
        }

        DatamartMedicationOrder.DispenseRequest dispenseRequest = maybeDispenseRequest.get();

        // todo: ignoring supply duration units for now?
        return MedicationRequest.DispenseRequest.builder()
                .numberOfRepeatsAllowed(dispenseRequest.numberOfRepeatsAllowed().orElse(0))
                .quantity(simpleQuantity(dispenseRequest.quantity(), dispenseRequest.unit()))
                .expectedSupplyDuration(durationConverter(dispenseRequest.expectedSupplyDuration()))
                .build();
    }

      MedicationRequest toFhir() {
          return MedicationRequest.builder()
                  .resourceType("MedicationRequest")
                  .id(datamart.cdwId())
                  .subject(asReference(datamart.patient()))
                  .authoredOn(asDateTimeString(datamart.dateWritten()))
                  .status(status(datamart.status()))
                  .requester(asReference(datamart.prescriber()))
                  ._requester(DataAbsentReason.of(DataAbsentReason.Reason.unknown))
                  .medicationReference(asReference(datamart.medication()))
                  .dosageInstruction(
                          dosageInstructionConverter(
                                  datamart.dosageInstruction(),
                                  datamart.dateWritten(),
                                  datamart.dateEnded()))
                  .dispenseRequest(dispenseRequestConverter(datamart.dispenseRequest()))
                  .build();
      }
}
