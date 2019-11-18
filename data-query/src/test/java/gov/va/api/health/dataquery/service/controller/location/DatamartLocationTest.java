package gov.va.api.health.dataquery.service.controller.location;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.SneakyThrows;
import org.junit.Test;

public class DatamartLocationTest {
  private static DatamartLocation sample() {
    return DatamartLocation.builder().cdwId("561596:I").build();
  }

  @SneakyThrows
  private void assertReadable(String json) {
    DatamartLocation dm =
        createMapper().readValue(getClass().getResourceAsStream(json), DatamartLocation.class);
    assertThat(dm).isEqualTo(sample());
  }

  @Test
  public void lazy() {
    //	  DatamartLocation dm = DatamartOrganization.builder().build();
    //    assertThat(dm.agencyId()).isEqualTo(empty());
    //    assertThat(dm.ediId()).isEqualTo(empty());
    //    assertThat(dm.npi()).isEqualTo(empty());
    //    assertThat(dm.partOf()).isEqualTo(empty());
    //    assertThat(dm.providerId()).isEqualTo(empty());
    //    assertThat(dm.stationIdentifier()).isEqualTo(empty());
    //    assertThat(dm.telecom()).isEmpty();
    //    assertThat(dm.type()).isEqualTo(empty());
  }

  @Test
  public void unmarshalSample() {
    assertReadable("datamart-location.json");
  }
}
