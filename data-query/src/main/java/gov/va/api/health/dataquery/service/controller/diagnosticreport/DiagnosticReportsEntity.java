package gov.va.api.health.dataquery.service.controller.diagnosticreport;

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

@Data
@Entity
@Builder
@Table(name = "DiagnosticReport", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiagnosticReportsEntity implements DatamartEntity {
  @Id
  @Column(name = "PatientFullIcn")
  @EqualsAndHashCode.Include
  private String icn;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "DiagnosticReport")
  private String payload;

  DatamartDiagnosticReports asDatamartDiagnosticReports() {
    return deserializeDatamart(payload, DatamartDiagnosticReports.class);
  }

  @Override
  public String cdwId() {
    return icn();
  }
}
