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
}
