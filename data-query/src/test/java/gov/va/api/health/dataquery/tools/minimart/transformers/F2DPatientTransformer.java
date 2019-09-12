package gov.va.api.health.dataquery.tools.minimart.transformers;

import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.health.dataquery.tools.minimart.FhirToDatamartUtils;
import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import gov.va.api.health.dstu2.api.datatypes.Identifier;
import gov.va.api.health.dstu2.api.elements.Extension;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class F2DPatientTransformer {

  FhirToDatamartUtils fauxIds;

  private DatamartPatient.Address address(Address address) {
    if (address == null) {
      return null;
    }
    String street1 = null;
    String street2 = null;
    String street3 = null;
    if (address.line() != null && !address.line().isEmpty()) {
      street1 = address.line().get(0);
      if (address.line().size() > 1) {
        street2 = address.line().get(1);
      }
      if (address.line().size() > 2) {
        street3 = address.line().get(2);
      }
    }
    return DatamartPatient.Address.builder()
        .street1(street1)
        .street2(street2)
        .street3(street3)
        .city(address.city())
        .state(address.state())
        .country(address.country())
        .postalCode(address.postalCode())
        .build();
  }

  private String birthDate(String birthDate) {
    if (birthDate == null) {
      return null;
    }
    return LocalDate.parse(birthDate).atStartOfDay().toInstant(ZoneOffset.UTC).toString();
  }

  private DatamartPatient.Contact contact(Patient.Contact contact) {
    if (contact == null) {
      return null;
    }
    return DatamartPatient.Contact.builder()
        .address(address(contact.address()))
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
    if (contact == null) {
      return null;
    }
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

  private DatamartPatient.Ethnicity ethnicity(List<Extension> extensions) {
    if (extensions == null || extensions.isEmpty()) {
      return null;
    }
    Extension ethnicityExtensions =
        findExtension(
            extensions, "http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity");
    if (ethnicityExtensions == null) {
      return null;
    }
    List<Extension> ethnicities = ethnicityExtensions.extension();
    if (ethnicities == null || ethnicities.isEmpty()) {
      return null;
    }
    Extension ethnicityExtension = findExtension(ethnicities, "ombCategory");
    if (ethnicityExtension == null || ethnicityExtension.valueCoding() == null) {
      return null;
    }
    return DatamartPatient.Ethnicity.builder()
        .hl7(ethnicityExtension.valueCoding().code())
        .display(ethnicityExtension.valueCoding().display())
        .build();
  }

  public DatamartPatient fhirToDatamart(Patient patient) {
    return DatamartPatient.builder()
        .objectVersion(1)
        .objectType(patient.resourceType())
        .fullIcn(fauxIds.unmask("Patient", patient.id()))
        .firstName(firstName(patient.name()))
        .lastName(lastName(patient.name()))
        .name(name(patient.name()))
        .birthDateTime(birthDate(patient.birthDate()))
        .deathDateTime(deathDateTime(patient.deceasedDateTime(), patient.deceasedBoolean()))
        .deceased(deceased(patient.deceasedDateTime(), patient.deceasedBoolean()))
        .gender(gender(patient.gender()))
        .contact(contacts(patient.contact()))
        .maritalStatus(maritalStatus(patient.maritalStatus()))
        .ssn(ssn(patient.identifier()))
        .ethnicity(ethnicity(patient.extension()))
        .race(race(patient.extension()))
        .address(patient.address().stream().map(a -> address(a)).collect(Collectors.toList()))
        .telecom(telecoms(patient.telecom()))
        .objectVersion(1)
        .build();
  }

  Extension findExtension(List<Extension> extensions, String url) {
    for (Extension extension : extensions) {
      if (extension.url().equals(url)) {
        return extension;
      }
    }
    return null;
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

  private DatamartPatient.MaritalStatus maritalStatus(CodeableConcept maritalStatus) {
    if (maritalStatus == null || maritalStatus.coding().isEmpty()) {
      return null;
    }
    String codingDisplay = maritalStatus.coding().get(0).display();
    String code;
    switch (codingDisplay) {
      case "Annulled":
        code = "A";
        break;
      case "Divorced":
        code = "D";
        break;
      case "Interlocutory":
        code = "I";
        break;
      case "Legally Separated":
        code = "L";
        break;
      case "Married":
        code = "M";
        break;
      case "Polygamous":
        code = "P";
        break;
      case "Never Married":
        code = "S";
        break;
      case "Domestic partner":
        code = "T";
        break;
      case "Widowed":
        code = "W";
        break;
      case "unknown":
        code = "UNK";
        break;
      default:
        code = null;
    }
    return DatamartPatient.MaritalStatus.builder().abbrev(code).code(code).build();
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

  private List<DatamartPatient.Race> race(List<Extension> extensions) {
    if (extensions == null || extensions.isEmpty()) {
      return null;
    }
    Extension raceExtension =
        findExtension(extensions, "http://fhir.org/guides/argonaut/StructureDefinition/argo-race");
    if (raceExtension == null) {
      return null;
    }
    List<Extension> races = raceExtension.extension();
    if (races == null || races.isEmpty()) {
      return null;
    }
    return races
        .stream()
        .filter(x -> x != null)
        .map(Extension::valueCoding)
        .filter(x -> x != null)
        .map(Coding::display)
        .map(d -> DatamartPatient.Race.builder().display(d).build())
        .collect(Collectors.toList());
  }

  private String ssn(List<Identifier> identifier) {
    if (identifier == null || identifier.size() < 2 || identifier.get(1) == null) {
      return null;
    }
    return identifier.get(1).value();
  }

  private DatamartPatient.Telecom telecom(ContactPoint contactPoint) {
    if (contactPoint == null) {
      return null;
    }
    String workPhoneNumber = null;
    String phoneNumber = null;
    String email = null;
    String type = null;
    if (contactPoint.system() == ContactPoint.ContactPointSystem.phone) {
      if (contactPoint.use() == ContactPoint.ContactPointUse.work) {
        workPhoneNumber = contactPoint.value();
        type = "Patient Employer";
      } else {
        switch (contactPoint.use()) {
          case mobile:
            type = "Patient Cell Phone";
            break;
          case temp:
            type = "Temporary";
            break;
          case home:
            type = "Patient Resident";
            break;
        }
        phoneNumber = contactPoint.value();
      }
    }
    if (contactPoint.system() == ContactPoint.ContactPointSystem.email) {
      type = "Patient Email";
      email = contactPoint.value();
    }
    return DatamartPatient.Telecom.builder()
        .workPhoneNumber(workPhoneNumber)
        .phoneNumber(phoneNumber)
        .email(email)
        .type(type)
        .build();
  }

  private List<DatamartPatient.Telecom> telecoms(List<ContactPoint> telecoms) {
    if (telecoms == null) {
      return null;
    }
    return telecoms.stream().map(t -> telecom(t)).collect(Collectors.toList());
  }

  private String type(List<CodeableConcept> relationship) {
    if (relationship == null || relationship.isEmpty() || relationship.get(0) == null) {
      return null;
    }
    return relationship.get(0).text();
  }
}
