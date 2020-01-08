package gov.va.api.health.dataquery.service.controller;

import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.stu3.api.datatypes.CodeableConcept;
import gov.va.api.health.stu3.api.datatypes.Coding;
import gov.va.api.health.stu3.api.elements.Reference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

/** Utility methods for STU3 transformers. */
@UtilityClass
public final class Stu3Transformers {

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
}
