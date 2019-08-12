package gov.va.api.health.dataquery.service.controller.procedure;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.datamart.HasReplaceableId;
import java.time.Instant;
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
public class DatamartProcedure implements HasReplaceableId {

  @Builder.Default private String objectType = "Procedure";

  @Builder.Default private String objectVersion = "1";

  private String cdwId;

  private DatamartReference patient;

  private Status status;

  private DatamartCoding coding;

  private boolean notPerformed;

  private Optional<String> reasonNotPerformed;

  private Optional<Instant> performedDateTime;

  private Optional<DatamartReference> encounter;

  private Optional<DatamartReference> location;

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
  public Optional<Instant> performedDateTime() {
    if (performedDateTime == null) {
      performedDateTime = Optional.empty();
    }
    return performedDateTime;
  }

  /** Lazy initialization with empty. */
  public Optional<String> reasonNotPerformed() {
    if (reasonNotPerformed == null) {
      reasonNotPerformed = Optional.empty();
    }
    return reasonNotPerformed;
  }

  /** Backwards compatibility for etlDate. */
  private void setEtlDate(String unused) {
    /* no op */
  }

  public enum Status {
    @JsonProperty("in-progress")
    in_progress,
    aborted,
    completed
  }
}
