package gov.va.api.health.dataquery.service.controller.medicationstatement;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.datamart.HasReplaceableId;
import java.time.Instant;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the document returned from Datamart. All fields are required unless specifically
 * declared as `Optional`. In those cases, the fields will still be populated using
 * `Optional.empty()`
 */
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartMedicationStatement implements HasReplaceableId {
  @Builder.Default private String objectType = "MedicationStatement";
  @Builder.Default private String objectVersion = "1";
  private String cdwId;
  private Instant etlDate;
  private DatamartReference patient;
  private Instant dateAsserted;
  private Status status;
  private Optional<Instant> effectiveDateTime;
  private Optional<String> note;
  private DatamartReference medication;
  private Dosage dosage;

  /** Lazy initialization with empty. */
  public Optional<Instant> effectiveDateTime() {
    if (effectiveDateTime == null) {
      effectiveDateTime = Optional.empty();
    }
    return effectiveDateTime;
  }

  /** Lazy initialization with empty. */
  public Optional<String> note() {
    if (note == null) {
      note = Optional.empty();
    }
    return note;
  }

  public enum Status {
    active,
    completed
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Dosage {
    private Optional<String> text;
    private Optional<String> timingCodeText;
    private Optional<String> routeText;

    /** Lazy initialization with empty. */
    public Optional<String> routeText() {
      if (routeText == null) {
        routeText = Optional.empty();
      }
      return routeText;
    }

    /** Lazy initialization with empty. */
    public Optional<String> text() {
      if (text == null) {
        text = Optional.empty();
      }
      return text;
    }

    /** Lazy initialization with empty. */
    public Optional<String> timingCodeText() {
      if (timingCodeText == null) {
        timingCodeText = Optional.empty();
      }
      return timingCodeText;
    }
  }
}
