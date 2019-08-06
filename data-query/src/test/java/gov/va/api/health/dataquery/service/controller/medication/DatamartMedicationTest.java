package gov.va.api.health.dataquery.service.controller.medication;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.SneakyThrows;
import org.junit.Test;

public class DatamartMedicationTest {
  public void assertReadable(String json) throws java.io.IOException {
    DatamartMedication dm =
        createMapper().readValue(getClass().getResourceAsStream(json), DatamartMedication.class);
    assertThat(dm).isEqualTo(sample());
  }

  private DatamartMedication sample() {
    return DatamartMedication.builder()
        .objectType("Medication")
        .objectVersion("1")
        .cdwId("1000")
        .etlDate("2019-08-06T16:12:26.430")
        .rxnorm(
            DatamartMedication.RxNorm.builder()
                .code("284205")
                .text("ALMOTRIPTAN MALATE 12.5MG TAB,UD")
                .build())
        .product(DatamartMedication.Product.builder().id("4015523").formText("TAB").build())
        .build();
  }

  @Test
  @SneakyThrows
  public void unmarshalSample() {
    assertReadable("datamart-medication.json");
  }

  @Test
  @SneakyThrows
  public void unmarshalSampleV0() {
    assertReadable("datamart-medication-v0.json");
  }
}
