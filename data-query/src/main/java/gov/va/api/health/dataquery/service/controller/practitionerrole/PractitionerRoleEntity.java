package gov.va.api.health.dataquery.service.controller.practitionerrole;

import gov.va.api.health.dataquery.service.controller.DatamartSupport;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.datamart.CompositeIdDatamartEntity;
import gov.va.api.lighthouse.datamart.Payload;
import java.math.BigInteger;
import java.time.Instant;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

/** PractionerRole DB model. */
@Data
@Entity
@Builder
@Table(name = "PractitionerRole", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@IdClass(CompositeCdwId.class)
public class PractitionerRoleEntity implements CompositeIdDatamartEntity {
  @Id
  @Column(name = "CdwIdNumber")
  @EqualsAndHashCode.Include
  private BigInteger cdwIdNumber;

  @Id
  @Column(name = "CdwIdResourceCode")
  @EqualsAndHashCode.Include
  private char cdwIdResourceCode;

  @Column(name = "PractitionerNPI", nullable = true)
  private String npi;

  @Column(name = "Specialty", nullable = false)
  private String specialty;

  @Column(name = "PractitionerFamilyName", nullable = true)
  private String familyName;

  @Column(name = "PractitionerGivenName", nullable = true)
  private String givenName;

  @Column(name = "LastUpdated", nullable = false)
  private Instant lastUpdated;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "PractitionerRole")
  private String payload;

  public static Sort naturalOrder() {
    return Sort.by("cdwId").ascending();
  }

  public DatamartPractitionerRole asDatamartPractitonerRole() {
    return toPayload().deserialize();
  }

  @Override
  public CompositeCdwId compositeCdwId() {
    return new CompositeCdwId(cdwIdNumber(), cdwIdResourceCode());
  }

  @Override
  public Payload<DatamartPractitionerRole> toPayload() {
    return Payload.ofType(DatamartPractitionerRole.class)
        .json(payload())
        .cdwId(cdwId())
        .mapper(DatamartSupport.mapper())
        .build();
  }
}
