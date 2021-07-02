package gov.va.api.health.dataquery.service.controller.practitionerrole;

import gov.va.api.lighthouse.datamart.DatamartEntity;
import gov.va.api.lighthouse.datamart.Payload;
import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@Table(name = "PractitionerRole_Specialty_Map", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@IdClass(PractitionerRoleSpecialtyMapEntity.SpecialtyId.class)
public class PractitionerRoleSpecialtyMapEntity implements DatamartEntity {

  @Id
  @Column(name = "PractitionerRoleIdNumber")
  @EqualsAndHashCode.Include
  private BigInteger practitionerRoleIdNumber;

  @Id
  @Column(name = "PractitionerRoleResourceCode")
  @EqualsAndHashCode.Include
  private char practitionerRoleResourceCode;

  @Id
  @Column(name = "SpecialtyCode")
  @EqualsAndHashCode.Include
  private String specialtyCode;

  @Override
  public String cdwId() {
    return practitionerRoleIdNumber + ":" + practitionerRoleResourceCode + ":" + specialtyCode;
  }

  public SpecialtyId specialtyId() {
    return SpecialtyId.builder()
        .practitionerRoleIdNumber(practitionerRoleIdNumber)
        .practitionerRoleResourceCode(practitionerRoleResourceCode)
        .specialtyCode(specialtyCode)
        .build();
  }

  public Payload<?> toPayload() {
    throw new IllegalArgumentException("not-used");
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class SpecialtyId implements Serializable {
    private BigInteger practitionerRoleIdNumber;
    private char practitionerRoleResourceCode;
    private String specialtyCode;
  }
}
