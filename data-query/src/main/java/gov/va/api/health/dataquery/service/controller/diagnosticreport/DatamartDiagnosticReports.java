package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartDiagnosticReports {
  private String objectType;

  private int objectVersion;

  private String fullIcn;

  private String patientName;

  @JsonProperty("DiagnosticReports")
  private List<DiagnosticReport> reports;

  /** Lazy getter. */
  public List<DiagnosticReport> reports() {
    if (reports == null) {
      reports = new ArrayList<>();
    }
    return reports;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static final class DiagnosticReport {
    private String identifier;

    private String sta3n;

    private String effectiveDateTime;

    private String issuedDateTime;

    private String accessionInstitutionSid;

    private String accessionInstitutionName;

    private String topographySid;

    private String topographyName;

    private String visitSid;

    private String visitCategory;

    private List<Order> orders;

    private List<Result> results;

    private String reportStatus;

    public List<Order> orders() {
      if (orders == null) {
        orders = new ArrayList<>();
      }
      return orders;
    }

    public List<Result> results() {
      if (results == null) {
        results = new ArrayList<>();
      }
      return results;
    }

    /** Backwards compatibility for etlDate. */
    @JsonProperty("ETLEditDateTime")
    private void setEtlDate(String unused) {
      /* no op */
    }
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static final class Order {
    private String sid;

    private String display;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static final class Result {
    private String result;

    private String display;
  }
}
