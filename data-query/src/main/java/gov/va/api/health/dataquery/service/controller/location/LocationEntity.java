package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartEntity;
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
import lombok.SneakyThrows;
import org.springframework.data.domain.Sort;

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

  @Lob
  @Column(name = "Location")
  @Basic(fetch = FetchType.EAGER)
  private String payload;

  static Sort naturalOrder() {
    return Sort.by("cdwId").ascending();
  }

  @SneakyThrows
  DatamartLocation asDatamartLocation() {
    return JacksonConfig.createMapper().readValue(payload, DatamartLocation.class);
  }
}
