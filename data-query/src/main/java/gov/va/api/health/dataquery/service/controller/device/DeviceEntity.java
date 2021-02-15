package gov.va.api.health.dataquery.service.controller.device;

import gov.va.api.health.dataquery.service.controller.DatamartSupport;
import gov.va.api.lighthouse.datamart.DatamartEntity;
import gov.va.api.lighthouse.datamart.Payload;
import java.time.Instant;
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
 * Datamart Device representing the following table.
 *
 * <pre>
 *  CREATE TABLE [App].[Device](
 *    [CDWId] [varchar](26) NOT NULL,
 *    [PatientFullICN] [varchar](50) NOT NULL,
 *    [Device]  AS ([App].[svfn_Device_Get_Json]([CDWId],[PatientFullICN])),
 *    [LastUpdated] [smalldatetime] NULL)
 * </pre>
 */
@Data
@Entity
@Builder
@Table(name = "Device", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeviceEntity implements DatamartEntity {
  @Id
  @Column(name = "CDWId")
  @EqualsAndHashCode.Include
  private String cdwId;

  @Column(name = "PatientFullICN")
  private String icn;

  @Column(name = "Device")
  @Basic(fetch = FetchType.EAGER)
  @Lob
  private String payload;

  @Column(name = "LastUpdated")
  private Instant lastUpdated;

  public static Sort naturalOrder() {
    return Sort.by("cdwId").ascending();
  }

  public DatamartDevice asDatamartDevice() {
    return toPayload().deserialize();
  }

  @Override
  public Payload<DatamartDevice> toPayload() {
    return Payload.ofType(DatamartDevice.class)
        .json(payload())
        .cdwId(cdwId())
        .mapper(DatamartSupport.mapper())
        .build();
  }
}
