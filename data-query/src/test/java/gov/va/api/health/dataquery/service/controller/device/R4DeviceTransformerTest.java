package gov.va.api.health.dataquery.service.controller.device;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.Device;
import org.junit.jupiter.api.Test;

public class R4DeviceTransformerTest {
  @Test
  void device() {
    var sample = tx(DeviceSamples.Datamart.create().device()).toFhir();
    var expected = DeviceSamples.R4.create().device();
    assertThat(sample).isEqualTo(expected);
  }

  @Test
  void empty() {
    var sample = tx(DatamartDevice.builder().build()).toFhir();
    var expected = Device.builder().resourceType("Device").build();
    assertThat(sample).isEqualTo(expected);
  }

  private R4DeviceTransformer tx(DatamartDevice dm) {
    return R4DeviceTransformer.builder().datamart(dm).build();
  }
}
