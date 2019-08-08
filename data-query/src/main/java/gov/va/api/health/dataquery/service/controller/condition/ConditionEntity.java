package gov.va.api.health.dataquery.service.controller.condition;

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
 * Datamart Condition representing the following table.
 *
 * <pre>
 * CREATE TABLE [app].[Condition](
 *  [CDWId] [varchar](50) NOT NULL,
 *  [PatientFullICN] [varchar](50) NOT NULL,
 *  [AsserterCDWId] [int] NULL,
 *  [Category] [varchar](50) NULL,
 *  [ClinicalStatus] [varchar](50) NULL,
 *  [DateRecorded] [datetime2](0) NULL,
 *  [EncounterCDWId] [bigint] NULL,
 *  [OnSet] [datetime2](0) NULL,
 *  [Condition] [varchar](max) NULL,
 *  [ETLBatchId] [int] NULL,
 *  [ETLCreateDate] [datetime2](0) NULL,
 *  [ETLEditDate] [datetime2](0) NULL,
 * </pre>
 */
@Data
@Entity
@Builder
@Table(name = "Condition", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConditionEntity {
  @Id
  @Column(name = "CDWId")
  @EqualsAndHashCode.Include
  private String cdwId;

  @Column(name = "PatientFullICN")
  private String icn;

  @Column(name = "Category")
  private String category;

  @Column(name = "ClinicalStatus")
  private String clinicalStatus;

  @Column(name = "Condition")
  @Basic(fetch = FetchType.EAGER)
  @Lob
  private String payload;

  @SneakyThrows
  DatamartCondition asDatamartCondition() {
    return JacksonConfig.createMapper().readValue(payload, DatamartCondition.class);
  }
}
