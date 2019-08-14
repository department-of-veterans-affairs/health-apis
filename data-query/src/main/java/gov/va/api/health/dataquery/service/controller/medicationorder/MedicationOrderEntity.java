package gov.va.api.health.dataquery.service.controller.medicationorder;

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

/**
 * Datamart MedicationOrder representing the following table.
 *
 * <pre>
 * CREATE TABLE [app].[MedicationOrder](
 *   [CDWId] [bigint] NOT NULL,
 *   [PatientFullICN] [varchar](50) NOT NULL,
 *   [DateRecorded] [datetime2](0) NULL,
 *   [MedicationOrder] [varchar](max) NULL,
 *   [ETLBatchId] [int] NULL,
 *   [ETLCreateDate] [datetime2](0) NULL,
 *   [ETLEditDate] [datetime2](0) NULL,
 * </pre>
 */
@Data
@Entity
@Builder
@Table(name = "MedicationOrder", schema = "app")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MedicationOrderEntity {

  @Id
  @EqualsAndHashCode.Include
  @Column(name = "CDWId")
  private String cdwId;

  @Column(name = "PatientFullICN")
  private String icn;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "MedicationOrder")
  private String payload;

  static Sort naturalOrder() {
    return Sort.by("cdwId").ascending();
  }

  @SneakyThrows
  DatamartMedicationOrder asDatamartMedicationOrder() {
    return JacksonConfig.createMapper().readValue(payload, DatamartMedicationOrder.class);
  }
}
