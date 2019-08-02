package gov.va.api.health.dataquery.service.controller;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * This utility provides the two-way translation of resource names from FHIR to Identity Service
 * based a set of rules that convert from CamelCaseNames of FHIR to UPPERCASE_NAMES of Identity
 * Service.
 *
 * <pre>
 * Patient -> PATIENT
 * AllergyIntolerance -> ALLERGY_INTOLERANCE
 * </pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceNameTranslation {

  private static final ResourceNameTranslation INSTANCE = new ResourceNameTranslation();

  /** Return a sharable, thread safe instance. */
  public static ResourceNameTranslation get() {
    return INSTANCE;
  }

  /**
   * Given a FHIR name, magically turn it into an Identity Service name. For example,
   *
   * <pre>
   * Patient -> PATIENT
   * AllergyIntolerance -> ALLERGY_INTOLERANCE
   * </pre>
   */
  public String fhirToIdentityService(String fhirName) {
    return Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(fhirName))
        .map(s -> s.toUpperCase(Locale.ENGLISH))
        .collect(Collectors.joining("_"));
  }

  /**
   * Given an Identity Service name, magically turn it into a FHIR name. For example,
   *
   * <pre>
   * PATIENT -> Patient
   * ALLERGY_INTOLERANCE -> AllergyIntolerance
   * </pre>
   */
  public String identityServiceToFhir(String identityServiceName) {
    return Arrays.stream(StringUtils.splitByWholeSeparator(identityServiceName, "_"))
        .map(s -> StringUtils.capitalize(s.toLowerCase(Locale.ENGLISH)))
        .collect(Collectors.joining());
  }
}
