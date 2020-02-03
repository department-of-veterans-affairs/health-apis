package gov.va.api.health.dataquery.service.controller.medicationstatement;

import static gov.va.api.health.dataquery.service.controller.datamart.DatamartEntity.deserializeDatamart;

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
import org.springframework.data.domain.Sort;

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
@Table(name = "MedicationStatement", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MedicationStatementEntity implements DatamartEntity {
  @Id
  @Column(name = "CDWId")
  @EqualsAndHashCode.Include
  private String cdwId;

  @Column(name = "PatientFullICN")
  private String icn;

  @Column(name = "MedicationStatement")
  @Basic(fetch = FetchType.EAGER)
  @Lob
  private String payload;

  static Sort naturalOrder() {
    return Sort.by("cdwId").ascending();
  }

  DatamartMedicationStatement asDatamartMedicationStatement() {
    return deserializeDatamart(payload, DatamartMedicationStatement.class);
  }
}
