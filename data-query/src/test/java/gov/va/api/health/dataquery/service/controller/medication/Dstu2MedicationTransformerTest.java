package gov.va.api.health.dataquery.service.controller.medication;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.medication.MedicationSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.medication.MedicationSamples.Dstu2;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Test;

public class Dstu2MedicationTransformerTest {

  @Test
  public void bestCode() {
    DatamartMedication dm = MedicationSamples.Datamart.create().medication();
    Optional<DatamartMedication.RxNorm> rxnorm = dm.rxnorm();
    String localDrugName = dm.localDrugName();
    // rxnorm: yes, localDrugName: yes
    dm.rxnorm(rxnorm);
    dm.localDrugName(localDrugName);
    assertThat(tx(dm).bestCode()).isEqualTo(Dstu2.create().codeRxNorm());
    // rxnorm: yes, localDrugName: no
    dm.rxnorm(rxnorm);
    dm.localDrugName(null);
    assertThat(tx(dm).bestCode()).isEqualTo(Dstu2.create().codeRxNorm());
    // rxnorm: no, localDrugName: yes, product: yes
    dm.rxnorm(null);
    dm.localDrugName(localDrugName);
    assertThat(tx(dm).bestCode())
        .isEqualTo(Dstu2.create().codeLocalDrugNameWithProduct(localDrugName));
    // rxnorm: no, localDrugName: yes, product: no
    dm.rxnorm(null);
    dm.product(Optional.empty());
    dm.localDrugName(localDrugName);
    assertThat(tx(dm).bestCode()).isEqualTo(Dstu2.create().codeLocalDrugNameOnly(localDrugName));
    // rxnorm: no, localDrugName: no
    dm.rxnorm(null);
    dm.localDrugName(null);
    assertThat(tx(dm).bestCode()).isEqualTo(Dstu2.create().codeLocalDrugNameOnly("Unknown"));
  }

  @Test
  public void bestText() {
    DatamartMedication dm = MedicationSamples.Datamart.create().medication();
    Optional<DatamartMedication.RxNorm> rxnorm = dm.rxnorm();
    String localDrugName = dm.localDrugName();
    // rxnorm: yes, localDrugName: yes
    dm.rxnorm(rxnorm);
    dm.localDrugName(localDrugName);
    assertThat(tx(dm).bestText()).isEqualTo(Dstu2.create().textRxNorm());
    // rxnorm: yes, localDrugName: no
    dm.rxnorm(rxnorm);
    dm.localDrugName(null);
    assertThat(tx(dm).bestText()).isEqualTo(Dstu2.create().textRxNorm());
    // rxnorm: no, localDrugName: yes
    dm.rxnorm(null);
    dm.localDrugName(localDrugName);
    assertThat(tx(dm).bestText()).isEqualTo(Dstu2.create().textLocalDrugName());
    // rxnorm: no, localDrugName: no
    dm.rxnorm(null);
    dm.localDrugName(null);
    assertThat(tx(dm).bestText())
        .isEqualTo(Dstu2.create().textLocalDrugName().div("<div>Unknown</div>"));
  }

  @Test
  public void code() {
    Dstu2MedicationTransformer tx = tx(MedicationSamples.Datamart.create().medication());
    assertThat(tx.bestCode()).isEqualTo(Dstu2.create().codeRxNorm());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @Test
  public void medication() {
    assertThat(json(tx(Datamart.create().medication()).toFhir()))
        .isEqualTo(json(Dstu2.create().medication()));
  }

  @Test
  public void product() {
    Dstu2MedicationTransformer tx = tx(MedicationSamples.Datamart.create().medication());
    assertThat(tx.product(Optional.empty())).isNull();
    assertThat(tx.product(Datamart.create().medication().product()))
        .isEqualTo(Dstu2.create().product());
  }

  @Test
  public void text() {
    Dstu2MedicationTransformer tx = tx(MedicationSamples.Datamart.create().medication());
    assertThat(tx.bestText()).isEqualTo(Dstu2.create().textRxNorm());
  }

  Dstu2MedicationTransformer tx(DatamartMedication dm) {
    return Dstu2MedicationTransformer.builder().datamart(dm).build();
  }
}
