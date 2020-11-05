package gov.va.api.health.dataquery.service.controller.device;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DatamartDeviceTest {

  DatamartDevice expected = DeviceSamples.Datamart.create().device();

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = {"datamart-device.json", "datamart-device-v1.json"})
  void assertReadable(String file) {
    DatamartDevice dm =
        JacksonConfig.createMapper()
            .readValue(getClass().getResourceAsStream(file), DatamartDevice.class);
    assertThat(dm).isEqualTo(expected);
  }
}
