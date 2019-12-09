package gov.va.api.health.dataquery.service.controller.condition;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.dataquery.service.controller.condition.ConditionSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.condition.ConditionSamples.Dstu2;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.IcdCode;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.SnomedCode;
import java.util.Optional;
import org.junit.Test;

public class Dstu2ConditionTransformerTest {

  @Test
  public void bestCode() {
    Datamart datamart = ConditionSamples.Datamart.create();
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
    assertThat(tx(condition).bestCode()).isEqualTo(Dstu2.create().snomedCode());
    // case: icd = yes, snomed = no -> icd
    condition.icd(icd);
    condition.snomed(null);
    assertThat(tx(condition).bestCode()).isEqualTo(Dstu2.create().icd10Code());
    // case: icd = yes, snomed = yes -> snomed
    condition.icd(icd);
    condition.snomed(snomed);
    assertThat(tx(condition).bestCode()).isEqualTo(Dstu2.create().snomedCode());
    // case: icd = yes, snomed = yes, snomed.code = no -> icd
    condition.icd(icd);
    condition.snomed(Optional.of(datamart.snomedCode().code(null)));
    assertThat(tx(condition).bestCode()).isEqualTo(Dstu2.create().icd10Code());
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
    Datamart datamart = ConditionSamples.Datamart.create();
    Dstu2 dstu2 = Dstu2.create();
    assertThat(tx(datamart.condition()).category(null)).isNull();
    assertThat(tx(datamart.condition()).category(DatamartCondition.Category.diagnosis))
        .isEqualTo(dstu2.diagnosisCategory());
    assertThat(tx(datamart.condition()).category(DatamartCondition.Category.problem))
        .isEqualTo(dstu2.problemCategory());
  }

  @Test
  public void clinicalStatusCode() {
    Dstu2ConditionTransformer tx = tx(ConditionSamples.Datamart.create().condition());
    assertThat(tx.clinicalStatusCode(null)).isNull();
    assertThat(tx.clinicalStatusCode(DatamartCondition.ClinicalStatus.active))
        .isEqualTo(Condition.ClinicalStatusCode.active);
    assertThat(tx.clinicalStatusCode(DatamartCondition.ClinicalStatus.resolved))
        .isEqualTo(Condition.ClinicalStatusCode.resolved);
  }

  @Test
  public void code() {
    Datamart datamart = ConditionSamples.Datamart.create();
    Dstu2 dstu2 = Dstu2.create();
    assertThat(tx(datamart.condition()).code((SnomedCode) null)).isNull();
    assertThat(tx(datamart.condition()).code((IcdCode) null)).isNull();
    assertThat(tx(datamart.condition()).code(datamart.snomedCode())).isEqualTo(dstu2.snomedCode());
    assertThat(tx(datamart.condition()).code(datamart.icd9Code())).isEqualTo(dstu2.icd9Code());
    assertThat(tx(datamart.condition()).code(datamart.icd10Code())).isEqualTo(dstu2.icd10Code());
  }

  @Test
  public void condition() {
    assertThat(tx(ConditionSamples.Datamart.create().condition()).toFhir())
        .isEqualTo(Dstu2.create().condition());
  }

  Dstu2ConditionTransformer tx(DatamartCondition dm) {
    return Dstu2ConditionTransformer.builder().datamart(dm).build();
  }
}
