package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.dataquery.service.controller.DatamartSupport;
import gov.va.api.lighthouse.datamart.DatamartEntity;
import gov.va.api.lighthouse.datamart.Payload;
import java.time.Instant;
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
@Table(name = "Patient", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PatientEntityV2 implements DatamartEntity {
  @Id
  @Column(name = "PatientFullICN")
  @EqualsAndHashCode.Include
  private String icn;

  @Column(name = "FullName")
  private String fullName;

  @Column(name = "LastName")
  private String lastName;

  @Column(name = "FirstName")
  private String firstName;

  @Column(name = "BirthDate")
  private Instant birthDate;

  @Column(name = "Gender")
  private String gender;

  @Column(name = "SSN")
  private String ssn;

  @Column(name = "LastUpdated")
  private Instant lastUpdated;

  @Column(name = "ManagingOrganization")
  private String managingOrganization;

  @Lob
  @Column(name = "Patient")
  @Basic(fetch = FetchType.EAGER)
  private String payload;

  static Sort naturalOrder() {
    return Sort.by("icn").ascending();
  }

  DatamartPatient asDatamartPatient() {
    return toPayload().deserialize();
  }

  @Override
  public String cdwId() {
    return icn();
  }

  @Override
  public Payload<DatamartPatient> toPayload() {
    return Payload.ofType(DatamartPatient.class)
        .json(payload())
        .cdwId(cdwId())
        .mapper(DatamartSupport.mapper())
        .build();
  }
}
