package gov.va.api.health.dataquery.service.controller.practitioner;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class DatamartPractitionerTest {
  private static DatamartPractitioner sample() {
    return DatamartPractitioner.builder()
        .cdwId("416704")
        .npi(Optional.of("1932127842"))
        .active(true)
        .name(
            DatamartPractitioner.Name.builder()
                .family("LASTNAME")
                .given("FIRSTNAME A.")
                .prefix(Optional.of("DR."))
                .suffix(Optional.of("PHD"))
                .build())
        .telecom(
            asList(
                DatamartPractitioner.Telecom.builder()
                    .system(DatamartPractitioner.Telecom.System.phone)
                    .value("555-555-1137")
                    .use(DatamartPractitioner.Telecom.Use.work)
                    .build(),
                DatamartPractitioner.Telecom.builder()
                    .system(DatamartPractitioner.Telecom.System.phone)
                    .value("555-4055")
                    .use(DatamartPractitioner.Telecom.Use.home)
                    .build(),
                DatamartPractitioner.Telecom.builder()
                    .system(DatamartPractitioner.Telecom.System.pager)
                    .value("5-541")
                    .use(DatamartPractitioner.Telecom.Use.mobile)
                    .build()))
        .address(
            asList(
                DatamartPractitioner.Address.builder()
                    .temp(false)
                    .line1("555 E 5TH ST")
                    .line2("SUITE B")
                    .city("CHEYENNE")
                    .state("WYOMING")
                    .postalCode("82001")
                    .build()))
        .gender(DatamartPractitioner.Gender.female)
        .birthDate(Optional.of(LocalDate.of(1965, 3, 16)))
        .build();
  }

  @SneakyThrows
  private void assertReadable(String json) {
    DatamartPractitioner dm =
        createMapper().readValue(getClass().getResourceAsStream(json), DatamartPractitioner.class);
    assertThat(dm).isEqualTo(sample());
  }

  @Test
  public void lazy() {
    DatamartPractitioner dm = DatamartPractitioner.builder().build();
    assertThat(dm.address()).isEmpty();
    assertThat(dm.birthDate()).isEqualTo(empty());
    assertThat(dm.npi()).isEqualTo(empty());
    assertThat(dm.telecom()).isEmpty();
    DatamartPractitioner.Name name = DatamartPractitioner.Name.builder().build();
    assertThat(name.prefix()).isEqualTo(empty());
    assertThat(name.suffix()).isEqualTo(empty());
  }

  @Test
  public void unmarshalSample() {
    assertReadable("datamart-practitioner.json");
  }
}
