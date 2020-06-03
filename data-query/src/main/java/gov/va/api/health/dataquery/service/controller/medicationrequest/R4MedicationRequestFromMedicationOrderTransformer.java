package gov.va.api.health.dataquery.service.controller.medicationrequest;

import com.google.common.collect.ImmutableMap;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.uscorer4.api.resources.MedicationRequest;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;

@Builder
@Slf4j
public class R4MedicationRequestFromMedicationOrderTransformer {

    @NonNull
    final DatamartMedicationOrder datamart;

    // todo these need confirmation from KBS? how do we do this?
    private static Map<String, MedicationRequest.Status> STATUS_VALUES =
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

      MedicationRequest toFhir() {
          return MedicationRequest.builder()
                  .resourceType("MedicationRequest")
                  .id(datamart.cdwId())
                  .subject(asReference(datamart.patient()))
                  .authoredOn(asDateTimeString(datamart.dateWritten())) // todo need to use date written again for dosage stuff
                  .status(status(datamart.status()))
                  // todo dosageInstruction.timing.repeat.boundsPeriod.start = dateWritten
                  // todo dosageInstruction.timing.repeat.boundsPeriod.end = dateended
                  .requester(asReference(datamart.prescriber()))
                  .note(note(datamart.note())) // missing note?
                  .medicationReference(asReference(datamart.medication()))
                  // todo format dosage instruction information
                  .dosageInstruction(datamart.dosageInstruction())
                  // todo format dispense request
                  .dispenseRequest(datamart.dispenseRequest())
                  






                  .build();
      }
}
