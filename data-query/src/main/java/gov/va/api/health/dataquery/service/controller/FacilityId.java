package gov.va.api.health.dataquery.service.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FacilityId {
  private String stationNumber;

  private FacilityType type;

  /**
   * Static Constructor that parses a Faciltiies API ID.
   *
   * @param id a Facilities API id
   * @return A FacilityId object
   */
  public static FacilityId from(String id) {
    FacilityType type = FacilityType.forFacilityApiId(id);
    if (type.fapiPrefix.length() >= id.length()) {
      throw new IllegalArgumentException("Cannot determine stationNumber for ID: " + id);
    }
    return FacilityId.builder()
        .type(type)
        .stationNumber(id.substring(type.fapiPrefix().length() - 1))
        .build();
  }

  @Override
  public String toString() {
    return type().fapiPrefix() + stationNumber();
  }

  @AllArgsConstructor
  public enum FacilityType {
    HEALTH("vha_"),
    BENEFITS("vba_"),
    VET_CENTER("vc_"),
    CEMETERY("nca_"),
    NONNATIONAL_CEMETERY("nca_s");

    @Getter private final String fapiPrefix;

    private static FacilityType cemeteryType(String id) {
      if (id.startsWith(NONNATIONAL_CEMETERY.fapiPrefix())) {
        return NONNATIONAL_CEMETERY;
      }
      return CEMETERY;
    }

    static FacilityType forFacilityApiId(@NonNull String id) {
      for (FacilityType type : FacilityType.values()) {
        if (id.startsWith(type.fapiPrefix())) {
          return type == CEMETERY ? cemeteryType(id) : type;
        }
      }
      throw new IllegalArgumentException("Cannot determine facility type for ID: " + id);
    }
  }
}
