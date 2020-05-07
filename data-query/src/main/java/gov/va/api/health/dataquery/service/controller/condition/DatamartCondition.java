package gov.va.api.health.dataquery.service.controller.condition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.datamart.HasReplaceableId;
import java.time.Instant;
import java.time.LocalDate;
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
public class DatamartCondition implements HasReplaceableId {
  @Builder.Default private String objectType = "Condition";

  @Builder.Default private String objectVersion = "1";

  private String cdwId;

  private DatamartReference patient;

  private Optional<DatamartReference> encounter;

  private Optional<DatamartReference> asserter;

  private Optional<LocalDate> dateRecorded;

  private Optional<SnomedCode> snomed;

  private Optional<IcdCode> icd;

  private Category category;

  private ClinicalStatus clinicalStatus;

  private Optional<Instant> onsetDateTime;

  private Optional<Instant> abatementDateTime;

  /** Lazy initialization with empty. */
  public Optional<Instant> abatementDateTime() {
    if (abatementDateTime == null) {
      abatementDateTime = Optional.empty();
    }
    return abatementDateTime;
  }

  /** Lazy initialization with empty. */
  public Optional<DatamartReference> asserter() {
    if (asserter == null) {
      asserter = Optional.empty();
    }
    return asserter;
  }

  /** Lazy initialization with empty. */
  public Optional<LocalDate> dateRecorded() {
    if (dateRecorded == null) {
      dateRecorded = Optional.empty();
    }
    return dateRecorded;
  }

  /** Lazy initialization with empty. */
  public Optional<DatamartReference> encounter() {
    if (encounter == null) {
      encounter = Optional.empty();
    }
    return encounter;
  }

  /** Determine if there is a valid icd code. */
  @JsonIgnore
  public boolean hasIcdCode() {
    return icd().isPresent() && icd().get().isUsable();
  }

  /** Determine if there is a valid snomed code. */
  @JsonIgnore
  public boolean hasSnomedCode() {
    return snomed().isPresent() && snomed().get().isUsable();
  }

  /** Lazy initialization with empty. */
  public Optional<IcdCode> icd() {
    if (icd == null) {
      icd = Optional.empty();
    }
    return icd;
  }

  /** Lazy initialization with empty. */
  public Optional<Instant> onsetDateTime() {
    if (onsetDateTime == null) {
      onsetDateTime = Optional.empty();
    }
    return onsetDateTime;
  }

  /** Backwards compatibility for etlDate. */
  @SuppressWarnings("unused")
  private void setEtlDate(String unused) {
    /* no op */
  }

  /** Lazy initialization with empty. */
  public Optional<SnomedCode> snomed() {
    if (snomed == null) {
      snomed = Optional.empty();
    }
    return snomed;
  }

  public enum ClinicalStatus {
    active,
    resolved
  }

  public enum Category {
    diagnosis,
    problem
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonIgnoreProperties({"usable"})
  public static class IcdCode {
    private String code;

    private String display;

    private String version;

    /** Determine if an icd code can be mapped without null values. */
    public boolean isUsable() {
      return code != null && display != null;
    }
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonIgnoreProperties({"usable"})
  public static class SnomedCode {
    private String code;

    private String display;

    /** Determine if a snomed code can be mapped without null values. */
    public boolean isUsable() {
      return code != null && display != null;
    }
  }
}
