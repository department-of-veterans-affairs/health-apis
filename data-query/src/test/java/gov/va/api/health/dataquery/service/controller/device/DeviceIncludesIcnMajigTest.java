package gov.va.api.health.dataquery.service.controller.device;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import java.util.List;
import org.junit.jupiter.api.Test;

public class DeviceIncludesIcnMajigTest {
  @Test
  public void r4() {
    ExtractIcnValidator.builder()
        .majig(new R4DeviceIncludesIcnMajig())
        .body(
            gov.va.api.health.r4.api.resources.Device.builder()
                .id("123")
                .patient(
                    gov.va.api.health.r4.api.elements.Reference.builder()
                        .reference("Patient/1010101010V666666")
                        .build())
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }
}
