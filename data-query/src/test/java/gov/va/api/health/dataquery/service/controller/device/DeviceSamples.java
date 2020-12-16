package gov.va.api.health.dataquery.service.controller.device;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.resources.Device;
import gov.va.api.lighthouse.datamart.DatamartCoding;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DeviceSamples {
  public static ResourceIdentity id(String cdwId) {
    return ResourceIdentity.builder().system("CDW").resource("DEVICE").identifier(cdwId).build();
  }

  @SneakyThrows
  static String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public static Registration registration(String cdwId, String publicId) {
    return Registration.builder().uuid(publicId).resourceIdentities(List.of(id(cdwId))).build();
  }

  @AllArgsConstructor(staticName = "create")
  public static class Datamart {
    public DatamartDevice device() {
      return device("800001608621", "1010101010V666666");
    }

    public DatamartDevice device(String cdwId, String patientIcn) {
      return DatamartDevice.builder()
          .objectType("Device")
          .objectVersion("1")
          .cdwId(cdwId)
          .patient(
              DatamartReference.of()
                  .type("Patient")
                  .reference(patientIcn)
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

    public DeviceEntity entity(DatamartDevice dm) {
      return DeviceEntity.builder()
          .cdwId(dm.cdwId())
          .icn(dm.patient().reference().orElse(null))
          .payload(json(dm))
          .build();
    }

    public DeviceEntity entity(String cdwId, String patientIcn) {
      return entity(device(cdwId, patientIcn));
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class R4 {
    public static gov.va.api.health.r4.api.resources.Device.Bundle asBundle(
        String baseUrl,
        Collection<Device> reports,
        int totalRecords,
        gov.va.api.health.r4.api.bundle.BundleLink... links) {
      return gov.va.api.health.r4.api.resources.Device.Bundle.builder()
          .resourceType("Bundle")
          .type(gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              reports.stream()
                  .map(
                      c ->
                          gov.va.api.health.r4.api.resources.Device.Entry.builder()
                              .fullUrl(baseUrl + "/Device/" + c.id())
                              .resource(c)
                              .search(
                                  gov.va.api.health.r4.api.bundle.AbstractEntry.Search.builder()
                                      .mode(
                                          gov.va.api.health.r4.api.bundle.AbstractEntry.SearchMode
                                              .match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    public static gov.va.api.health.r4.api.bundle.BundleLink link(
        gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation rel,
        String base,
        int page,
        int count) {
      return gov.va.api.health.r4.api.bundle.BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    private gov.va.api.health.r4.api.elements.Reference asReference(String display, String ref) {
      return gov.va.api.health.r4.api.elements.Reference.builder()
          .display(display)
          .reference(ref)
          .build();
    }

    public gov.va.api.health.r4.api.resources.Device device() {
      return device("800001608621", "1010101010V666666");
    }

    public gov.va.api.health.r4.api.resources.Device device(String publicId, String patientIcn) {
      return gov.va.api.health.r4.api.resources.Device.builder()
          .resourceType("Device")
          .id(publicId)
          .patient(asReference("VETERAN,HERNAM MINAM", "Patient/" + patientIcn))
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
