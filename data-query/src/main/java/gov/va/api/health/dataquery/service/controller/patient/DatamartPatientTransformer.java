package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.parseLocalDateTime;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.length;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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

  private static Patient.Contact contact(DatamartPatient.Contact contact) {
    if (contact == null) {
      return null;
    }
    List<CodeableConcept> relationships = emptyToNull(relationships(contact));
    if (isEmpty(relationships)) {
      return null;
    }
    HumanName name = name(contact);
    List<ContactPoint> telecoms = emptyToNull(contactTelecoms(contact.phone()));
    Address address = address(contact.address());
    return Patient.Contact.builder()
        .name(name)
        .relationship(relationships)
        .telecom(telecoms)
        .address(address)
        .build();
  }

  private static ContactPoint.ContactPointUse contactPointUse(DatamartPatient.Telecom telecom) {
    if (telecom == null) {
      return null;
    }
    switch (upperCase(trimToEmpty(telecom.type()))) {
      case "PATIENT CELL PHONE":
        return ContactPoint.ContactPointUse.mobile;
      case "PATIENT RESIDENCE":
        // falls through
      case "PATIENT EMAIL":
        // falls through
      case "PATIENT PAGER":
        return ContactPoint.ContactPointUse.home;
      case "PATIENT EMPLOYER":
        // falls through
      case "SPOUSE EMPLOYER":
        return ContactPoint.ContactPointUse.work;
      case "TEMPORARY":
        return ContactPoint.ContactPointUse.temp;
      default:
        return null;
    }
  }

  private static List<ContactPoint> contactTelecoms(DatamartPatient.Contact.Phone phone) {
    if (phone == null) {
      return null;
    }
    Set<ContactPoint> results = new LinkedHashSet<>(3);
    String phoneNumber = stripPhone(phone.phoneNumber());
    if (isNotBlank(phoneNumber)) {
      results.add(
          ContactPoint.builder()
              .system(ContactPoint.ContactPointSystem.phone)
              .value(phoneNumber)
              .build());
    }
    String workPhoneNumber = stripPhone(phone.workPhoneNumber());
    if (isNotBlank(workPhoneNumber)) {
      results.add(
          ContactPoint.builder()
              .system(ContactPoint.ContactPointSystem.phone)
              .value(workPhoneNumber)
              .build());
    }
    if (isNotBlank(phone.email())) {
      results.add(
          ContactPoint.builder()
              .system(ContactPoint.ContactPointSystem.email)
              .value(phone.email())
              .build());
    }
    return emptyToNull(new ArrayList<>(results));
  }

  private static String ethnicityDisplay(DatamartPatient.Ethnicity ethnicity) {
    if (ethnicity == null) {
      return null;
    }
    switch (upperCase(trimToEmpty(ethnicity.hl7()), Locale.US)) {
      case "2135-2":
        return "Hispanic or Latino";
      case "2186-5":
        return "Non Hispanic or Latino";
      default:
        return ethnicity.display();
    }
  }

  private static List<Extension> ethnicityExtensions(DatamartPatient.Ethnicity ethnicity) {
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

  private static Coding maritalStatusCoding(String code) {
    String upper = upperCase(trimToEmpty(code), Locale.US);
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

  private static HumanName name(DatamartPatient.Contact contact) {
    if (contact == null || isBlank(contact.name())) {
      return null;
    }
    return HumanName.builder().text(contact.name()).build();
  }

  private static Coding raceCoding(DatamartPatient.Race race) {
    if (race == null || isBlank(race.hl7())) {
      return null;
    }
    Coding.CodingBuilder result =
        Coding.builder().system("http://hl7.org/fhir/v3/Race").code(race.hl7());
    switch (upperCase(trimToEmpty(race.hl7()), Locale.US)) {
      case "1002-5":
        return result.display("American Indian or Alaska Native").build();
      case "2028-9":
        return result.display("Asian").build();
      case "2054-5":
        return result.display("Black or African American").build();
      case "2076-8":
        return result.display("Native Hawaiian or Other Pacific Islander").build();
      case "2106-3":
        return result.display("White").build();
      case "UNK":
        return result.system("http://hl7.org/fhir/v3/NullFlavor").display("Unknown").build();
      case "ASKU":
        return result
            .system("http://hl7.org/fhir/v3/NullFlavor")
            .display("Asked but no answer")
            .build();
      default:
        return result.display(race.display()).build();
    }
  }

  private static List<Extension> raceExtensions(List<DatamartPatient.Race> races) {
    if (isEmpty(races)) {
      return null;
    }
    List<Extension> results = new ArrayList<>(races.size() + 1);
    for (DatamartPatient.Race race : races) {
      if (race == null) {
        continue;
      }
      Coding coding = raceCoding(race);
      if (coding == null) {
        continue;
      }
      results.add(Extension.builder().url("ombCategory").valueCoding(coding).build());
    }
    Optional<Coding> firstCoding =
        races.stream().map(race -> raceCoding(race)).filter(Objects::nonNull).findFirst();
    if (firstCoding.isPresent()) {
      results.add(Extension.builder().url("text").valueString(firstCoding.get().display()).build());
    }
    return emptyToNull(results);
  }

  private static Coding relationshipCoding(DatamartPatient.Contact contact) {
    if (contact == null) {
      return null;
    }
    Coding.CodingBuilder builder =
        Coding.builder().system("http://hl7.org/fhir/patient-contact-relationship");
    switch (upperCase(trimToEmpty(contact.type()), Locale.US)) {
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
    if (coding == null) {
      return null;
    }
    return asList(CodeableConcept.builder().coding(asList(coding)).text(contact.type()).build());
  }

  private static int sortNum(ContactPoint.ContactPointUse use) {
    if (use == null) {
      return 6;
    }
    switch (use) {
      case mobile:
        return 1;
      case home:
        return 2;
      case temp:
        return 3;
      case work:
        return 4;
      case old:
        return 5;
      default:
        return 6;
    }
  }

  private static String stripPhone(String phone) {
    if (phone == null) {
      return null;
    }
    return phone.replaceAll("\\(|\\)|-", "");
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
    switch (upperCase(trimToEmpty(datamart.deceased()), Locale.US)) {
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

  private List<Extension> extensions() {
    List<Extension> results = new ArrayList<>(2);
    List<Extension> raceExtensions = emptyToNull(raceExtensions(datamart.race()));
    if (!isEmpty(raceExtensions)) {
      results.add(
          Extension.builder()
              .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
              .extension(raceExtensions)
              .build());
    }
    List<Extension> ethnicityExtensions = emptyToNull(ethnicityExtensions(datamart.ethnicity()));
    if (!isEmpty(ethnicityExtensions)) {
      results.add(
          Extension.builder()
              .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
              .extension(ethnicityExtensions)
              .build());
    }
    if (isNotBlank(datamart.gender())) {
      results.add(
          Extension.builder()
              .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-birthsex")
              .valueCode(datamart.gender())
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
    Coding coding = maritalStatusCoding(status.code());
    if (coding == null) {
      coding = maritalStatusCoding(status.abbrev());
    }
    if (coding == null) {
      return null;
    }
    return CodeableConcept.builder().coding(asList(coding)).build();
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
      String phoneNumber = stripPhone(telecom.phoneNumber());
      if (isNotBlank(phoneNumber) && contactPointUse(telecom) != null) {
        results.add(
            ContactPoint.builder()
                .system(ContactPoint.ContactPointSystem.phone)
                .value(phoneNumber)
                .use(contactPointUse(telecom))
                .build());
      }
      String workPhoneNumber = stripPhone(telecom.workPhoneNumber());
      if (isNotBlank(workPhoneNumber)) {
        results.add(
            ContactPoint.builder()
                .system(ContactPoint.ContactPointSystem.phone)
                .value(workPhoneNumber)
                .use(ContactPoint.ContactPointUse.work)
                .build());
      }
      if (isNotBlank(telecom.email())) {
        results.add(
            ContactPoint.builder()
                .system(ContactPoint.ContactPointSystem.email)
                .value(telecom.email())
                .use(
                    Optional.ofNullable(contactPointUse(telecom))
                        .orElse(ContactPoint.ContactPointUse.temp))
                .build());
      }
    }
    List<ContactPoint> asList = new ArrayList<>(results);
    Collections.sort(
        asList, (left, right) -> Integer.compare(sortNum(left.use()), sortNum(right.use())));
    return emptyToNull(asList);
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
