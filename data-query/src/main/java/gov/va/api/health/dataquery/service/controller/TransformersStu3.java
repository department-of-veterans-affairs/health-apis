package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.stu3.api.elements.Reference;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/** Utility methods for STU3 transformers. */
@UtilityClass
public final class TransformersStu3 {
  /**
   * Return false if at least one value in the given list is a non-blank string, or a non-null
   * object.
   */
  public static boolean allBlank(Object... values) {
    for (Object v : values) {
      if (!isBlank(v)) {
        return false;
      }
    }
    return true;
  }

  /** Convert the datamart reference (if specified) to a FHIR reference. */
  public static Reference asReference(Optional<DatamartReference> maybeReference) {
    if (maybeReference == null || maybeReference.isEmpty()) {
      return null;
    }
    return asReference(maybeReference.get());
  }

  /** Convert the datamart reference (if specified) to a FHIR reference. */
  public static Reference asReference(DatamartReference maybeReference) {
    if (maybeReference == null) {
      return null;
    }
    Optional<String> path = maybeReference.asRelativePath();
    if (maybeReference.display().isEmpty() && path.isEmpty()) {
      return null;
    }
    return Reference.builder()
        .display(maybeReference.display().orElse(null))
        .reference(path.orElse(null))
        .build();
  }

  /** Return true if the value is a blank string, or any other object that is null. */
  public static boolean isBlank(Object value) {
    if (value instanceof CharSequence) {
      return StringUtils.isBlank((CharSequence) value);
    }
    if (value instanceof Collection<?>) {
      return ((Collection<?>) value).isEmpty();
    }
    if (value instanceof Map<?, ?>) {
      return ((Map<?, ?>) value).isEmpty();
    }
    return value == null;
  }
}
