package gov.va.api.health.dataquery.tools.minimart.transformers;


import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.argonaut.api.resources.Condition.ClinicalStatusCode;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.Category;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.ClinicalStatus;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.IcdCode;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.SnomedCode;
import gov.va.api.health.dataquery.tools.minimart.FhirToDatamartUtils;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

public class F2DConditionTransformer {

  FhirToDatamartUtils fauxIds;

  private Optional<Instant> abatementDateTime(String abatementDatetime) {
    if (abatementDatetime == null) {
      return null;
    }
    return Optional.of(Instant.parse(abatementDatetime));
  }

  private Category category(CodeableConcept category) {
    if (category == null || category.coding().isEmpty()) {
      return null;
    }
    return EnumSearcher.of(DatamartCondition.Category.class).find(category.coding().get(0).code());
  }

  private ClinicalStatus clinicalStatus(ClinicalStatusCode clinicalStatus) {
    if (clinicalStatus == null) {
      return null;
    }
    return EnumSearcher.of(DatamartCondition.ClinicalStatus.class).find(clinicalStatus.toString());
  }

  private Optional<LocalDate> dateTime(String dateRecorded) {
    if (dateRecorded == null) {
      return null;
    }
    return Optional.of(LocalDate.parse(dateRecorded));
  }

  public DatamartCondition fhirToDatamart(Condition condition) {
    return DatamartCondition.builder()
        .abatementDateTime(abatementDateTime(condition.abatementDateTime()))
        .asserter(fauxIds.toDatamartReferenceWithCdwId(condition.asserter()))
        .category(category(condition.category()))
        .cdwId(fauxIds.unmask("Condition", condition.id()))
        .clinicalStatus(clinicalStatus(condition.clinicalStatus()))
        .dateRecorded(dateTime(condition.dateRecorded()))
        .encounter(fauxIds.toDatamartReferenceWithCdwId(condition.encounter()))
        .onsetDateTime(onsetDateTime(condition.onsetDateTime()))
        .patient(fauxIds.toDatamartReferenceWithCdwId(condition.patient()).get())
        .snomed(maybeSnomed(condition.code()))
        .icd(maybeIcd(condition.code()))
        .build();
  }

  private Optional<IcdCode> maybeIcd(CodeableConcept code) {
    if (code == null) {
      return null;
    }
    return code.coding()
        .stream()
        .filter(coding -> whichCode(coding) == CodeSystem.icd)
        .map(
            coding ->
                IcdCode.builder()
                    .code(coding.code())
                    .display(coding.display())
                    .version(coding.version())
                    .build())
        .findFirst();
  }

  private Optional<SnomedCode> maybeSnomed(CodeableConcept code) {
    if (code == null) {
      return null;
    }
    return code.coding()
        .stream()
        .filter(coding -> whichCode(coding) == CodeSystem.snomed)
        .map(coding -> SnomedCode.builder().code(coding.code()).display(coding.display()).build())
        .findFirst();
  }

  private Optional<Instant> onsetDateTime(String onsetDateTime) {
    if (onsetDateTime == null) {
      return null;
    }
    return Optional.of(Instant.parse(onsetDateTime));
  }

  private CodeSystem whichCode(Coding coding) {
    if (coding.system().contains("icd")) {
      return CodeSystem.icd;
    }
    if (coding.system().contains("snomed")) {
      return CodeSystem.snomed;
    }
    return null;
  }

  private enum CodeSystem {
    snomed,
    icd
  }
}
