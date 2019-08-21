package gov.va.api.health.dataquery.tools.minimart.transformers;

import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import java.time.Instant;
import java.util.List;

public class F2DPatientTransformer {

  private String deathDateTime(String deathDateTime, Boolean deceasedBoolean) {
    if (deceasedBoolean != null || isBlank(deathDateTime)) {
      return null;
    }
    Instant instant = parseInstant(deathDateTime);
    if (instant == null) {
      return null;
    }
    return instant.toString();
  }

  private String deceased(String deathDateTime, Boolean deceasedBoolean) {
    if (deathDateTime != null || deceasedBoolean == null) {
      return null;
    }
    if (deceasedBoolean) {
      return "Y";
    }
    return "N";
  }

  public DatamartPatient fhirToDatamart(Patient patient) {
    return DatamartPatient.builder()
        .objectType(patient.resourceType())
        .fullIcn(patient.id())
        .firstName(firstName(patient.name()))
        .lastName(lastName(patient.name()))
        .name(name(patient.name()))
        .birthDateTime(patient.birthDate())
        .deathDateTime(deathDateTime(patient.deceasedDateTime(), patient.deceasedBoolean()))
        .deceased(deceased(patient.deceasedDateTime(), patient.deceasedBoolean()))
        .gender(gender(patient.gender()))
        .build();
  }

  private String firstName(List<HumanName> name) {
    if (name == null
        || name.isEmpty()
        || name.get(0) == null
        || name.get(0).given() == null
        || name.get(0).given().isEmpty()) {
      return null;
    }
    return name.get(0).given().get(0);
  }

  private String gender(Patient.Gender gender) {
    switch (gender.toString()) {
      case "male":
        return "M";
      case "female":
        return "F";
      case "other":
        return "X";
      default:
        return null;
    }
  }

  private String lastName(List<HumanName> name) {
    if (name == null
        || name.isEmpty()
        || name.get(0) == null
        || name.get(0).family() == null
        || name.get(0).family().isEmpty()) {
      return null;
    }
    return name.get(0).family().get(0);
  }

  private String name(List<HumanName> name) {
    if (name == null || name.isEmpty() || name.get(0) == null) {
      return null;
    }
    return name.get(0).text();
  }
}
