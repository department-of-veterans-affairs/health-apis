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

  private Instant effectiveDateTime;

  private Instant issuedDateTime;

  private String code;

  private String document;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof DiagnosticReportEntity)) {
      return false;
    }
    DiagnosticReportEntity other = (DiagnosticReportEntity) obj;
    if (id != other.id) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return 31 + (int) (id ^ (id >>> 32));
  }
}
