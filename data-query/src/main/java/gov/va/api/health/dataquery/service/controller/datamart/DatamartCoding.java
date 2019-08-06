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
public final class DatamartCoding {

  private Optional<String> system;

  private Optional<String> code;

  private Optional<String> display;

  /** Creates a second builder that does not take Optionals. */
  @Builder(builderMethodName = "of", builderClassName = "DatamartReferenceOfBuilder")
  private DatamartCoding(String system, String code, String display) {
    this(Optional.ofNullable(system), Optional.ofNullable(code), Optional.ofNullable(display));
  }

  /** Lazy initialization with empty. */
  public Optional<String> code() {
    if (code == null) {
      code = Optional.empty();
    }
    return code;
  }

  /** Lazy initialization with empty. */
  public Optional<String> display() {
    if (display == null) {
      display = Optional.empty();
    }
    return display;
  }

  /** Return true if system, code, or display is set. */
  public boolean hasAnyValue() {
    return system().isPresent() || code().isPresent() || display().isPresent();
  }

  /** Lazy initialization with empty. */
  public Optional<String> system() {
    if (system == null) {
      system = Optional.empty();
    }
    return system;
  }
}
