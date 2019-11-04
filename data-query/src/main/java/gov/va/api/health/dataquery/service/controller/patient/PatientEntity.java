package gov.va.api.health.dataquery.service.controller.patient;

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

@Data
@Entity
@Builder
@Table(name = "PatientReport", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PatientEntity implements DatamartEntity {
  @Id
  @Column(name = "PatientFullIcn")
  @EqualsAndHashCode.Include
  private String icn;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "PatientReport")
  private String payload;

  @SneakyThrows
  DatamartPatient asDatamartPatient() {
    return JacksonConfig.createMapper().readValue(payload, DatamartPatient.class);
  }

  @Override
  public String cdwId() {
    return icn();
  }
}
