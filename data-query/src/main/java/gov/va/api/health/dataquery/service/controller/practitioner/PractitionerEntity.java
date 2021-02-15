package gov.va.api.health.dataquery.service.controller.practitioner;

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

@Data
@Entity
@Builder
@Table(name = "Practitioner", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PractitionerEntity implements DatamartEntity {
  @Id
  @Column(name = "CDWID")
  @EqualsAndHashCode.Include
  private String cdwId;

  @Column(name = "NPI", nullable = true)
  private String npi;

  @Column(name = "FamilyName", nullable = true)
  private String familyName;

  @Column(name = "GivenName", nullable = true)
  private String givenName;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "Practitioner")
  private String payload;

  public static Sort naturalOrder() {
    return Sort.by("cdwId").ascending();
  }

  /** Deserialize payload. */
  public DatamartPractitioner asDatamartPractitioner() {
    DatamartPractitioner dm = toPayload().deserialize();

    if (dm.practitionerRole().isPresent()) {
      dm.practitionerRole()
          .get()
          .managingOrganization()
          .ifPresent(mo -> mo.typeIfMissing("Organization"));
      dm.practitionerRole().get().location().forEach(loc -> loc.typeIfMissing("Location"));
    }

    return dm;
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
