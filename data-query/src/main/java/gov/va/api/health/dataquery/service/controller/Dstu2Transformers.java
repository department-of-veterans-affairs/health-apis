package gov.va.api.health.dataquery.service.controller;

import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.NonNull;
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
    String[] splitReference = maybeReference.reference().split("/");
    if (splitReference.length <= 1) {
      return null;
    }
    String resourceName = splitReference[splitReference.length - 2];
    if (StringUtils.isBlank(resourceName)) {
      return null;
    }
    if (!StringUtils.isAllUpperCase(resourceName.substring(0, 1))) {
      return null;
    }
    String resourceId = splitReference[splitReference.length - 1];
    if (StringUtils.isBlank(resourceId)) {
      return null;
    }
    return DatamartReference.builder()
        .display(Optional.ofNullable(maybeReference.display()))
        .reference(Optional.of(resourceId))
        .type(Optional.of(resourceName))
        .build();
  }

  /** Return null if the date is null, otherwise return ands ISO-8601 date. */
  public static String asDateString(XMLGregorianCalendar maybeDate) {
    if (maybeDate == null) {
      return null;
    }
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    return formatter.format(maybeDate.toGregorianCalendar().getTime());
  }

  /** Return null if the date is null, otherwise return an ISO-8601 date. */
  public static String asDateString(Optional<LocalDate> maybeDateTime) {
    if (maybeDateTime == null) {
      return null;
    }
    return asDateString(maybeDateTime.orElse(null));
  }

  /** Return null if the date is null, otherwise return an ISO-8601 date. */
  public static String asDateString(LocalDate maybeDateTime) {
    if (maybeDateTime == null) {
      return null;
    }
    return DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
        .withZone(ZoneOffset.UTC)
        .format(maybeDateTime);
  }

  /** Return null if the date is null, otherwise return an ISO-8601 date time. */
  public static String asDateTimeString(Optional<Instant> maybeDateTime) {
    if (maybeDateTime == null) {
      return null;
    }
    return asDateTimeString(maybeDateTime.orElse(null));
  }

  /** Return null if the date is null, otherwise return an ISO-8601 date time. */
  public static String asDateTimeString(Instant maybeDateTime) {
    if (maybeDateTime == null) {
      return null;
    }
    return maybeDateTime.toString();
  }

  /** Return null if the date is null, otherwise return an ISO-8601 date time. */
  public static String asDateTimeString(XMLGregorianCalendar maybeDateTime) {
    if (maybeDateTime == null) {
      return null;
    }
    return maybeDateTime.toString();
  }

  /** Return null if the big integer is null, otherwise return the value as an integer. */
  public static Integer asInteger(BigInteger maybeBigInt) {
    if (maybeBigInt == null) {
      return null;
    }
    return maybeBigInt.intValue();
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

  /** Return null if the given object is null, otherwise return the converted value. */
  public static <T, R> R convert(T source, Function<T, R> mapper) {
    if (source == null) {
      return null;
    }
    return mapper.apply(source);
  }

  /**
   * Return null if the source list is null or empty, otherwise convert the items in the list and
   * return a new one.
   */
  public static <T, R> List<R> convertAll(List<T> source, Function<T, R> mapper) {
    if (isEmpty(source)) {
      return null;
    }
    List<R> probablyItems =
        source.stream().map(mapper).filter(Objects::nonNull).collect(Collectors.toList());
    return probablyItems.isEmpty() ? null : probablyItems;
  }

  /** Throw a MissingPayload exception if the list does not have at least 1 item. */
  public static <T> T firstPayloadItem(@NonNull List<T> items) {
    if (items.isEmpty()) {
      throw new MissingPayload();
    }
    return items.get(0);
  }

  /** Throw a MissingPayload exception if the value is null. */
  public static <T> T hasPayload(T value) {
    if (value == null) {
      throw new MissingPayload();
    }
    return value;
  }

  /**
   * Return the result of the given extractor function if the given object is present. The object
   * will be passed to the apply method of the extractor function.
   *
   * <p>Consider this example:
   *
   * <pre>
   * ifPresent(patient.getGender(), gender -> Patient.Gender.valueOf(gender.value()))
   * </pre>
   *
   * This is equivalent to this standard Java code.
   *
   * <pre>
   * Gender gender = patient.getGender();
   * if (gender == null) {
   *   return null;
   * } else {
   *   return Patient.Gender.valueOf(gender.value());
   * }
   * </pre>
   */
  public static <T, R> R ifPresent(T object, Function<T, R> extract) {
    if (object == null) {
      return null;
    }
    return extract.apply(object);
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

  /**
   * Indicates the CDW payload is missing, but no errors were reported. This exception indicates
   * there is a bug in CDW.
   */
  static class MissingPayload extends TransformationException {

    MissingPayload() {
      super(
          "Payload is missing, but no errors reported."
              + " This can occur when the resource is registered with the identity service"
              + " but the database returns an empty search result.");
    }
  }

  /** Base exception for controller errors. */
  static class TransformationException extends RuntimeException {

    @SuppressWarnings("SameParameterValue")
    TransformationException(String message) {
      super(message);
    }
  }
}
