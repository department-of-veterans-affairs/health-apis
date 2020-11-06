package gov.va.api.health.dataquery.service.controller.device;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DeviceSamples {

  @AllArgsConstructor(staticName = "create")
  public static class Datamart {

    public DatamartDevice device() {
      return DatamartDevice.builder()
          .objectType("Device")
          .objectVersion("1")
          .cdwId("800001608621")
          .patient(
              DatamartReference.of()
                  .type("Patient")
                  .reference("1010101010V666666")
                  .display("VETERAN,HERNAM MINAM")
                  .build())
          .location(
              Optional.of(
                  DatamartReference.of()
                      .type("Location")
                      .reference("528")
                      .display("JONESBORO VA CLINIC")
                      .build()))
          .type(
              DatamartCoding.of()
                  .system("http://snomed.info/sct")
                  .code("53350007")
                  .display("Prosthesis, device (physical object)")
                  .build())
          .manufacturer(Optional.of("BOSTON SCIENTIFIC"))
          .model(Optional.of("L331"))
          .udi(Optional.of("unsupported"))
          .lotNumber(Optional.of("A19031"))
          .serialNumber(Optional.of("819569"))
          .deviceName(Optional.of("PACEMAKER"))
          .build();
    }
  }
}
