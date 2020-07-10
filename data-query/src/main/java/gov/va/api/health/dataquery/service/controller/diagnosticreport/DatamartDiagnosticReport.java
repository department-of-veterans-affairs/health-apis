package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartDiagnosticReport {
  @Builder.Default private String objectType = "DiagnosticReport";

  @Builder.Default private int objectVersion = 2;

  private String cdwId;

  private DatamartReference patient;

  private String sta3n;

  private String effectiveDateTime;

  private String issuedDateTime;

  @Builder.Default private Optional<DatamartReference> accessionInstitution = Optional.empty();

  @Builder.Default private Optional<DatamartReference> verifyingStaff = Optional.empty();

  @Builder.Default private Optional<DatamartReference> topography = Optional.empty();

  @Builder.Default private Optional<DatamartReference> visit = Optional.empty();

  @Builder.Default private List<DatamartReference> orders = new ArrayList<>();

  @Builder.Default private List<DatamartReference> results = new ArrayList<>();

  private String reportStatus;
}
