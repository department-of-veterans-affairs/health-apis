package gov.va.api.health.dataquery.service.controller.location;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class DatamartLocationTest {
  private static DatamartLocation sample() {
    return DatamartLocation.builder()
        .cdwId("1000200441:L")
        .status(DatamartLocation.Status.active)
        .name("TEM MH PSO TRS IND93EH")
        .description(Optional.of("BLDG 146, RM W02"))
        .type(Optional.of("PSYCHIATRY CLINIC"))
        .telecom("254-743-2867")
        .address(
            DatamartLocation.Address.builder()
                .line1("1901 VETERANS MEMORIAL DRIVE")
                .city("TEMPLE")
                .state("TEXAS")
                .postalCode("76504")
                .build())
        .physicalType(Optional.of("BLDG 146, RM W02"))
        .managingOrganization(
            DatamartReference.builder()
                .reference(Optional.of("390026:I"))
                .display(Optional.of("OLIN E. TEAGUE VET CENTER"))
                .build())
        .build();
  }

  @SneakyThrows
  private void assertReadable(String json) {
    DatamartLocation dm =
        createMapper().readValue(getClass().getResourceAsStream(json), DatamartLocation.class);
    assertThat(dm).isEqualTo(sample());
  }

  @Test
  public void lazy() {
    DatamartLocation dm = DatamartLocation.builder().build();
    assertThat(dm.description()).isEqualTo(empty());
    assertThat(dm.type()).isEqualTo(empty());
    assertThat(dm.physicalType()).isEqualTo(empty());
  }

  @Test
  public void unmarshalSample() {
    assertReadable("datamart-location.json");
  }
}
