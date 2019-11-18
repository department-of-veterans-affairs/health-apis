package gov.va.api.health.dataquery.service.controller.location;

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
public final class DatamartLocation implements HasReplaceableId {

  @Builder.Default private String objectType = "Location";

  @Builder.Default private String objectVersion = "1";

  private String cdwId;

  private Status status;

  private String name;

  private Optional<String> description;

  private Optional<String> type;

  private String telecom;

  private Address address;

  private Optional<String> physicalType;

  private DatamartReference managingOrganization;

  /** Lazy initialization. */
  public Optional<String> description() {
    if (description == null) {
      description = Optional.empty();
    }
    return description;
  }

  /** Lazy initialization. */
  public Optional<String> physicalType() {
    if (physicalType == null) {
      physicalType = Optional.empty();
    }
    return physicalType;
  }

  /** Lazy initialization. */
  public Optional<String> type() {
    if (type == null) {
      type = Optional.empty();
    }
    return type;
  }

  public enum Status {
    active,
    inactive
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Address {

    private String line1;

    private String city;

    private String state;

    private String postalCode;
  }
}
