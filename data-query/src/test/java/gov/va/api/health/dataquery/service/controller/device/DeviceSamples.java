package gov.va.api.health.dataquery.service.controller.device;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import java.util.List;
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

  @AllArgsConstructor(staticName = "create")
  public static class R4 {
    private gov.va.api.health.r4.api.elements.Reference asReference(String display, String ref) {
      return gov.va.api.health.r4.api.elements.Reference.builder()
          .display(display)
          .reference(ref)
          .build();
    }

    public gov.va.api.health.r4.api.resources.Device device() {
      return gov.va.api.health.r4.api.resources.Device.builder()
          .resourceType("Device")
          .id("800001608621")
          .patient(asReference("VETERAN,HERNAM MINAM", "Patient/1010101010V666666"))
          .owner(asReference("JONESBORO VA CLINIC", "Location/528"))
          .type(
              gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                  .coding(
                      List.of(
                          gov.va.api.health.r4.api.datatypes.Coding.builder()
                              .system("http://snomed.info/sct")
                              .code("53350007")
                              .display("Prosthesis, device (physical object)")
                              .build()))
                  .build())
          .manufacturer("BOSTON SCIENTIFIC")
          .deviceName(
              List.of(
                  gov.va.api.health.r4.api.resources.Device.DeviceName.builder()
                      .name("L331")
                      .type(gov.va.api.health.r4.api.resources.Device.DeviceNameType.model_name)
                      .build(),
                  gov.va.api.health.r4.api.resources.Device.DeviceName.builder()
                      .name("PACEMAKER")
                      .type(
                          gov.va.api.health.r4.api.resources.Device.DeviceNameType
                              .user_friendly_name)
                      .build()))
          .lotNumber("A19031")
          .serialNumber("819569")
          .build();
    }
  }
}
