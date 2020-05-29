package gov.va.api.health.dataquery.service.controller.procedure;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asCodeableConceptWrapping;
import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.uscorer4.api.resources.Procedure;
import gov.va.api.health.uscorer4.api.resources.Procedure.Status;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class R4ProcedureTransformer {
  @NonNull private final DatamartProcedure datamart;

  static Status status(DatamartProcedure.Status dmStatus) {
    if (dmStatus == null) {
      return null;
    }
    /*
    TODO:
     These mappings are our initial guess. We are waiting on feedback from the terminology team
     for the official mappings.
     */
    switch (dmStatus) {
      case in_progress:
        return Status.in_progress;
      case aborted:
        return Status.stopped;
      case cancelled:
        return Status.not_done;
      case completed:
        return Status.completed;
      default:
        throw new IllegalArgumentException("Unsupported status: " + dmStatus);
    }
  }

  static CodeableConcept statusReason(Optional<String> reasonNotPerformed) {
    if (reasonNotPerformed.isEmpty()) {
      return null;
    }
    return CodeableConcept.builder().text(reasonNotPerformed.get()).build();
  }

  Procedure toFhir() {
    return Procedure.builder()
        .resourceType("Procedure")
        .id(datamart.cdwId())
        .subject(asReference(datamart.patient()))
        .status(status(datamart.status()))
        .code(asCodeableConceptWrapping(datamart.coding()))
        .statusReason(statusReason(datamart.reasonNotPerformed()))
        .performedDateTime(asDateTimeString(datamart.performedDateTime()))
        .location(asReference(datamart.location()))
        .build();
  }
}
