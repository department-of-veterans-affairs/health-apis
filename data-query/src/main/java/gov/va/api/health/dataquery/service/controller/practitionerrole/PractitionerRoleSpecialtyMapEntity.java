package gov.va.api.health.dataquery.service.controller.practitionerrole;

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
@IdClass(SpecialtyMapCompositeId.class)
public class PractitionerRoleSpecialtyMapEntity {

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
}
