package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
@Table(name = "app.DiagnosticReport_PatientList")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiagnosticReportCrossEntity {
  @Id
  @EqualsAndHashCode.Include
  @Column(name = "Identifier")
  private String reportId;

  @Column(name = "PatientFullICN")
  private String icn;
}
