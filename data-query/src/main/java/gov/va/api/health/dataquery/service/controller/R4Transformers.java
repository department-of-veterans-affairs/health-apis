package gov.va.api.health.dataquery.service.controller;

import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.r4.api.elements.Reference;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/** Utility methods for r4 transformers. */
@Slf4j
@UtilityClass
public class R4Transformers {

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

  /**
   * Parse an Instant from a string such as '2007-12-03T10:15:30Z', appending 'Z' if it is missing.
   */
  public static Instant parseInstant(String instant) {
    try {
      String zoned = endsWithIgnoreCase(instant, "Z") ? instant : instant + "Z";
      return Instant.parse(zoned);
    } catch (DateTimeParseException e) {
      log.error("Failed to parse '{}' as instant", instant);
      return null;
    }
  }
}
