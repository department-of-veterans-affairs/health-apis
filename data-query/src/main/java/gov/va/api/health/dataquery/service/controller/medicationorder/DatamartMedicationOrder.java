package gov.va.api.health.dataquery.service.controller.medicationorder;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.datamart.HasReplaceableId;
import java.time.Instant;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartMedicationOrder implements HasReplaceableId {

  @Builder.Default private String objectType = "MedicationOrder";

  @Builder.Default private String objectVersion = "1";

  private String cdwId;

  private DatamartReference patient;

  private Instant dateWritten;

  private Status status;

  private Optional<Instant> dateEnded;

  private DatamartReference prescriber;

  private DatamartReference medication;

  private List<DosageInstruction> dosageInstruction;

  private Optional<DispenseRequest> dispenseRequest;

  /** Lazy initialization with empty. */
  public Optional<Instant> dateEnded() {
    if (dateEnded == null) {
      return Optional.empty();
    }
    return dateEnded;
  }

  /** Lazy Inititialization with empty. */
  public Optional<DispenseRequest> dispenseRequest() {
    if (dispenseRequest == null) {
      return Optional.empty();
    }
    return dispenseRequest;
  }

  /** Lazy Getter. */
  public List<DosageInstruction> dosageInstruction() {
    if (dosageInstruction == null) {
      return new ArrayList<>();
    }
    return dosageInstruction;
  }

  /** Backwards compatibility for etlDate. */
  private void setEtlDate(String unused) {
    /* no op */
  }

  public enum Status {
    completed,
    stopped,
    @JsonProperty(value = "on-hold")
    on_hold,
    active,
    draft,
    @JsonProperty(value = "entered-in-error")
    entered_in_error
  }

  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  static final class DosageInstruction {

    private Optional<String> dosageText;

    private Optional<String> timingText;

    private Optional<String> additionalInstructions;

    private boolean asNeeded;

    private Optional<String> routeText;

    private Optional<Double> doseQuantityValue;

    private Optional<String> doseQuantityUnit;

    /** Lazy initialization with empty. */
    public Optional<String> additionalInstructions() {
      if (additionalInstructions == null) {
        return Optional.empty();
      }
      return additionalInstructions;
    }

    /** Lazy initialization with empty. */
    public Optional<String> dosageText() {
      if (dosageText == null) {
        return Optional.empty();
      }
      return dosageText;
    }

    /** Lazy initialization with empty. */
    public Optional<String> doseQuantityUnit() {
      if (doseQuantityUnit == null) {
        return Optional.empty();
      }
      return doseQuantityUnit;
    }

    /** Lazy initialization with empty. */
    public Optional<Double> doseQuantityValue() {
      if (doseQuantityValue == null) {
        return Optional.empty();
      }
      return doseQuantityValue;
    }

    /** Lazy initialization with empty. */
    public Optional<String> routeText() {
      if (routeText == null) {
        return Optional.empty();
      }
      return routeText;
    }

    /** Lazy initialization with empty. */
    public Optional<String> timingText() {
      if (timingText == null) {
        return Optional.empty();
      }
      return timingText;
    }
  }

  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  static final class DispenseRequest {

    private Optional<Integer> numberOfRepeatsAllowed;

    private Optional<Double> quantity;

    private Optional<String> unit;

    private Optional<Integer> expectedSupplyDuration;

    private Optional<String> supplyDurationUnits;

    /** Lazy initialization with empty. */
    public Optional<Integer> expectedSupplyDuration() {
      if (expectedSupplyDuration == null) {
        return Optional.empty();
      }
      return expectedSupplyDuration;
    }

    /** Lazy initialization with empty. */
    public Optional<Integer> numberOfRepeatsAllowed() {
      if (numberOfRepeatsAllowed == null) {
        return Optional.empty();
      }
      return numberOfRepeatsAllowed;
    }

    /** Lazy initialization with empty. */
    public Optional<Double> quantity() {
      if (quantity == null) {
        return Optional.empty();
      }
      return quantity;
    }

    /** Lazy initialization with empty. */
    public Optional<String> supplyDurationUnits() {
      if (supplyDurationUnits == null) {
        return Optional.empty();
      }
      return supplyDurationUnits;
    }

    /** Lazy initialization with empty. */
    public Optional<String> unit() {
      if (unit == null) {
        return Optional.empty();
      }
      return unit;
    }
  }
}
