package gov.va.api.health.dataquery.service.controller.device;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asCoding;
import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.resources.Device;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class R4DeviceTransformer {
  @NonNull private final DatamartDevice datamart;

  private List<Device.DeviceName> deviceNames() {
    List<Device.DeviceName> deviceNames = new ArrayList<>();
    datamart
        .model()
        .ifPresent(
            model ->
                deviceNames.add(
                    Device.DeviceName.builder()
                        .name(model)
                        .type(Device.DeviceNameType.model_name)
                        .build()));
    datamart
        .deviceName()
        .ifPresent(
            name ->
                deviceNames.add(
                    Device.DeviceName.builder()
                        .name(name)
                        .type(Device.DeviceNameType.user_friendly_name)
                        .build()));
    return emptyToNull(deviceNames);
  }

  private CodeableConcept deviceType() {
    Coding coding = asCoding(datamart.type());
    if (coding == null) {
      return null;
    }
    return CodeableConcept.builder().coding(List.of(coding)).build();
  }

  /** Transform DatamartDevice to a valid US-Core-R4 Implantable-Device. */
  public Device toFhir() {
    return Device.builder()
        .resourceType("Device")
        .id(datamart.cdwId())
        .patient(asReference(datamart.patient()))
        .owner(asReference(datamart.location()))
        .type(deviceType())
        .manufacturer(datamart.manufacturer().orElse(null))
        .deviceName(deviceNames())
        .lotNumber(datamart.lotNumber().orElse(null))
        .serialNumber(datamart.serialNumber().orElse(null))
        .build();
  }
}
