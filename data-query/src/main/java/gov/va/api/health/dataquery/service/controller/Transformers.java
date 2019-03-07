package gov.va.api.health.dataquery.service.controller;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/** Utility methods for transforming CDW results to Argonaut. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

  /** Return null if the date is null, otherwise return ands ISO-8601 date. */
  public static String asDateString(XMLGregorianCalendar maybeDate) {
    if (maybeDate == null) {
      return null;
    }
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    return formatter.format(maybeDate.toGregorianCalendar().getTime());
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
    if (source == null || source.isEmpty()) {
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

  /** Return true if the value is a blank string, or any other object that is null. */
  public static boolean isBlank(Object value) {
    if (value instanceof CharSequence) {
      return StringUtils.isBlank((CharSequence) value);
    }
    return value == null;
  }

  /**
   * Indicates the CDW payload is missing, but no errors were reported. This exception indicates
   * there is a bug in CDW, Mr. Anderson, or the Mr. Anderson client.
   */
  static class MissingPayload extends TransformationException {
    MissingPayload() {
      super(
          "Payload is missing, but no errors reported by Mr. Anderson."
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
