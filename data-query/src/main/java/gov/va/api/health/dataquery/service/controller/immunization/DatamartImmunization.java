package gov.va.api.health.dataquery.service.controller.immunization;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.va.api.lighthouse.datamart.DatamartReference;
import gov.va.api.lighthouse.datamart.HasReplaceableId;
import java.time.Instant;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datarmart JSON model. */
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartImmunization implements HasReplaceableId {
  @Builder.Default private String objectType = "Immunization";

  @Builder.Default private String objectVersion = "1";

  private String cdwId;

  private Status status;

  private Instant date;

  private VaccineCode vaccineCode;

  private DatamartReference patient;

  private boolean wasNotGiven;

  private Optional<DatamartReference> performer;

  private Optional<DatamartReference> requester;

  private Optional<DatamartReference> encounter;

  private Optional<DatamartReference> location;

  private Optional<String> note;

  private Optional<DatamartReference> reaction;

  private Optional<VaccinationProtocols> vaccinationProtocols;

  /** Lazy initialization with empty. */
  public Optional<DatamartReference> encounter() {
    if (encounter == null) {
      encounter = Optional.empty();
    }
    return encounter;
  }

  /** Lazy initialization with empty. */
  public Optional<DatamartReference> location() {
    if (location == null) {
      location = Optional.empty();
    }
    return location;
  }

  /** Lazy initialization with empty. */
  public Optional<String> note() {
    if (note == null) {
      note = Optional.empty();
    }
    return note;
  }

  /** Lazy initialization with empty. */
  public Optional<DatamartReference> performer() {
    if (performer == null) {
      performer = Optional.empty();
    }
    return performer;
  }

  /** Lazy initialization with empty. */
  public Optional<DatamartReference> reaction() {
    if (reaction == null) {
      reaction = Optional.empty();
    }
    return reaction;
  }

  /** Lazy initialization with empty. */
  public Optional<DatamartReference> requester() {
    if (requester == null) {
      requester = Optional.empty();
    }
    return requester;
  }

  /** Backwards compatibility for etlDate. */
  @SuppressWarnings("unused")
  private void setEtlDate(String unused) {
    /* no op */
  }

  /** Lazy initialization with empty. */
  public Optional<VaccinationProtocols> vaccinationProtocols() {
    if (vaccinationProtocols == null) {
      vaccinationProtocols = Optional.empty();
    }
    return vaccinationProtocols;
  }

  /** status. */
  public enum Status {
    completed,
    @JsonProperty("entered-in-error")
    entered_in_error,
    @JsonProperty("data-absent-reason:unsupported")
    data_absent_reason_unsupported
  }

  /** VaccinationProtocols. */
  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class VaccinationProtocols {
    private Optional<String> series;

    private Optional<Integer> seriesDoses;

    /** Lazy initialization with empty. */
    public Optional<String> series() {
      if (series == null) {
        series = Optional.empty();
      }
      return series;
    }

    /** Lazy initialization with empty. */
    public Optional<Integer> seriesDoses() {
      if (seriesDoses == null) {
        seriesDoses = Optional.empty();
      }
      return seriesDoses;
    }
  }

  /** VaccineCode. */
  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class VaccineCode {
    private String text;

    private String code;
  }
}
