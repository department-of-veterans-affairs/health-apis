package gov.va.api.health.dataquery.service.controller.allergyintolerance;

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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartAllergyIntolerance implements HasReplaceableId {

  private String objectType;

  private int objectVersion;

  private String cdwId;

  private String etlDate;

  private Optional<DatamartReference> patient;

  private Optional<Instant> recordedDate;

  private Optional<DatamartReference> recorder;

  private Optional<Substance> substance;

  private Status status;

  private Type type;

  private Category category;

  private List<Note> notes;

  private Optional<Reaction> reactions;

  /** Lazy getter. */
  public List<Note> notes() {
    if (notes == null) {
      notes = new ArrayList<>();
    }
    return notes;
  }

  /** Lazy getter. */
  public Optional<DatamartReference> patient() {
    if (patient == null) {
      patient = Optional.empty();
    }
    return patient;
  }

  /** Lazy getter. */
  public Optional<Reaction> reactions() {
    if (reactions == null) {
      reactions = Optional.empty();
    }
    return reactions;
  }

  /** Lazy getter. */
  public Optional<Instant> recordedDate() {
    if (recordedDate == null) {
      recordedDate = Optional.empty();
    }
    return recordedDate;
  }

  /** Lazy getter. */
  public Optional<DatamartReference> recorder() {
    if (recorder == null) {
      recorder = Optional.empty();
    }
    return recorder;
  }

  /** Lazy getter. */
  public Optional<Substance> substance() {
    if (substance == null) {
      substance = Optional.empty();
    }
    return substance;
  }

  public enum Category {
    medication,
    food
  }

  public enum Certainty {
    likely,
    unlikely,
    confirmed
  }

  public enum Status {
    active,
    unconfirmed,
    confirmed,
    inactive,
    resolved,
    refuted,
    @JsonProperty("entered-in-error")
    entered_in_error
  }

  public enum Type {
    allergy,
    intolerance
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static final class Coding {

    private String system;

    private String code;

    private String display;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static final class Note {

    private String text;

    private Optional<Instant> time;

    private Optional<DatamartReference> practitioner;

    /** Lazy getter. */
    public Optional<DatamartReference> practitioner() {
      if (practitioner == null) {
        practitioner = Optional.empty();
      }
      return practitioner;
    }

    public void setReferencePractitionerId(String id) {
      if (practitioner().isEmpty()) {
        practitioner(
            Optional.of(DatamartReference.builder().type(Optional.of("Practitioner")).build()));
      }
      practitioner().get().reference(Optional.of(id));
    }

    public void setReferencePractitionerName(String name) {
      if (practitioner().isEmpty()) {
        practitioner(
            Optional.of(DatamartReference.builder().type(Optional.of("Practitioner")).build()));
      }
      practitioner().get().display(Optional.of(name));
    }

    /** Lazy getter. */
    public Optional<Instant> time() {
      if (time == null) {
        time = Optional.empty();
      }
      return time;
    }
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static final class Reaction {

    private Certainty certainty;

    private List<Coding> manifestations;

    /** Lazy getter. */
    public List<Coding> manifestations() {
      if (manifestations == null) {
        manifestations = new ArrayList<>();
      }
      return manifestations;
    }
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static final class Substance {

    private Optional<Coding> coding;

    private String text;

    /** Lazy getter. */
    public Optional<Coding> coding() {
      if (coding == null) {
        coding = Optional.empty();
      }
      return coding;
    }
  }
}
