package gov.va.api.health.dataquery.service.controller;

import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Reference;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/** Utility methods for r4 transformers. */
@Slf4j
@UtilityClass
public class R4Transformers {
  /**
   * Convert the coding to a FHIR coding and wrap it in a codeable concept. Returns null if it
   * cannot be converted.
   */
  public static CodeableConcept asCodeableConceptWrapping(DatamartCoding coding) {
    Coding fhirCoding = asCoding(coding);
    if (fhirCoding == null) {
      return null;
    }
    return CodeableConcept.builder().coding(List.of(fhirCoding)).build();
  }

  /**
   * Convert the optional coding to a FHIR coding and wrap it in a codeable concept. Returns null if
   * it cannot be converted.
   */
  public static CodeableConcept asCodeableConceptWrapping(Optional<DatamartCoding> coding) {
    if (coding == null || coding.isEmpty()) {
      return null;
    }
    return CodeableConcept.builder().coding(List.of(asCoding(coding.get()))).build();
  }

  /** Convert the datamart coding to coding if possible, otherwise return null. */
  public static Coding asCoding(Optional<DatamartCoding> maybeCoding) {
    if (maybeCoding == null || maybeCoding.isEmpty()) {
      return null;
    }
    return asCoding(maybeCoding.get());
  }

  /** Convert the datamart coding to coding if possible, otherwise return null. */
  public static Coding asCoding(DatamartCoding datamartCoding) {
    if (datamartCoding == null || !datamartCoding.hasAnyValue()) {
      return null;
    }
    return Coding.builder()
        .system(datamartCoding.system().orElse(null))
        .code(datamartCoding.code().orElse(null))
        .display(datamartCoding.display().orElse(null))
        .build();
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
