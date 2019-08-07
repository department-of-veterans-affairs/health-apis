package gov.va.api.health.dataquery.service.controller.immunization;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import java.math.BigInteger;
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

/**
 *
 *
 * <pre>
 *  CREATE TABLE [app].[Immunization](
 *         [CDWId] [bigint] NOT NULL,
 *         [PatientFullICN] [varchar](50) NOT NULL,
 *         [PerformerCDWId] [int] NULL,
 *         [RequesterCDWId] [int] NULL,
 *         [DateRecorded] [datetime2](0) NULL,
 *         [Immunization] [varchar](max) NULL,
 *         [ETLBatchId] [int] NULL,
 *         [ETLCreateDate] [datetime2](0) NULL,
 *         [ETLEditDate] [datetime2](0) NULL,
 * PRIMARY KEY CLUSTERED
 * </pre>
 */
@Data
@Entity
@Builder
@Table(name = "app.Immunization")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ImmunizationEntity {
  @Id
  @Column(name = "CDWId")
  @EqualsAndHashCode.Include
  private BigInteger cdwId;

  @Column(name = "PatientFullICN")
  private String icn;

  @Column(name = "Immunization")
  @Basic(fetch = FetchType.EAGER)
  @Lob
  private String payload;

  @SneakyThrows
  DatamartImmunization asDatamartImmunization() {
    return JacksonConfig.createMapper().readValue(payload, DatamartImmunization.class);
  }
}
