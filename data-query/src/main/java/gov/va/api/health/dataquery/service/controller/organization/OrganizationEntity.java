package gov.va.api.health.dataquery.service.controller.organization;

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
import org.springframework.data.domain.Sort;

@Data
@Entity
@Builder
@Table(name = "Organization", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrganizationEntity implements DatamartEntity {
  @Id
  @Column(name = "CDWID")
  @EqualsAndHashCode.Include
  private String cdwId;

  @Column(name = "NPI", nullable = true)
  private String npi;

  @Column(name = "ProviderID", nullable = true)
  private String providerId;

  @Column(name = "EDIID", nullable = true)
  private String ediId;

  @Column(name = "AgencyID", nullable = true)
  private String agencyId;

  @Column(name = "Address", nullable = true)
  private String street;

  @Column(name = "Name", nullable = true)
  private String name;

  @Column(name = "City", nullable = true)
  private String city;

  @Column(name = "State", nullable = true)
  private String state;

  @Column(name = "PostalCode", nullable = true)
  private String postalCode;

  @Lob
  @Column(name = "Organization")
  @Basic(fetch = FetchType.EAGER)
  private String payload;

  static Sort naturalOrder() {
    return Sort.by("cdwId").ascending();
  }

  DatamartOrganization asDatamartOrganization() {
    return deserializeDatamart(payload, DatamartOrganization.class);
  }
}
