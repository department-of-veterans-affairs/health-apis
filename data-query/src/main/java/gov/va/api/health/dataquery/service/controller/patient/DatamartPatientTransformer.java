package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.parseLocalDateTime;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.length;
import static org.apache.commons.lang3.StringUtils.upperCase;
import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import gov.va.api.health.dstu2.api.datatypes.Identifier;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import lombok.Builder;
import lombok.NonNull;

@Builder
final class DatamartPatientTransformer {
  @NonNull final DatamartPatient datamart;

  private static Address address(DatamartPatient.Address address) {
    if (address == null
        || allBlank(
            address.street1(),
            address.street2(),
            address.street3(),
            address.city(),
            address.state(),
            address.postalCode(),
            address.country())) {
      return null;
    }

    return Address.builder()
        .line(emptyToNull(Arrays.asList(address.street1(), address.street2(), address.street3())))
        .city(address.city())
        .state(address.state())
        .postalCode(address.postalCode())
        .country(address.country())
        .build();
  }

  private static List<Extension> argoExtensions(DatamartPatient.Ethnicity ethnicity) {
    if (ethnicity == null) {
      return null;
    }

    List<Extension> results = new ArrayList<>(2);

    String display = ethnicityDisplay(ethnicity);
    if (!allBlank(display, ethnicity.hl7())) {
      results.add(
          Extension.builder()
              .url("ombCategory")
              .valueCoding(
                  Coding.builder()
                      .system("http://hl7.org/fhir/ValueSet/v3-Ethnicity")
                      .code(ethnicity.hl7())
                      .display(display)
                      .build())
              .build());
    }

    if (isNotBlank(display)) {
      results.add(Extension.builder().url("text").valueString(display).build());
    }

    return emptyToNull(results);
  }

  private static List<Extension> argoExtensions(List<DatamartPatient.Race> datamartRaces) {
    if (isEmpty(datamartRaces)) {
      return null;
    }

    List<Extension> results = new ArrayList<>(datamartRaces.size() + 1);

    for (DatamartPatient.Race datamartRace : datamartRaces) {
      if (datamartRace == null || allBlank(datamartRace.abbrev(), datamartRace.display())) {
        continue;
      }

      results.add(
          Extension.builder()
              .url("ombCategory")
              .valueCoding(
                  Coding.builder()
                      .system("http://hl7.org/fhir/v3/Race")
                      .code(datamartRace.abbrev())
                      .display(datamartRace.display())
                      .build())
              .build());
    }

    Optional<DatamartPatient.Race> firstDisplay =
        datamartRaces.stream().filter(race -> isNotBlank(race.display())).findFirst();
    if (firstDisplay.isPresent()) {
      results.add(
          Extension.builder().url("text").valueString(firstDisplay.get().display()).build());
    }

    return emptyToNull(results);
  }

  private static Patient.Contact contact(DatamartPatient.Contact contact) {
    if (contact == null) {
      return null;
    }

    HumanName name = name(contact);
    List<CodeableConcept> relationships = emptyToNull(relationships(contact));
    List<ContactPoint> telecoms = emptyToNull(contactTelecoms(contact.phone()));
    Address address = address(contact.address());

    if (allBlank(name, relationships, telecoms, address)) {
      return null;
    }

    return Patient.Contact.builder()
        .name(name)
        .relationship(relationships)
        .telecom(telecoms)
        .address(address)
        .build();
  }

  private static List<ContactPoint> contactTelecoms(DatamartPatient.Contact.Phone phone) {
    if (phone == null) {
      return null;
    }

    List<ContactPoint> results = new ArrayList<>(3);

    if (isNotBlank(phone.phoneNumber())) {
      results.add(
          ContactPoint.builder()
              .system(ContactPoint.ContactPointSystem.phone)
              .value(phone.phoneNumber())
              .build());
    }

    if (isNotBlank(phone.workPhoneNumber())) {
      results.add(
          ContactPoint.builder()
              .system(ContactPoint.ContactPointSystem.phone)
              .value(phone.workPhoneNumber())
              .use(ContactPoint.ContactPointUse.work)
              .build());
    }

    if (isNotBlank(phone.email())) {
      results.add(
          ContactPoint.builder()
              .system(ContactPoint.ContactPointSystem.email)
              .value(phone.email())
              .build());
    }

    return emptyToNull(results);
  }

  private static HumanName name(DatamartPatient.Contact contact) {
    if (contact == null || isBlank(contact.name())) {
      return null;
    }
    return HumanName.builder().text(contact.name()).build();
  }

  private static Coding relationshipCoding(DatamartPatient.Contact contact) {
    if (contact == null || isBlank(contact.type())) {
      return null;
    }

    Coding.CodingBuilder builder =
        Coding.builder().system("http://hl7.org/fhir/patient-contact-relationship");

    switch (upperCase(contact.type(), Locale.US)) {
      case "CIVIL GUARDIAN":
        // falls through
      case "VA GUARDIAN":
        return builder.code("guardian").display("Guardian").build();

      case "EMERGENCY CONTACT":
        // falls through
      case "SECONDARY EMERGENCY CONTACT":
        return builder.code("emergency").display("Emergency").build();

      case "NEXT OF KIN":
        // falls through
      case "SECONDARY NEXT OF KIN":
        // falls through
      case "SPOUSE EMPLOYER":
        return builder.code("family").display("Family").build();

      default:
        return null;
    }
  }

  private static List<CodeableConcept> relationships(DatamartPatient.Contact contact) {
    if (contact == null) {
      return null;
    }

    Coding coding = relationshipCoding(contact);
    if (allBlank(coding, contact.relationship())) {
      return null;
    }

    return asList(
        CodeableConcept.builder()
            .coding(emptyToNull(asList(coding)))
            .text(contact.relationship())
            .build());
  }

  private List<Address> addresses() {
    return emptyToNull(
        datamart.address().stream().map(adr -> address(adr)).collect(Collectors.toList()));
  }

  private String birthDate() {
    if (length(datamart.birthDateTime()) <= 9) {
      return null;
    }

    LocalDate date = LocalDate.parse(datamart.birthDateTime().substring(0, 10));
    if (date == null) {
      return null;
    }

    return date.toString();
  }

  private List<Patient.Contact> contacts() {
    return emptyToNull(
        datamart.contact().stream().map(con -> contact(con)).collect(Collectors.toList()));
  }

  private Boolean deceased() {
    if (isBlank(datamart.deceased())) {
      return null;
    }
    switch (upperCase(datamart.deceased(), Locale.US)) {
      case "Y":
        return true;
      case "N":
        return false;
      default:
        return null;
    }
  }

  private String deceasedDateTime() {
    if (isBlank(datamart.deathDateTime())) {
      return null;
    }

    LocalDateTime dateTime = parseLocalDateTime(datamart.deathDateTime());
    if (dateTime == null) {
      return null;
    }

    return dateTime.atZone(ZoneId.of("Z")).toString();
  }

  private static String ethnicityDisplay(DatamartPatient.Ethnicity ethnicity) {
    if (ethnicity == null || isBlank(ethnicity.hl7())) {
      return null;
    }

    switch (upperCase(ethnicity.hl7(), Locale.US)) {
      case "2135-2":
        return "Hispanic or Latino";
      case "2186-5":
        return "Non Hispanic or Latino";
      default:
        return ethnicity.display();
    }
  }

  private List<Extension> extensions() {
    List<Extension> results = new ArrayList<>(2);

    List<Extension> raceExtensions = emptyToNull(argoExtensions(datamart.race()));
    if (!isEmpty(raceExtensions)) {
      results.add(
          Extension.builder()
              .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
              .extension(raceExtensions)
              .build());
    }

    List<Extension> ethnicityExtensions = emptyToNull(argoExtensions(datamart.ethnicity()));
    if (!isEmpty(ethnicityExtensions)) {
      results.add(
          Extension.builder()
              .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
              .extension(ethnicityExtensions)
              .build());
    }

    return emptyToNull(results);
  }

  private Patient.Gender gender() {
    if (isBlank(datamart.gender())) {
      return null;
    }
    return GenderMapping.toFhir(datamart.gender());
  }

  private List<Identifier> identifiers() {
    List<Identifier> results = new ArrayList<>(2);

    if (isNotBlank(datamart.fullIcn())) {
      results.add(
          Identifier.builder()
              .use(Identifier.IdentifierUse.usual)
              .type(
                  CodeableConcept.builder()
                      .coding(
                          asList(
                              Coding.builder()
                                  .system("http://hl7.org/fhir/v2/0203")
                                  .code("MR")
                                  .build()))
                      .build())
              .system("http://va.gov/mvi")
              .value(datamart.fullIcn())
              .assigner(Reference.builder().display("Master Veteran Index").build())
              .build());
    }

    if (isNotBlank(datamart.ssn())) {
      results.add(
          Identifier.builder()
              .use(Identifier.IdentifierUse.official)
              .type(
                  CodeableConcept.builder()
                      .coding(
                          asList(
                              Coding.builder()
                                  .system("http://hl7.org/fhir/v2/0203")
                                  .code("SB")
                                  .build()))
                      .build())
              .system("http://hl7.org/fhir/sid/us-ssn")
              .value(datamart.ssn())
              .assigner(Reference.builder().display("United States Social Security Number").build())
              .build());
    }

    return emptyToNull(results);
  }

  private CodeableConcept maritalStatus() {
    DatamartPatient.MaritalStatus status = datamart.maritalStatus();
    if (status == null) {
      return null;
    }

    Coding coding = maritalStatus(status.code());
    if (coding == null) {
      coding = maritalStatus(status.abbrev());
    }
    if (coding == null) {
      return null;
    }

    return CodeableConcept.builder().coding(asList(coding)).build();
  }

  private Coding maritalStatus(String code) {
    if (isBlank(code)) {
      return null;
    }

    String upper = upperCase(code, Locale.US);
    Coding.CodingBuilder result =
        Coding.builder().system("http://hl7.org/fhir/marital-status").code(upper);

    switch (upper) {
      case "A":
        return result.display("Annulled").build();
      case "D":
        return result.display("Divorced").build();
      case "I":
        return result.display("Interlocutory").build();
      case "L":
        return result.display("Legally Separated").build();
      case "M":
        return result.display("Married").build();
      case "P":
        return result.display("Polygamous").build();
      case "S":
        return result.display("Never Married").build();
      case "T":
        return result.display("Domestic partner").build();
      case "W":
        return result.display("Widowed").build();
      case "UNK":
        return result.system("http://hl7.org/fhir/v3/NullFlavor").display("unknown").build();
      default:
        return null;
    }
  }

  private List<HumanName> names() {
    if (allBlank(datamart.name(), datamart.firstName(), datamart.lastName())) {
      return null;
    }

    HumanName.HumanNameBuilder builder =
        HumanName.builder().use(HumanName.NameUse.usual).text(datamart.name());

    if (isNotBlank(datamart.firstName())) {
      builder.given(asList(datamart.firstName()));
    }

    if (isNotBlank(datamart.lastName())) {
      builder.family(asList(datamart.lastName()));
    }

    return asList(builder.build());
  }

  private List<ContactPoint> telecoms() {
    Set<ContactPoint> results = new LinkedHashSet<>();

    for (final DatamartPatient.Telecom telecom : datamart.telecom()) {
      if (telecom == null) {
        continue;
      }

      if (isNotBlank(telecom.phoneNumber())) {
        results.add(
            ContactPoint.builder()
                .system(ContactPoint.ContactPointSystem.phone)
                .value(telecom.phoneNumber())
                .build());
      }

      if (isNotBlank(telecom.workPhoneNumber())) {
        results.add(
            ContactPoint.builder()
                .system(ContactPoint.ContactPointSystem.phone)
                .value(telecom.workPhoneNumber())
                .use(ContactPoint.ContactPointUse.work)
                .build());
      }

      if (isNotBlank(telecom.email())) {
        results.add(
            ContactPoint.builder()
                .system(ContactPoint.ContactPointSystem.email)
                .value(telecom.email())
                .build());
      }
    }

    return isEmpty(results) ? null : new ArrayList<>(results);
  }

  Patient toFhirPatient() {
    return Patient.builder()
        .id(datamart.fullIcn())
        .resourceType("Patient")
        .extension(extensions())
        .identifier(identifiers())
        .name(names())
        .telecom(telecoms())
        .address(addresses())
        .gender(gender())
        .birthDate(birthDate())
        .deceasedBoolean(deceased())
        .deceasedDateTime(deceasedDateTime())
        .maritalStatus(maritalStatus())
        .contact(contacts())
        .build();
  }
}
