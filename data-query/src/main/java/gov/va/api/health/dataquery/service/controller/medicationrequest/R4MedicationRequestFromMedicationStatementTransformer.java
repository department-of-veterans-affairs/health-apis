package gov.va.api.health.dataquery.service.controller.medicationrequest;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;

import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.health.r4.api.DataAbsentReason;
import gov.va.api.health.r4.api.datatypes.Annotation;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.datatypes.Timing;
import gov.va.api.health.r4.api.elements.Dosage;
import gov.va.api.health.uscorer4.api.resources.MedicationRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class R4MedicationRequestFromMedicationStatementTransformer {
  @NonNull final DatamartMedicationStatement datamart;

  static List<Dosage> dosageInstructionConverter(
      Optional<Instant> effectiveDate, DatamartMedicationStatement.Dosage dosage) {
    if (dosage == null
        || allBlank(dosage.text(), dosage.timingCodeText(), dosage.routeText(), effectiveDate)) {
      return null;
    }
    return List.of(
        Dosage.builder()
            .text(dosage.text().orElse(null))
            .timing(
                Timing.builder()
                    .code(
                        CodeableConcept.builder()
                            .text(dosage.timingCodeText().orElse(null))
                            .build())
                    .repeat(
                        Timing.Repeat.builder()
                            .boundsPeriod(
                                Period.builder()
                                    .start(asDateTimeString(effectiveDate.orElse(null)))
                                    .build())
                            .build())
                    .build())
            .route(CodeableConcept.builder().text(dosage.routeText().orElse(null)).build())
            .build());
  }

  static List<Annotation> note(Optional<String> note) {
    return note.isPresent() ? List.of(Annotation.builder().text(note.get()).build()) : null;
  }

  static MedicationRequest.Status status(DatamartMedicationStatement.Status status) {
    if (allBlank(status)) {
      return null;
    }
    switch (status) {
      case active:
        return MedicationRequest.Status.active;
      case completed:
        return MedicationRequest.Status.completed;
      default:
        throw new IllegalArgumentException("Cannot convert: " + status);
    }
  }

  // TODO future needs requester field filled once the
  //  DatamartMedicationStatement is updated in the future
  MedicationRequest toFhir() {
    return MedicationRequest.builder()
        .resourceType("MedicationRequest")
        .id(datamart.cdwId())
        .subject(asReference(datamart.patient()))
        .authoredOn(asDateTimeString(datamart.dateAsserted()))
        .status(status(datamart.status()))
        .note(note(datamart.note()))
        .medicationReference(asReference(datamart.medication()))
        .dosageInstruction(
            dosageInstructionConverter(datamart.effectiveDateTime(), datamart.dosage()))
        .reportedBoolean(true)
        .intent(MedicationRequest.Intent.plan)
        ._requester(DataAbsentReason.of(DataAbsentReason.Reason.unknown))
        .build();
  }
}
