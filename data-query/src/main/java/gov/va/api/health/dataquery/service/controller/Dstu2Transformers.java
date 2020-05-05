package gov.va.api.health.dataquery.service.controller;

import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

import com.google.common.base.Splitter;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/** Utility methods for transforming CDW results to Argonaut. */
@Slf4j
@UtilityClass
public final class Dstu2Transformers {
  /**
   * Convert the coding to a FHIR coding and wrap it with a codeable concept. Returns null of it
   * cannot be converted.
   */
  public static CodeableConcept asCodeableConceptWrapping(DatamartCoding coding) {
    Coding fhirCoding = asCoding(coding);
    if (fhirCoding == null) {
      return null;
    }
    return CodeableConcept.builder().coding(List.of(fhirCoding)).build();
  }

  /** Convert the Optional datamart coding to Fhir, otherwise return null. */
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

  /** Convert the reference (if specified) to a Datamart reference. */
  public static DatamartReference asDatamartReference(Reference maybeReference) {
    if (maybeReference == null || StringUtils.isBlank(maybeReference.reference())) {
      return null;
    }
    List<String> splitReference = Splitter.on('/').splitToList(maybeReference.reference());
    if (splitReference.size() <= 1) {
      return null;
    }
    String resourceName = splitReference.get(splitReference.size() - 2);
    if (StringUtils.isBlank(resourceName)) {
      return null;
    }
    if (!StringUtils.isAllUpperCase(resourceName.substring(0, 1))) {
      return null;
    }
    String resourceId = splitReference.get(splitReference.size() - 1);
    if (StringUtils.isBlank(resourceId)) {
      return null;
    }
    return DatamartReference.builder()
        .display(Optional.ofNullable(maybeReference.display()))
        .reference(Optional.of(resourceId))
        .type(Optional.of(resourceName))
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

  /** Get the reference id from the given reference. */
  public static String asReferenceId(Reference maybeReference) {
    DatamartReference maybeDatamart = asDatamartReference(maybeReference);
    if (maybeDatamart == null) {
      return null;
    }
    return maybeDatamart.reference().get();
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
