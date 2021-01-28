package gov.va.api.health.dataquery.service.controller.appointment;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DatamartAppointmentTest {
  private final DatamartAppointment expected = AppointmentSamples.Datamart.create().appointment();

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = {"datamart-appointment-v1.json"})
  void assertReadable(String file) {
    DatamartAppointment sample =
        JacksonConfig.createMapper()
            .readValue(getClass().getResourceAsStream(file), DatamartAppointment.class);
    assertThat(sample).isEqualTo(expected);
  }
}
