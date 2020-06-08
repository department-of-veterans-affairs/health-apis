package gov.va.api.health.dataquery.service.controller;

import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.google.common.base.Splitter;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.fhir.api.IsReference;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/** Utility methods for transformers that are not specific to one FHIR version. */
@UtilityClass
@Slf4j
public final class Transformers {
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

  /**
   * Converts the given object to a BigDecimal. Currently supports Integer, Double, and Long
   * objects.
   */
  public static BigDecimal asBigDecimal(Optional<?> opt) {
    Object v = opt.orElse(null);
    if (v instanceof Integer) {
      return BigDecimal.valueOf((Integer) v);
    }
    if (v instanceof Double) {
      return BigDecimal.valueOf((Double) v);
    }
    if (v instanceof Long) {
      return BigDecimal.valueOf((Long) v);
    }
    return null;
  }

  /**
   * Convert the reference (if specified) to a Datamart reference. This is looking for any number of
   * path elements, a resource type in all upper case, followed by an ID, i.e.
   * `what/ever/RESOURCE/id`. For example, PATIENT/1234567890V123456
   */
  public static DatamartReference asDatamartReference(IsReference maybeReference) {
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

  /** Get the reference id from the given reference. */
  public static String asReferenceId(IsReference maybeReference) {
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

  /** Filter null items and return null if the result is null or empty. */
  public static <T> List<T> emptyToNull(List<T> items) {
    if (isEmpty(items)) {
      return null;
    }
    List<T> filtered = items.stream().filter(Objects::nonNull).collect(Collectors.toList());
    return filtered.isEmpty() ? null : filtered;
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

  /** Return true if the value is a blank string, or any other object that is null. */
  public static boolean isBlank(Object value) {
    if (value instanceof CharSequence) {
      return StringUtils.isBlank((CharSequence) value);
    }
    if (value instanceof Collection<?>) {
      return ((Collection<?>) value).isEmpty();
    }
    if (value instanceof Optional<?>) {
      return ((Optional<?>) value).isEmpty() || isBlank(((Optional<?>) value).get());
    }
    if (value instanceof Map<?, ?>) {
      return ((Map<?, ?>) value).isEmpty();
    }
    return value == null;
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
