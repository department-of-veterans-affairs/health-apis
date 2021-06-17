package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.dataquery.service.controller.DatamartSupport;
import gov.va.api.lighthouse.datamart.DatamartEntity;
import gov.va.api.lighthouse.datamart.Payload;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

/** Location DB model. */
@Data
@Entity
@Builder
@Table(name = "Location", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationEntity implements DatamartEntity {
  @Id
  @Column(name = "CDWID")
  @EqualsAndHashCode.Include
  private String cdwId;

  @Column(name = "Name", nullable = true)
  private String name;

  @Column(name = "Street", nullable = true)
  private String street;

  @Column(name = "City", nullable = true)
  private String city;

  @Column(name = "State", nullable = true)
  private String state;

  @Column(name = "PostalCode", nullable = true)
  private String postalCode;

  @Column(name = "StationNumber", nullable = true)
  private String stationNumber;

  @Column(name = "FacilityType", nullable = true)
  private String facilityType;

  @Column(name = "LocationIen", nullable = true)
  private String locationIen;

  @Column(name = "ManagingOrgIdNumber", nullable = true)
  private Integer managingOrgIdNumber;

  @Column(name = "ManagingOrgResourceCode", nullable = true)
  private Character managingOrgResourceCode;

  @Lob
  @Column(name = "Location")
  @Basic(fetch = FetchType.EAGER)
  private String payload;

  static Sort naturalOrder() {
    return Sort.by("cdwId").ascending();
  }

  DatamartLocation asDatamartLocation() {
    return toPayload().deserialize();
  }

  @Override
  public Payload<DatamartLocation> toPayload() {
    return Payload.ofType(DatamartLocation.class)
        .json(payload())
        .cdwId(cdwId())
        .mapper(DatamartSupport.mapper())
        .build();
  }
}
