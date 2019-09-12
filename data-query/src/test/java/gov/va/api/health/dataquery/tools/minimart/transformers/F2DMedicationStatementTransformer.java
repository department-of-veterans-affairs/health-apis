package gov.va.api.health.dataquery.tools.minimart.transformers;

import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement.Dosage;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement.Status;
import gov.va.api.health.dataquery.tools.minimart.FhirToDatamartUtils;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
public class F2DMedicationStatementTransformer {

  FhirToDatamartUtils fauxIds;

  private Instant dateAsserted(String dateAsserted) {
    if (StringUtils.isBlank(dateAsserted)) {
      return null;
    }
    return Instant.parse(dateAsserted);
  }

  private Dosage dosage(List<MedicationStatement.Dosage> dosage) {
    if (dosage.isEmpty()) {
      return null;
    }
    return DatamartMedicationStatement.Dosage.builder()
        .text(Optional.ofNullable(dosage.get(0).text()))
        .timingCodeText(Optional.ofNullable(timingText(dosage.get(0))))
        .routeText(Optional.ofNullable(routeText(dosage.get(0))))
        .build();
  }

  private Optional<Instant> effectiveDateTime(String effectiveDateTime) {
    return Optional.ofNullable(Instant.parse(effectiveDateTime));
  }

  public DatamartMedicationStatement fhirToDatamart(MedicationStatement medicationStatement) {
    return DatamartMedicationStatement.builder()
        .cdwId(fauxIds.unmask("MedicationStatement", medicationStatement.id()))
        .patient(fauxIds.toDatamartReferenceWithCdwId(medicationStatement.patient()).get())
        .dateAsserted(dateAsserted(medicationStatement.dateAsserted()))
        .status(status(medicationStatement.status()))
        .effectiveDateTime(effectiveDateTime(medicationStatement.effectiveDateTime()))
        .note(note(medicationStatement.note()))
        .medication(
            fauxIds.toDatamartReferenceWithCdwId(medicationStatement.medicationReference()).get())
        .dosage(dosage(medicationStatement.dosage()))
        .build();
  }

  private Optional<String> note(String note) {
    return Optional.ofNullable(note);
  }

  private String routeText(MedicationStatement.Dosage dosage) {
    if (dosage.route() == null) {
      return null;
    }
    return dosage.route().text();
  }

  private Status status(MedicationStatement.Status status) {
    if (status == null) {
      return null;
    }
    return EnumSearcher.of(DatamartMedicationStatement.Status.class).find(status.toString());
  }

  private String timingText(MedicationStatement.Dosage dosage) {
    if (dosage.timing() == null || dosage.timing().code() == null) {
      return null;
    }
    return dosage.timing().code().text();
  }
}
