package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "app.DiagnosticReport")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DiagnosticReportEntity {
  @Id private long id;

  private String identifier;

  private long sid;

  private long patientId;

  private String category;

  private Instant startedDtg;

  private Instant endedDtg;

  private String code;

  private String document;
}
