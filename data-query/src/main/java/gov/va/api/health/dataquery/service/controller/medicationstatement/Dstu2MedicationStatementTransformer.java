package gov.va.api.health.dataquery.service.controller.medicationstatement;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asReference;

import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Timing;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class Dstu2MedicationStatementTransformer {

  @NonNull final DatamartMedicationStatement datamart;

  CodeableConcept codeableConcept(Optional<String> text) {
    if (text.isEmpty()) {
      return null;
    }
    return CodeableConcept.builder().text(text.get()).build();
  }

  List<MedicationStatement.Dosage> dosage(DatamartMedicationStatement.Dosage source) {
    if (source == null) {
      return null;
    }
    return List.of(
        MedicationStatement.Dosage.builder()
            .route(codeableConcept(source.routeText()))
            .text(source.text().orElse(null))
            .timing(timing(source.timingCodeText()))
            .build());
  }

  MedicationStatement.Status status(DatamartMedicationStatement.Status status) {
    if (status == null) {
      return null;
    }
    switch (status) {
      case active:
        return MedicationStatement.Status.active;
      case completed:
        return MedicationStatement.Status.completed;
      default:
        throw new IllegalArgumentException("Cannot convert: " + status);
    }
  }

  Timing timing(Optional<String> timingCodeText) {
    if (timingCodeText.isEmpty()) {
      return null;
    }
    return Timing.builder().code(codeableConcept(timingCodeText)).build();
  }

  MedicationStatement toFhir() {
    return MedicationStatement.builder()
        .resourceType("MedicationStatement")
        .id(datamart.cdwId())
        .dateAsserted(asDateTimeString(datamart.dateAsserted()))
        .dosage(dosage(datamart.dosage()))
        .effectiveDateTime(asDateTimeString(datamart.effectiveDateTime()))
        .medicationReference(asReference(datamart.medication()))
        .note(datamart.note().orElse(null))
        .patient(asReference(datamart.patient()))
        .status(status(datamart.status()))
        .build();
  }
}
