package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.length;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.upperCase;
import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointUse;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import gov.va.api.health.dstu2.api.datatypes.Identifier;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.Patient;
import java.time.Instant;
import java.time.LocalDate;
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
@SuppressWarnings("UnnecessaryParentheses")
final class Dstu2PatientTransformer {
  @NonNull final DatamartPatient datamart;

  static Address address(DatamartPatient.Address address) {
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

  static Patient.Contact contact(DatamartPatient.Contact contact) {
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

  static ContactPoint.ContactPointUse contactPointUse(DatamartPatient.Telecom telecom) {
    if (telecom == null) {
      return null;
    }
    return switch (upperCase(trimToEmpty(telecom.type()))) {
      case "PATIENT CELL PHONE" -> ContactPointUse.mobile;
      case "PATIENT RESIDENCE", "PATIENT EMAIL", "PATIENT PAGER" -> ContactPointUse.home;
      case "PATIENT EMPLOYER", "SPOUSE EMPLOYER" -> ContactPointUse.work;
      case "TEMPORARY" -> ContactPointUse.temp;
      default -> null;
    };
  }

  static List<ContactPoint> contactTelecoms(DatamartPatient.Contact.Phone phone) {
    if (phone == null) {
      return null;
    }
    Set<ContactPoint> results = new LinkedHashSet<>(3);
    String phoneNumber = stripPhone(phone.phoneNumber());
    if (isNotBlank(phoneNumber)) {
      results.add(
          ContactPoint.builder()
              .system(ContactPoint.ContactPointSystem.phone)
              .use(ContactPointUse.home)
              .value(phoneNumber)
              .build());
    }
    String workPhoneNumber = stripPhone(phone.workPhoneNumber());
    if (isNotBlank(workPhoneNumber)) {
      results.add(
          ContactPoint.builder()
              .system(ContactPoint.ContactPointSystem.phone)
              .use(ContactPointUse.work)
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

  static String ethnicityDisplay(DatamartPatient.Ethnicity ethnicity) {
    if (ethnicity == null) {
      return null;
    }
    return switch (upperCase(trimToEmpty(ethnicity.hl7()), Locale.US)) {
      case "2135-2" -> "Hispanic or Latino";
      case "2186-5" -> "Non Hispanic or Latino";
      default -> ethnicity.display();
    };
  }

  static List<Extension> ethnicityExtensions(DatamartPatient.Ethnicity ethnicity) {
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

  static Coding maritalStatusCoding(String code) {
    String upper = upperCase(trimToEmpty(code), Locale.US);
    Coding.CodingBuilder result =
        Coding.builder().system("http://hl7.org/fhir/marital-status").code(upper);
    return switch (upper) {
      case "A" -> result.display("Annulled").build();
      case "D" -> result.display("Divorced").build();
      case "I" -> result.display("Interlocutory").build();
      case "L" -> result.display("Legally Separated").build();
      case "M" -> result.display("Married").build();
      case "P" -> result.display("Polygamous").build();
      case "S" -> result.display("Never Married").build();
      case "T" -> result.display("Domestic partner").build();
      case "W" -> result.display("Widowed").build();
      case "UNK" -> result.system("http://hl7.org/fhir/v3/NullFlavor").display("unknown").build();
      default -> null;
    };
  }

  static HumanName name(DatamartPatient.Contact contact) {
    if (contact == null || isBlank(contact.name())) {
      return null;
    }
    return HumanName.builder().text(contact.name()).build();
  }

  static Coding raceCoding(DatamartPatient.Race race) {
    if (race == null || isBlank(race.display())) {
      return null;
    }
    Coding.CodingBuilder result = Coding.builder().system("http://hl7.org/fhir/v3/Race");
    if (containsIgnoreCase(race.display(), "INDIAN")
        || containsIgnoreCase(race.display(), "ALASKA")) {
      return result.code("1002-5").display("American Indian or Alaska Native").build();
    } else if (containsIgnoreCase(race.display(), "ASIAN")) {
      return result.code("2028-9").display("Asian").build();
    } else if (containsIgnoreCase(race.display(), "BLACK")
        || containsIgnoreCase(race.display(), "AFRICA")) {
      return result.code("2054-5").display("Black or African American").build();
    } else if (containsIgnoreCase(race.display(), "HAWAII")
        || containsIgnoreCase(race.display(), "PACIFIC")) {
      return result.code("2076-8").display("Native Hawaiian or Other Pacific Islander").build();
    } else if (containsIgnoreCase(race.display(), "WHITE")) {
      return result.code("2106-3").display("White").build();
    } else {
      return result
          .system("http://hl7.org/fhir/v3/NullFlavor")
          .code("UNK")
          .display("Unknown")
          .build();
    }
  }

  static List<Extension> raceExtensions(List<DatamartPatient.Race> races) {
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

  static Coding relationshipCoding(DatamartPatient.Contact contact) {
    if (contact == null) {
      return null;
    }
    Coding.CodingBuilder builder =
        Coding.builder().system("http://hl7.org/fhir/patient-contact-relationship");
    return switch (upperCase(trimToEmpty(contact.type()), Locale.US)) {
      case "CIVIL GUARDIAN", "VA GUARDIAN" -> builder.code("guardian").display("Guardian").build();
      case "EMERGENCY CONTACT", "SECONDARY EMERGENCY CONTACT" -> builder
          .code("emergency")
          .display("Emergency")
          .build();
      case "NEXT OF KIN", "SECONDARY NEXT OF KIN", "SPOUSE EMPLOYER" -> builder
          .code("family")
          .display("Family")
          .build();
      default -> null;
    };
  }

  static List<CodeableConcept> relationships(DatamartPatient.Contact contact) {
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
    return switch (use) {
      case mobile -> 1;
      case home -> 2;
      case temp -> 3;
      case work -> 4;
      case old -> 5;
      default -> 6;
    };
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

  private String birthsex(String maybeBirthsex) {
    if (isBlank(maybeBirthsex)) {
      return null;
    }
    return switch (upperCase(maybeBirthsex, Locale.US)) {
      case "M", "MALE" -> "M";
      case "F", "FEMALE" -> "F";
      case "*UNKNOWN AT THIS TIME*", "UNKNOWN" -> "UNK";
      default -> null;
    };
  }

  private List<Patient.Contact> contacts() {
    return emptyToNull(
        datamart.contact().stream().map(con -> contact(con)).collect(Collectors.toList()));
  }

  private Boolean deceased() {
    if (deceasedDateTime() != null) {
      return null;
    }
    return switch (upperCase(trimToEmpty(datamart.deceased()), Locale.US)) {
      case "Y" -> true;
      case "N" -> false;
      default -> null;
    };
  }

  private String deceasedDateTime() {
    if (isBlank(datamart.deathDateTime())) {
      return null;
    }
    Instant instant = parseInstant(datamart.deathDateTime());
    if (instant == null) {
      return null;
    }
    return instant.toString();
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
    String birthsex = birthsex(datamart.gender());
    if (isNotBlank(birthsex)) {
      results.add(
          Extension.builder()
              .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-birthsex")
              .valueCode(birthsex)
              .build());
    }
    return emptyToNull(results);
  }

  private Patient.Gender gender() {
    if (isNotBlank(datamart.gender())) {
      return GenderMapping.toDstu2Fhir(datamart.gender());
    }
    return Patient.Gender.unknown;
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
              .system("http://va.gov/mpi")
              .value(datamart.fullIcn())
              .assigner(Reference.builder().display("Master Patient Index").build())
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

  Patient toFhir() {
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
