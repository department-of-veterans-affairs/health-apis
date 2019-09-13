package gov.va.api.health.dataquery.service.controller.observation;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartObservation implements HasReplaceableId {
  private String objectType;

  private int objectVersion;

  private String cdwId;

  private Status status;

  private Category category;

  private Optional<CodeableConcept> code;

  private Optional<DatamartReference> subject;

  private Optional<DatamartReference> encounter;

  private Optional<Instant> effectiveDateTime;

  private Optional<Instant> issued;

  private List<DatamartReference> performer;

  private Optional<Quantity> valueQuantity;

  private Optional<CodeableConcept> valueCodeableConcept;

  private String interpretation;

  private String comment;

  private Optional<DatamartReference> specimen;

  private Optional<ReferenceRange> referenceRange;

  private List<VitalsComponent> vitalsComponents;

  private List<AntibioticComponent> antibioticComponents;

  private Optional<BacteriologyComponent> mycobacteriologyComponents;

  private Optional<BacteriologyComponent> bacteriologyComponents;

  /** Lazy getter. */
  public List<AntibioticComponent> antibioticComponents() {
    if (antibioticComponents == null) {
      antibioticComponents = new ArrayList<>();
    }
    return antibioticComponents;
  }

  /** Lazy getter. */
  public Optional<BacteriologyComponent> bacteriologyComponents() {
    if (bacteriologyComponents == null) {
      bacteriologyComponents = Optional.empty();
    }
    return bacteriologyComponents;
  }

  /** Lazy getter. */
  public Optional<CodeableConcept> code() {
    if (code == null) {
      code = Optional.empty();
    }
    return code;
  }

  /** Lazy getter. */
  public Optional<Instant> effectiveDateTime() {
    if (effectiveDateTime == null) {
      effectiveDateTime = Optional.empty();
    }
    return effectiveDateTime;
  }

  /** Lazy getter. */
  public Optional<DatamartReference> encounter() {
    if (encounter == null) {
      encounter = Optional.empty();
    }
    return encounter;
  }

  /** Lazy getter. */
  public Optional<Instant> issued() {
    if (issued == null) {
      issued = Optional.empty();
    }
    return issued;
  }

  /** Lazy getter. */
  public Optional<BacteriologyComponent> mycobacteriologyComponents() {
    if (mycobacteriologyComponents == null) {
      mycobacteriologyComponents = Optional.empty();
    }
    return mycobacteriologyComponents;
  }

  /** Lazy getter. */
  public List<DatamartReference> performer() {
    if (performer == null) {
      performer = new ArrayList<>();
    }
    return performer;
  }

  /** Lazy getter. */
  public Optional<ReferenceRange> referenceRange() {
    if (referenceRange == null) {
      referenceRange = Optional.empty();
    }
    return referenceRange;
  }

  /** Lazy getter. */
  public Optional<DatamartReference> specimen() {
    if (specimen == null) {
      specimen = Optional.empty();
    }
    return specimen;
  }

  /** Lazy getter. */
  public Optional<DatamartReference> subject() {
    if (subject == null) {
      subject = Optional.empty();
    }
    return subject;
  }

  /** Lazy getter. */
  public Optional<CodeableConcept> valueCodeableConcept() {
    if (valueCodeableConcept == null) {
      valueCodeableConcept = Optional.empty();
    }
    return valueCodeableConcept;
  }

  /** Lazy getter. */
  public Optional<Quantity> valueQuantity() {
    if (valueQuantity == null) {
      valueQuantity = Optional.empty();
    }
    return valueQuantity;
  }

  /** Lazy getter. */
  public List<VitalsComponent> vitalsComponents() {
    if (vitalsComponents == null) {
      vitalsComponents = new ArrayList<>();
    }
    return vitalsComponents;
  }

  public enum Category {
    @JsonProperty("social-history")
    social_history,
    @JsonProperty("vital-signs")
    vital_signs,
    imaging,
    laboratory,
    procedure,
    survey,
    exam,
    therapy
  }

  public enum Status {
    registered,
    preliminary,
    @JsonProperty("final")
    _final,
    amended,
    cancelled,
    @JsonProperty("entered-in-error")
    entered_in_error,
    unknown
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static final class AntibioticComponent {
    private String id;

    private String codeText;

    private Optional<CodeableConcept> code;

    private Optional<DatamartCoding> valueCodeableConcept;

    /** Lazy getter. */
    public Optional<CodeableConcept> code() {
      if (code == null) {
        code = Optional.empty();
      }
      return code;
    }

    /** Lazy getter. */
    public Optional<DatamartCoding> valueCodeableConcept() {
      if (valueCodeableConcept == null) {
        valueCodeableConcept = Optional.empty();
      }
      return valueCodeableConcept;
    }
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static final class BacteriologyComponent {
    private Optional<Text> code;

    private Optional<Text> valueText;

    /** Lazy getter. */
    public Optional<Text> code() {
      if (code == null) {
        code = Optional.empty();
      }
      return code;
    }

    /** Lazy getter. */
    public Optional<Text> valueText() {
      if (valueText == null) {
        valueText = Optional.empty();
      }
      return valueText;
    }
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static final class CodeableConcept {
    private Optional<DatamartCoding> coding;

    private String text;

    public Optional<DatamartCoding> coding() {
      if (coding == null) {
        coding = Optional.empty();
      }
      return coding;
    }

    void setCode(String code) {
      if (coding().isEmpty()) {
        coding(Optional.of(DatamartCoding.builder().build()));
      }
      coding().get().code(Optional.ofNullable(code));
    }

    void setDisplay(String display) {
      if (coding().isEmpty()) {
        coding(Optional.of(DatamartCoding.builder().build()));
      }
      coding().get().display(Optional.ofNullable(display));
    }

    void setSystem(String system) {
      if (coding().isEmpty()) {
        coding(Optional.of(DatamartCoding.builder().build()));
      }
      coding().get().system(Optional.ofNullable(system));
    }
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static final class Quantity {
    private Double value;

    private String unit;

    private String system;

    private String code;

    void setUnitCode(String code) {
      code(code);
    }

    void setUnitSystem(String system) {
      system(system);
    }
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static final class ReferenceRange {
    private Optional<Quantity> low;

    private Optional<Quantity> high;

    /** Lazy getter. */
    public Optional<Quantity> high() {
      if (high == null) {
        high = Optional.empty();
      }
      return high;
    }

    /** Lazy getter. */
    public Optional<Quantity> low() {
      if (low == null) {
        low = Optional.empty();
      }
      return low;
    }
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static final class Text {
    private String text;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static final class VitalsComponent {
    private Optional<DatamartCoding> code;

    private Optional<Quantity> valueQuantity;

    /** Lazy getter. */
    public Optional<DatamartCoding> code() {
      if (code == null) {
        code = Optional.empty();
      }
      return code;
    }

    /** Lazy getter. */
    public Optional<Quantity> valueQuantity() {
      if (valueQuantity == null) {
        valueQuantity = Optional.empty();
      }
      return valueQuantity;
    }
  }
}
