package gov.va.api.health.dataquery.service.controller.practitioner;

import gov.va.api.health.dataquery.service.controller.DatamartSupport;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.datamart.CompositeIdDatamartEntity;
import gov.va.api.lighthouse.datamart.Payload;
import java.math.BigInteger;
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
@Table(name = "Practitioner", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PractitionerEntity implements CompositeIdDatamartEntity {
  @Id
  @EqualsAndHashCode.Include
  @Column(name = "CdwIdNumber")
  private BigInteger cdwIdNumber;

  @Id
  @EqualsAndHashCode.Include
  @Column(name = "CdwIdResourceCode")
  private char cdwIdResourceCode;

  @Column(name = "NPI")
  private String npi;

  @Column(name = "FamilyName")
  private String familyName;

  @Column(name = "GivenName")
  private String givenName;

  @Lob
  @Column(name = "Practitioner")
  @Basic(fetch = FetchType.EAGER)
  private String payload;

  public static Sort naturalOrder() {
    return Sort.by("cdwIdNumber").ascending().and(Sort.by("cdwIdResourceCode").ascending());
  }

  /** Deserialize payload. */
  public DatamartPractitioner asDatamartPractitioner() {
    DatamartPractitioner dm = toPayload().deserialize();
    dm.practitionerRole()
        .ifPresent(
            pr -> pr.managingOrganization().ifPresent(mo -> mo.typeIfMissing("Organization")));
    dm.practitionerRole()
        .ifPresent(pr -> pr.location().forEach(loc -> loc.typeIfMissing("Location")));
    return dm;
  }

  @Override
  public CompositeCdwId compositeCdwId() {
    return new CompositeCdwId(cdwIdNumber(), cdwIdResourceCode());
  }

  @Override
  public Payload<DatamartPractitioner> toPayload() {
    return Payload.ofType(DatamartPractitioner.class)
        .json(payload())
        .cdwId(cdwId())
        .mapper(DatamartSupport.mapper())
        .build();
  }
}
