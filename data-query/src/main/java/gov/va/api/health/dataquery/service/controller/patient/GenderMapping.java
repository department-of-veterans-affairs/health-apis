package gov.va.api.health.dataquery.service.controller.patient;

import static org.apache.commons.lang3.StringUtils.upperCase;

import java.util.Locale;
import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings("UnnecessaryParentheses")
class GenderMapping {

  String toCdw(String fhir) {
    return switch (upperCase(fhir, Locale.US)) {
      case "MALE" -> "M";
      case "FEMALE" -> "F";
      case "OTHER" -> "*Missing*";
      case "UNKNOWN" -> "*Unknown at this time*";
      default -> null;
    };
  }

  gov.va.api.health.dstu2.api.resources.Patient.Gender toDstu2Fhir(String cdw) {
    return switch (upperCase(cdw, Locale.US)) {
      case "M", "MALE" -> gov.va.api.health.dstu2.api.resources.Patient.Gender.male;
      case "F", "FEMALE" -> gov.va.api.health.dstu2.api.resources.Patient.Gender.female;
      case "*UNKNOWN AT THIS TIME*", "UNKNOWN", "DOES NOT WISH TO DISCLOSE" -> gov.va.api.health
          .dstu2.api.resources.Patient.Gender.unknown;
      default -> gov.va.api.health.dstu2.api.resources.Patient.Gender.other;
    };
  }

  gov.va.api.health.r4.api.resources.Patient.Gender toR4Fhir(String cdw) {
    return switch (upperCase(cdw, Locale.US)) {
      case "M", "MALE" -> gov.va.api.health.r4.api.resources.Patient.Gender.male;
      case "F", "FEMALE" -> gov.va.api.health.r4.api.resources.Patient.Gender.female;
      case "*UNKNOWN AT THIS TIME*", "UNKNOWN", "DOES NOT WISH TO DISCLOSE" -> gov.va.api.health.r4
          .api.resources.Patient.Gender.unknown;
      default -> gov.va.api.health.r4.api.resources.Patient.Gender.other;
    };
  }
}
