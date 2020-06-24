package gov.va.api.health.dataquery.service.controller.condition;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.condition.ConditionSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.condition.ConditionSamples.R4;
import gov.va.api.health.uscorer4.api.resources.Condition;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class R4ConditionTransformerTest {
  @Test
  public void bestCode() {
    Datamart datamart = Datamart.create();
    Optional<DatamartCondition.SnomedCode> snomed = Optional.of(datamart.snomedCode());
    Optional<DatamartCondition.IcdCode> icd = Optional.of(datamart.icd10Code());
    DatamartCondition condition = datamart.condition();
    R4 r4 = R4.create();
    // case: icd = no, snomed = no -> null
    condition.icd(null);
    condition.snomed(null);
    assertThat(tx(condition).bestCode()).isNull();
    // case: icd = no, snomed = yes -> snomed
    condition.icd(null);
    condition.snomed(snomed);
    assertThat(tx(condition).bestCode()).isEqualTo(r4.snomedCode());
    // case: icd = yes, snomed = no -> icd
    condition.icd(icd);
    condition.snomed(null);
    assertThat(tx(condition).bestCode()).isEqualTo(r4.icd10Code());
    // case: icd = yes, snomed = yes -> snomed
    condition.icd(icd);
    condition.snomed(snomed);
    assertThat(tx(condition).bestCode()).isEqualTo(r4.snomedCode());
    // case: icd = yes, snomed = yes, snomed.code = no -> icd
    condition.icd(icd);
    condition.snomed(Optional.of(datamart.snomedCode().code(null)));
    assertThat(tx(condition).bestCode()).isEqualTo(r4.icd10Code());
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
    R4ConditionTransformer tx = tx(ConditionSamples.Datamart.create().condition());
    R4 r4 = R4.create();
    assertThat(tx.category(null)).isNull();
    assertThat(tx.category(DatamartCondition.Category.diagnosis)).isEqualTo(r4.categoryDiagnosis());
    assertThat(tx.category(DatamartCondition.Category.problem)).isEqualTo(r4.categoryProblem());
  }

  @Test
  public void clinicalStatus() {
    R4ConditionTransformer tx = tx(ConditionSamples.Datamart.create().condition());
    R4 r4 = R4.create();
    assertThat(tx.clinicalStatus(null)).isNull();
    assertThat(tx.clinicalStatus(DatamartCondition.ClinicalStatus.active))
        .isEqualTo(r4.clinicalStatusActive());
    assertThat(tx.clinicalStatus(DatamartCondition.ClinicalStatus.resolved))
        .isEqualTo(r4.clinicalStatusResolved());
  }

  @Test
  public void code() {
    Datamart datamart = ConditionSamples.Datamart.create();
    R4 r4 = R4.create();
    assertThat(tx(datamart.condition()).code((DatamartCondition.SnomedCode) null)).isNull();
    assertThat(tx(datamart.condition()).code((DatamartCondition.IcdCode) null)).isNull();
    assertThat(tx(datamart.condition()).code(datamart.snomedCode())).isEqualTo(r4.snomedCode());
    assertThat(tx(datamart.condition()).code(datamart.icd9Code())).isEqualTo(r4.icd9Code());
    assertThat(tx(datamart.condition()).code(datamart.icd10Code())).isEqualTo(r4.icd10Code());
  }

  @Test
  public void condition() {
    assertThat(tx(Datamart.create().condition()).toFhir()).isEqualTo(R4.create().condition());
  }

  @Test
  public void empty() {
    assertThat(tx(DatamartCondition.builder().build()).toFhir())
        .isEqualTo(
            Condition.builder()
                .resourceType("Condition")
                .verificationStatus(ConditionSamples.R4.create().verificationStatus())
                .build());
  }

  R4ConditionTransformer tx(DatamartCondition dm) {
    return R4ConditionTransformer.builder().datamart(dm).build();
  }
}
