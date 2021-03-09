package gov.va.api.health.dataquery.service.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FacilityId {
  private String stationNumber;

  private FacilityType type;

  public enum FacilityType {
    HEALTH,
    BENEFITS,
    VET_CENTER,
    CEMETERY,
    NONNATIONAL_CEMETERY
  }
}