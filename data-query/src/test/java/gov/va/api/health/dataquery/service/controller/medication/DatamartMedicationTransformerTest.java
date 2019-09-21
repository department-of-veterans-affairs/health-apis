package gov.va.api.health.dataquery.service.controller.medication;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedicationSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedicationSamples.Fhir;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Test;

public class DatamartMedicationTransformerTest {

  @Test
  public void bestCode() {
    DatamartMedication dm = DatamartMedicationSamples.Datamart.create().medication();
    Optional<DatamartMedication.RxNorm> rxnorm = dm.rxnorm();
    String localDrugName = dm.localDrugName();
    // rxnorm: yes, localDrugName: yes
    dm.rxnorm(rxnorm);
    dm.localDrugName(localDrugName);
    assertThat(tx(dm).bestCode()).isEqualTo(DatamartMedicationSamples.Fhir.create().codeRxNorm());
    // rxnorm: yes, localDrugName: no
    dm.rxnorm(rxnorm);
    dm.localDrugName(null);
    assertThat(tx(dm).bestCode()).isEqualTo(DatamartMedicationSamples.Fhir.create().codeRxNorm());
    // rxnorm: no, localDrugName: yes
    dm.rxnorm(null);
    dm.localDrugName(localDrugName);
    assertThat(tx(dm).bestCode())
        .isEqualTo(DatamartMedicationSamples.Fhir.create().codeLocalDrugName());
    // rxnorm: no, localDrugName: no
    dm.rxnorm(null);
    dm.localDrugName(null);
    assertThat(tx(dm).bestCode())
        .isEqualTo(DatamartMedicationSamples.Fhir.create().codeLocalDrugName("Unknown"));
  }

  @Test
  public void bestText() {
    DatamartMedication dm = DatamartMedicationSamples.Datamart.create().medication();
    Optional<DatamartMedication.RxNorm> rxnorm = dm.rxnorm();
    String localDrugName = dm.localDrugName();
    // rxnorm: yes, localDrugName: yes
    dm.rxnorm(rxnorm);
    dm.localDrugName(localDrugName);
    assertThat(tx(dm).bestText()).isEqualTo(DatamartMedicationSamples.Fhir.create().textRxNorm());
    // rxnorm: yes, localDrugName: no
    dm.rxnorm(rxnorm);
    dm.localDrugName(null);
    assertThat(tx(dm).bestText()).isEqualTo(DatamartMedicationSamples.Fhir.create().textRxNorm());
    // rxnorm: no, localDrugName: yes
    dm.rxnorm(null);
    dm.localDrugName(localDrugName);
    assertThat(tx(dm).bestText())
        .isEqualTo(DatamartMedicationSamples.Fhir.create().textLocalDrugName());
    // rxnorm: no, localDrugName: no
    dm.rxnorm(null);
    dm.localDrugName(null);
    assertThat(tx(dm).bestText())
        .isEqualTo(
            DatamartMedicationSamples.Fhir.create().textLocalDrugName().div("<div>Unknown</div>"));
  }

  @Test
  public void code() {
    DatamartMedicationTransformer tx = tx(DatamartMedicationSamples.Datamart.create().medication());
    assertThat(tx.bestCode()).isEqualTo(Fhir.create().codeRxNorm());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @Test
  public void medication() {
    assertThat(json(tx(Datamart.create().medication()).toFhir()))
        .isEqualTo(json(Fhir.create().medication()));
  }

  @Test
  public void product() {
    DatamartMedicationTransformer tx = tx(DatamartMedicationSamples.Datamart.create().medication());
    assertThat(tx.product(Optional.empty())).isNull();
    assertThat(tx.product(Datamart.create().medication().product()))
        .isEqualTo(Fhir.create().product());
  }

  @Test
  public void text() {
    DatamartMedicationTransformer tx = tx(DatamartMedicationSamples.Datamart.create().medication());
    assertThat(tx.bestText()).isEqualTo(Fhir.create().textRxNorm());
  }

  DatamartMedicationTransformer tx(DatamartMedication dm) {
    return DatamartMedicationTransformer.builder().datamart(dm).build();
  }
}
