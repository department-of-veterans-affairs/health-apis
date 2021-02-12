package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.dataquery.service.controller.DatamartSupport;
import gov.va.api.lighthouse.datamart.DatamartEntity;
import gov.va.api.lighthouse.datamart.Payload;
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

/**
 *
 *
 * <pre>
 *  CREATE TABLE [app].[Medication](
 *         [CDWId] [varchar](50) NOT NULL,
 *         [Medication] [varchar](max) NULL,
 *         [ETLBatchId] [int] NULL,
 *         [ETLCreateDate] [datetime2](0) NULL,
 *         [ETLEditDate] [datetime2](0) NULL,
 * PRIMARY KEY CLUSTERED
 * </pre>
 */
@Data
@Entity
@Builder
@Table(name = "Medication", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MedicationEntity implements DatamartEntity {
  @Id
  @Column(name = "CDWId")
  @EqualsAndHashCode.Include
  private String cdwId;

  @Column(name = "Medication")
  @Basic(fetch = FetchType.EAGER)
  @Lob
  private String payload;

  DatamartMedication asDatamartMedication() {
    return toPayload().deserialize();
  }

  @Override
  public Payload<DatamartMedication> toPayload() {
    return Payload.ofType(DatamartMedication.class)
        .json(payload())
        .cdwId(cdwId())
        .mapper(DatamartSupport.mapper())
        .build();
  }
}
