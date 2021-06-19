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

@Data
@Entity
@Builder
@IdClass(CompositeCdwId.class)
@Table(name = "PractitionerRole", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PractitionerRoleEntity implements CompositeIdDatamartEntity {
  @Id
  @Column(name = "CdwIdNumber")
  @EqualsAndHashCode.Include
  private BigInteger cdwIdNumber;

  @Id
  @Column(name = "CdwIdResourceCode")
  @EqualsAndHashCode.Include
  private char cdwIdResourceCode;

  @Column(name = "PractitionerIdNumber")
  private BigInteger practitionerIdNumber;

  @Column(name = "PractitionerResourceCode")
  private char practitionerResourceCode;

  @Column(name = "PractitionerGivenName")
  private String givenName;

  @Column(name = "PractitionerFamilyName")
  private String familyName;

  @Column(name = "Active")
  private Boolean active;

  @Column(name = "LastUpdated")
  private Instant lastUpdated;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "PractitionerRole")
  private String payload;

  public static Sort naturalOrder() {
    return Sort.by("cdwIdNumber").ascending().and(Sort.by("cdwIdResourceCode").ascending());
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
