package gov.va.api.health.dataquery.service.controller.immunization;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
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
@Table(name = "Immunization", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ImmunizationEntity {
  @Id
  @Column(name = "CDWId")
  @EqualsAndHashCode.Include
  private String cdwId;

  @Column(name = "PatientFullICN")
  private String icn;

  @Column(name = "Immunization")
  @Basic(fetch = FetchType.EAGER)
  @Lob
  private String payload;

  static Sort naturalOrder() {
    return Sort.by("cdwId").ascending();
  }

  @SneakyThrows
  DatamartImmunization asDatamartImmunization() {
    return JacksonConfig.createMapper().readValue(payload, DatamartImmunization.class);
  }
}
