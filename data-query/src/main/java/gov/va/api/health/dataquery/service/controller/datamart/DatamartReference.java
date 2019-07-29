package gov.va.api.health.dataquery.service.controller.datamart;

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
public class DatamartReference {
  private Optional<String> type;
  private Optional<String> reference;
  private Optional<String> display;

  /** Creates a second builder that does not take Optionals. */
  @Builder(builderMethodName = "of", builderClassName = "DatamartReferenceOfBuilder")
  private DatamartReference(String type, String reference, String display) {
    this(Optional.ofNullable(type), Optional.ofNullable(reference), Optional.ofNullable(display));
  }

  /** Lazy initialization with empty. */
  public Optional<String> display() {
    if (display == null) {
      display = Optional.empty();
    }
    return display;
  }

  /** Lazy initialization with empty. */
  public Optional<String> reference() {
    if (reference == null) {
      reference = Optional.empty();
    }
    return reference;
  }

  /** Lazy initialization with empty. */
  public Optional<String> type() {
    if (type == null) {
      type = Optional.empty();
    }
    return type;
  }
}
