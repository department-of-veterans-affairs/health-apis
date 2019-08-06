package gov.va.api.health.dataquery.service.controller.medicationstatement;

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

/**
 * Datamart MedicationStatement representing the following table.
 *
 * <pre>
 * CREATE TABLE [app].[MedicationStatement](
 *   [CDWId] [bigint] NOT NULL,
 *   [PatientFullICN] [varchar](50) NOT NULL,
 *   [DateRecorded] [datetime2](0) NULL,
 *   [MedicationStatement] [varchar](max) NULL,
 *   [ETLBatchId] [int] NULL,
 *   [ETLCreateDate] [datetime2](0) NULL,
 *   [ETLEditDate] [datetime2](0) NULL,
 * </pre>
 */
@Data
@Entity
@Builder
@Table(name = "app.MedicationStatement")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MedicationStatementEntity {

  @Id
  @Column(name = "CDWId")
  @EqualsAndHashCode.Include
  private String cdwId;

  @Column(name = "PatientFullICN")
  private String icn;

  @Column(name = "MedicationStatement")
  @Basic(fetch = FetchType.LAZY)
  @Lob
  private String payload;

  @SneakyThrows
  DatamartMedicationStatement asDatamartMedicationStatement() {
    return JacksonConfig.createMapper().readValue(payload, DatamartMedicationStatement.class);
  }
}
