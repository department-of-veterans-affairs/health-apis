package gov.va.api.health.dataquery.service.controller.observation;

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

@Data
@Entity
@Builder
@Table(name = "Observation", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ObservationEntity {
  @Id
  @Column(name = "CDWId")
  @EqualsAndHashCode.Include
  private String id;

  @Column(name = "PatientFullICN")
  private String icn;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "Observation")
  private String payload;

  @SneakyThrows
  DatamartObservation asDatamartObservation() {
    return JacksonConfig.createMapper().readValue(payload, DatamartObservation.class);
  }
}
