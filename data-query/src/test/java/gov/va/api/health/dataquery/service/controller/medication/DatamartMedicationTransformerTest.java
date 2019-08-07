package gov.va.api.health.dataquery.service.controller.medication;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedicationSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedicationSamples.Fhir;
import lombok.SneakyThrows;
import org.junit.Test;

public class DatamartMedicationTransformerTest {

  @Test
  public void code() {
    DatamartMedicationTransformer tx = tx(DatamartMedicationSamples.Datamart.create().medication());
    assertThat(tx.code(Datamart.create().medication().rxnorm())).isEqualTo(Fhir.create().code());
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
    assertThat(tx.product(Datamart.create().medication().product()))
        .isEqualTo(Fhir.create().product());
  }

  @Test
  public void text() {
    DatamartMedicationTransformer tx = tx(DatamartMedicationSamples.Datamart.create().medication());
    assertThat(tx.text(Datamart.create().medication().rxnorm().text()))
        .isEqualTo(Fhir.create().text());
  }

  DatamartMedicationTransformer tx(DatamartMedication dm) {
    return DatamartMedicationTransformer.builder().datamart(dm).build();
  }
}
