package gov.va.api.health.dataquery.service.controller.patient;

import static org.apache.commons.lang3.StringUtils.upperCase;

import gov.va.api.health.uscorer4.api.resources.Patient;
import java.util.Locale;
import lombok.experimental.UtilityClass;

@UtilityClass
class R4GenderMapping {

  String toCdw(String fhir) {
    switch (upperCase(fhir, Locale.US)) {
      case "MALE":
        return "M";
      case "FEMALE":
        return "F";
      case "OTHER":
        return "*Missing*";
      default:
        return "*Unknown at this time*";
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
      default:
        return Patient.Gender.unknown;
    }
  }
}
