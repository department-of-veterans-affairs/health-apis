package gov.va.api.health.dataquery.tools.minimart.transformers;

import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class F2DPatientTransformer {

  private DatamartPatient.Contact contact(Patient.Contact contact) {
    if (contact == null) {
      return null;
    }
    return DatamartPatient.Contact.builder()
        .phone(phone(contact.telecom()))
        .name(contactName(contact.name()))
        .type(type(contact.relationship()))
        .build();
  }

  private String contactName(HumanName humanName) {
    if (humanName == null) {
      return null;
    }
    return humanName.text();
  }

  private List<DatamartPatient.Contact> contacts(List<Patient.Contact> contact) {
    return contact.stream().map(this::contact).collect(Collectors.toList());
  }

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
        .contact(contacts(patient.contact()))
        .objectVersion(1)
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
    switch (gender) {
      case male:
        return "M";
      case female:
        return "F";
      case other:
        return "*MISSING*";
      case unknown:
        return "*UNKNOWN AT THIS TIME*";
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

  private DatamartPatient.Contact.Phone phone(List<ContactPoint> telecoms) {
    if (telecoms == null || telecoms.isEmpty()) {
      return null;
    }
    String phoneNumber = telecoms.get(0) == null ? null : telecoms.get(0).value();
    String workPhoneNumber = telecoms.get(1) == null ? null : telecoms.get(1).value();
    String email = telecoms.get(2) == null ? null : telecoms.get(2).value();
    return DatamartPatient.Contact.Phone.builder()
        .phoneNumber(phoneNumber)
        .workPhoneNumber(workPhoneNumber)
        .email(email)
        .build();
  }

  private String type(List<CodeableConcept> relationship) {
    if (relationship == null || relationship.isEmpty() || relationship.get(0) == null) {
      return null;
    }
    return relationship.get(0).text();
  }
}
