package gov.va.api.health.dataquery.service.controller.condition;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.IcdCode;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.SnomedCode;
import gov.va.api.health.dataquery.service.controller.condition.DatamartConditionSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.condition.DatamartConditionSamples.Fhir;
import java.util.Optional;
import org.junit.Test;

public class DatamartConditionTransformerTest {

  @Test
  public void bestCode() {
    Datamart datamart = DatamartConditionSamples.Datamart.create();
    Optional<SnomedCode> snomed = Optional.of(datamart.snomedCode());
    Optional<IcdCode> icd = Optional.of(datamart.icd10Code());
    DatamartCondition condition = datamart.condition();
    // case: icd = no, snomed = no -> null
    condition.icd(null);
    condition.snomed(null);
    assertThat(tx(condition).bestCode()).isNull();
    // case: icd = no, snomed = yes -> snomed
    condition.icd(null);
    condition.snomed(snomed);
    assertThat(tx(condition).bestCode())
        .isEqualTo(DatamartConditionSamples.Fhir.create().snomedCode());
    // case: icd = yes, snomed = no -> icd
    condition.icd(icd);
    condition.snomed(null);
    assertThat(tx(condition).bestCode())
        .isEqualTo(DatamartConditionSamples.Fhir.create().icd10Code());
    // case: icd = yes, snomed = yes -> snomed
    condition.icd(icd);
    condition.snomed(snomed);
    assertThat(tx(condition).bestCode())
        .isEqualTo(DatamartConditionSamples.Fhir.create().snomedCode());
    // case: icd = yes, snomed = yes, snomed.code = no -> icd
    condition.icd(icd);
    condition.snomed(Optional.of(datamart.snomedCode().code(null)));
    assertThat(tx(condition).bestCode())
        .isEqualTo(DatamartConditionSamples.Fhir.create().icd10Code());
    // case: icd = yes, icd.code = no, snomed = yes, snomed.display = no -> null
    condition.icd(Optional.of(datamart.icd10Code().code(null)));
    condition.snomed(Optional.of(datamart.snomedCode().display(null)));
    assertThat(tx(condition).bestCode()).isNull();
    // case: snomed = no, icd = yes, icd.display = no -> null
    condition.icd(Optional.of(datamart.icd10Code().display(null)));
    condition.snomed(null);
    assertThat(tx(condition).bestCode()).isNull();
  }

  @Test
  public void category() {
    Datamart datamart = DatamartConditionSamples.Datamart.create();
    Fhir fhir = DatamartConditionSamples.Fhir.create();
    assertThat(tx(datamart.condition()).category(null)).isNull();
    assertThat(tx(datamart.condition()).category(DatamartCondition.Category.diagnosis))
        .isEqualTo(fhir.diagnosisCategory());
    assertThat(tx(datamart.condition()).category(DatamartCondition.Category.problem))
        .isEqualTo(fhir.problemCategory());
  }

  @Test
  public void clinicalStatusCode() {
    DatamartConditionTransformer tx = tx(DatamartConditionSamples.Datamart.create().condition());
    assertThat(tx.clinicalStatusCode(null)).isNull();
    assertThat(tx.clinicalStatusCode(DatamartCondition.ClinicalStatus.active))
        .isEqualTo(Condition.ClinicalStatusCode.active);
    assertThat(tx.clinicalStatusCode(DatamartCondition.ClinicalStatus.resolved))
        .isEqualTo(Condition.ClinicalStatusCode.resolved);
  }

  @Test
  public void code() {
    Datamart datamart = DatamartConditionSamples.Datamart.create();
    Fhir fhir = DatamartConditionSamples.Fhir.create();
    assertThat(tx(datamart.condition()).code((SnomedCode) null)).isNull();
    assertThat(tx(datamart.condition()).code((IcdCode) null)).isNull();
    assertThat(tx(datamart.condition()).code(datamart.snomedCode())).isEqualTo(fhir.snomedCode());
    assertThat(tx(datamart.condition()).code(datamart.icd9Code())).isEqualTo(fhir.icd9Code());
    assertThat(tx(datamart.condition()).code(datamart.icd10Code())).isEqualTo(fhir.icd10Code());
  }

  @Test
  public void condition() {
    assertThat(tx(DatamartConditionSamples.Datamart.create().condition()).toFhir())
        .isEqualTo(DatamartConditionSamples.Fhir.create().condition());
  }

  DatamartConditionTransformer tx(DatamartCondition dm) {
    return DatamartConditionTransformer.builder().datamart(dm).build();
  }
}
