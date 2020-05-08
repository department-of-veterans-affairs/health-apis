package gov.va.api.health.dataquery.service.controller.patient;

import static org.apache.commons.lang3.StringUtils.upperCase;

import gov.va.api.health.argonaut.api.resources.Patient;
import java.util.Locale;
import lombok.experimental.UtilityClass;

@UtilityClass
class Dstu2GenderMapping {

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

  Patient.Gender toFhir(String cdw) {
    switch (upperCase(cdw, Locale.US)) {
      case "M":
        return Patient.Gender.male;
      case "F":
        return Patient.Gender.female;
      case "*MISSING*":
        return Patient.Gender.other;
      case "*UNKNOWN AT THIS TIME*":
        return Patient.Gender.unknown;
      default:
        return null;
    }
  }
}
