package gov.va.api.health.dataquery.service.controller.observation;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.SneakyThrows;
import org.junit.Test;

public class DatamartObservationTest {
  @SneakyThrows
  public void assertReadable(String json) {
    assertThat(
            createMapper()
                .readValue(getClass().getResourceAsStream(json), DatamartObservation.class))
        .isEqualTo(sample());
  }

  public DatamartObservation sample() {
    return DatamartObservation.builder().objectType("Observation").objectVersion(1).build();
  }

  @Test
  public void unmarshalSample() {
    assertReadable("datamart-observation.json");
  }
}
