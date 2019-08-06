package gov.va.api.health.dataquery.service.controller.datamart;

import gov.va.api.health.dataquery.service.controller.ResourceNameTranslation;
import gov.va.api.health.ids.api.ResourceIdentity;
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

  /** Return "type/reference" if both are available, otherwise return empty. */
  public Optional<String> asRelativePath() {
    if (!hasTypeAndReference()) {
      return Optional.empty();
    }
    return Optional.of(type().get() + "/" + reference().get());
  }

  /** Return a ResourceIdentity if the type and reference fields are available. */
  public Optional<ResourceIdentity> asResourceIdentity() {
    if (!hasTypeAndReference()) {
      return Optional.empty();
    }
    return Optional.of(
        ResourceIdentity.builder()
            .system("CDW")
            .resource(ResourceNameTranslation.get().fhirToIdentityService(type().get()))
            .identifier(reference().get())
            .build());
  }

  /** Lazy initialization with empty. */
  public Optional<String> display() {
    if (display == null) {
      display = Optional.empty();
    }
    return display;
  }

  public boolean hasTypeAndReference() {
    return type().isPresent() && reference().isPresent();
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
