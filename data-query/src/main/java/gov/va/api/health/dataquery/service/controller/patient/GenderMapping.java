package gov.va.api.health.dataquery.service.controller.patient;

import static org.apache.commons.lang3.StringUtils.upperCase;

import java.util.Locale;
import lombok.experimental.UtilityClass;

@UtilityClass
class GenderMapping {

  String toCdw(String fhir) {
    switch (upperCase(fhir, Locale.US)) {
      case "MALE":
        return "M";
      case "FEMALE":
        return "F";
      case "OTHER":
        return "*Missing*";
      case "UNKNOWN":
        return "*Unknown at this time*";
      default:
        return null;
    }
  }

  gov.va.api.health.argonaut.api.resources.Patient.Gender toDstu2Fhir(String cdw) {
    switch (upperCase(cdw, Locale.US)) {
      case "M":
        return gov.va.api.health.argonaut.api.resources.Patient.Gender.male;
      case "F":
        return gov.va.api.health.argonaut.api.resources.Patient.Gender.female;
      case "*MISSING*":
        return gov.va.api.health.argonaut.api.resources.Patient.Gender.other;
      case "*UNKNOWN AT THIS TIME*":
        return gov.va.api.health.argonaut.api.resources.Patient.Gender.unknown;
      default:
        return null;
    }
  }

  gov.va.api.health.uscorer4.api.resources.Patient.Gender toR4Fhir(String cdw) {
    switch (upperCase(cdw, Locale.US)) {
      case "M":
        return gov.va.api.health.uscorer4.api.resources.Patient.Gender.male;
      case "F":
        return gov.va.api.health.uscorer4.api.resources.Patient.Gender.female;
      case "*MISSING*":
        return gov.va.api.health.uscorer4.api.resources.Patient.Gender.other;
      case "*UNKNOWN AT THIS TIME*":
        return gov.va.api.health.uscorer4.api.resources.Patient.Gender.unknown;
      default:
        return null;
    }
  }
}
