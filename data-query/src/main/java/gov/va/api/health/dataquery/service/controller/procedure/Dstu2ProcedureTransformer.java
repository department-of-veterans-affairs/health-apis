package gov.va.api.health.dataquery.service.controller.procedure;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asCodeableConceptWrapping;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asReference;

import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.argonaut.api.resources.Procedure.Status;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import java.util.List;
import java.util.Optional;
import lombok.Builder;

@Builder
public class Dstu2ProcedureTransformer {
  private final DatamartProcedure datamart;

  List<CodeableConcept> reasonNotPerformed(Optional<String> reasonNotPerformed) {
    if (reasonNotPerformed.isEmpty()) {
      return null;
    }
    return List.of(CodeableConcept.builder().text(reasonNotPerformed.get()).build());
  }

  Status status(DatamartProcedure.Status status) {
    if (status == null) {
      return null;
    }
    if (status == DatamartProcedure.Status.cancelled) {
      return Status.entered_in_error;
    }
    return EnumSearcher.of(Procedure.Status.class).find(status.toString());
  }

  /** Convert the datamart structure to FHIR compliant structure. */
  public Procedure toFhir() {
    return Procedure.builder()
        .resourceType("Procedure")
        .id(datamart.cdwId())
        .subject(asReference(datamart.patient()))
        .status(status(datamart.status()))
        .code(asCodeableConceptWrapping(datamart.coding()))
        .notPerformed(datamart.notPerformed())
        .reasonNotPerformed(reasonNotPerformed(datamart.reasonNotPerformed()))
        .performedDateTime(asDateTimeString(datamart.performedDateTime()))
        .location(asReference(datamart.location()))
        .build();
  }
}
