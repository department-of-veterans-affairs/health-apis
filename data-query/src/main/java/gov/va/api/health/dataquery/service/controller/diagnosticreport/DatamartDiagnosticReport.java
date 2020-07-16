package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.datamart.HasReplaceableId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartDiagnosticReport implements HasReplaceableId {
  @Builder.Default private String objectType = "DiagnosticReport";

  @Builder.Default private int objectVersion = 2;

  private String cdwId;

  private DatamartReference patient;

  private String sta3n;

  private String effectiveDateTime;

  private String issuedDateTime;

  private Optional<DatamartReference> accessionInstitution;

  private Optional<DatamartReference> verifyingStaff;

  private Optional<DatamartReference> topography;

  private Optional<DatamartReference> visit;

  private List<DatamartReference> orders;

  private List<DatamartReference> results;

  private String reportStatus;

  /** Lazy getter. */
  public Optional<DatamartReference> accessionInstitution() {
    if (accessionInstitution == null) {
      accessionInstitution = Optional.empty();
    }
    return accessionInstitution;
  }

  /** Lazy Getter. */
  public List<DatamartReference> orders() {
    if (orders == null) {
      orders = new ArrayList<>();
    }
    return orders;
  }

  /** Lazy Getter. */
  public List<DatamartReference> results() {
    if (results == null) {
      results = new ArrayList<>();
    }
    return results;
  }

  /** Lazy getter. */
  public Optional<DatamartReference> topography() {
    if (topography == null) {
      topography = Optional.empty();
    }
    return topography;
  }

  /** Lazy getter. */
  public Optional<DatamartReference> verifyingStaff() {
    if (verifyingStaff == null) {
      verifyingStaff = Optional.empty();
    }
    return verifyingStaff;
  }

  /** Lazy getter. */
  public Optional<DatamartReference> visit() {
    if (visit == null) {
      visit = Optional.empty();
    }
    return visit;
  }
}
