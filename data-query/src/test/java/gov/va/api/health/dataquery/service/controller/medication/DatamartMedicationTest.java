package gov.va.api.health.dataquery.service.controller.medication;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Test;

public class DatamartMedicationTest {
  public void assertReadable(String json, DatamartMedication expected) throws java.io.IOException {
    DatamartMedication dm =
        createMapper().readValue(getClass().getResourceAsStream(json), DatamartMedication.class);
    assertThat(dm).isEqualTo(expected);
  }

  private DatamartMedication sample() {
    return DatamartMedication.builder()
        .objectType("Medication")
        .objectVersion("1")
        .cdwId("1000")
        .localDrugName("Axert")
        .rxnorm(
            Optional.of(
                DatamartMedication.RxNorm.builder()
                    .code("284205")
                    .text("ALMOTRIPTAN MALATE 12.5MG TAB,UD")
                    .build()))
        .product(
            Optional.of(DatamartMedication.Product.builder().id("4015523").formText("TAB").build()))
        .build();
  }

  @Test
  @SneakyThrows
  public void unmarshalSample() {
    assertReadable("datamart-medication.json", sample());
  }

  @Test
  @SneakyThrows
  public void unmarshalSampleV0() {
    assertReadable("datamart-medication-v0.json", sample().localDrugName("Unknown"));
  }
}
