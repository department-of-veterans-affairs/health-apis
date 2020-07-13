package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartEntity;
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

@Data
@Entity
@Builder
@Table(name = "DiagnosticReport_V2", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiagnosticReportEntity implements DatamartEntity {
  @Id
  @Column(name = "CDWId")
  @EqualsAndHashCode.Include
  private String cdwId;

  @Column(name = "PatientFullICN")
  private String icn;

  @Column(name = "Category")
  private String category;

  @Column(name = "Code")
  private String code;

  @Column(name = "DateUTC")
  private Instant dateUtc;

  @Column(name = "LastUpdated")
  private Instant lastUpdated;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "DiagnosticReport")
  private String payload;

  static Sort naturalOrder() {
    return Sort.by("cdwId").ascending();
  }

  DatamartDiagnosticReport asDatamartDiagnosticReport() {
    return deserializeDatamart(payload, DatamartDiagnosticReport.class);
  }
}
