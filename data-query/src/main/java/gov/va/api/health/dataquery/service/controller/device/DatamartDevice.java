package gov.va.api.health.dataquery.service.controller.device;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.datamart.HasReplaceableId;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartDevice implements HasReplaceableId {
  String cdwId;

  DatamartReference patient;

  Optional<DatamartReference> location;

  DatamartCoding type;

  Optional<String> manufacturer;

  Optional<String> model;

  Optional<String> udi;

  Optional<String> lotNumber;

  Optional<String> serialNumber;

  Optional<String> deviceName;

  @Builder.Default private String objectType = "Device";

  @Builder.Default private String objectVersion = "1";

  /** Lazy Getter. */
  public Optional<String> deviceName() {
    if (deviceName == null) {
      return Optional.empty();
    }
    return deviceName;
  }

  /** Lazy Getter. */
  public Optional<DatamartReference> location() {
    if (location == null) {
      return Optional.empty();
    }
    return location;
  }

  /** Lazy Getter. */
  public Optional<String> lotNumber() {
    if (lotNumber == null) {
      return Optional.empty();
    }
    return lotNumber;
  }

  /** Lazy Getter. */
  public Optional<String> manufacturer() {
    if (manufacturer == null) {
      return Optional.empty();
    }
    return manufacturer;
  }

  /** Lazy Getter. */
  public Optional<String> model() {
    if (model == null) {
      return Optional.empty();
    }
    return model;
  }

  /** Lazy Getter. */
  public Optional<String> serialNumber() {
    if (serialNumber == null) {
      return Optional.empty();
    }
    return serialNumber;
  }

  /** Lazy Getter. */
  public Optional<String> udi() {
    if (udi == null) {
      return Optional.empty();
    }
    return udi;
  }
}
