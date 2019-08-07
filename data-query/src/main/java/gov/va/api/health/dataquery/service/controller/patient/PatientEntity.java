package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
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
@Table(name = "app.PatientReport")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PatientEntity {
  @Id
  @Column(name = "PatientFullIcn")
  @EqualsAndHashCode.Include
  private String icn;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "PatientReport")
  private String payload;

  @OneToOne
  @JoinColumn(name = "PatientFullIcn", referencedColumnName = "fullIcn")
  private PatientSearchEntity search;

  @SneakyThrows
  DatamartPatient asDatamartPatient() {
    return JacksonConfig.createMapper().readValue(payload, DatamartPatient.class);
  }
}
