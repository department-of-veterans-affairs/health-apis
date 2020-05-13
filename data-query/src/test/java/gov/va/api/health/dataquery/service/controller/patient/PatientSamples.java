package gov.va.api.health.dataquery.service.controller.patient;

import static java.util.Arrays.asList;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.argonaut.api.resources.Patient.Gender;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient.Address;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient.MaritalStatus;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient.Race;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient.Telecom;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointUse;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import gov.va.api.health.dstu2.api.datatypes.HumanName.NameUse;
import gov.va.api.health.dstu2.api.datatypes.Identifier;
import gov.va.api.health.dstu2.api.datatypes.Identifier.IdentifierUse;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PatientSamples {
  @AllArgsConstructor(staticName = "create")
  public static class Datamart {
    public DatamartPatient.Contact contact() {
      return DatamartPatient.Contact.builder()
          .type("Emergency Contact")
          .address(
              DatamartPatient.Address.builder()
                  .street1("111 MacGyver Viaduct")
                  .city("Anchorage")
                  .state("Alaska")
                  .postalCode("99501")
                  .build())
          .name("Name Name")
          .phone(
              DatamartPatient.Contact.Phone.builder()
                  .email("e@mail.com")
                  .phoneNumber("5555555555")
                  .workPhoneNumber("5555555555")
                  .build())
          .relationship("Emergency Contact")
          .build();
    }

    public DatamartPatient patient() {
      return patient("1000003");
    }

    public DatamartPatient patient(String id) {
      return DatamartPatient.builder()
          .fullIcn(id)
          .ssn("999-30-3951")
          .name("Mr. Tobias236 Wolff180")
          .lastName("Wolff180")
          .firstName("Tobias236")
          .birthDateTime("1970-11-14T00:00:00Z")
          .deathDateTime("2001-03-03T15:08:09Z")
          .gender("M")
          .maritalStatus(MaritalStatus.builder().abbrev("M").code("M").build())
          .race(List.of(Race.builder().display("American Indian or Alaska Native").build()))
          .telecom(
              List.of(
                  Telecom.builder().type("Patient Cell Phone").phoneNumber("5551836103").build(),
                  Telecom.builder()
                      .type("Patient Email")
                      .email("Tobias236.Wolff180@email.example")
                      .build()))
          .address(
              List.of(
                  Address.builder()
                      .street1("111 MacGyver Viaduct")
                      .city("Anchorage")
                      .state("Alaska")
                      .postalCode("99501")
                      .build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class Dstu2 {
    static Patient.Bundle asBundle(
        String basePath, Collection<Patient> records, BundleLink... links) {
      return Patient.Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(records.size())
          .link(asList(links))
          .entry(
              records.stream()
                  .map(
                      c ->
                          Patient.Entry.builder()
                              .fullUrl(basePath + "/Patient/" + c.id())
                              .resource(c)
                              .search(AbstractEntry.Search.builder().mode(SearchMode.match).build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static BundleLink link(BundleLink.LinkRelation relation, String base, int page, int count) {
      return BundleLink.builder()
          .relation(relation)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    public Patient patient() {
      return patient("1000003");
    }

    public Patient patient(String id) {
      return Patient.builder()
          .id(id)
          .resourceType("Patient")
          .extension(
              List.of(
                  Extension.builder()
                      .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
                      .extension(
                          List.of(
                              Extension.builder()
                                  .url("ombCategory")
                                  .valueCoding(
                                      Coding.builder()
                                          .system("http://hl7.org/fhir/v3/Race")
                                          .code("1002-5")
                                          .display("American Indian or Alaska Native")
                                          .build())
                                  .build(),
                              Extension.builder()
                                  .url("text")
                                  .valueString("American Indian or Alaska Native")
                                  .build()))
                      .build(),
                  Extension.builder()
                      .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-birthsex")
                      .valueCode("M")
                      .build()))
          .identifier(
              List.of(
                  Identifier.builder()
                      .use(IdentifierUse.usual)
                      .type(
                          CodeableConcept.builder()
                              .coding(
                                  List.of(
                                      Coding.builder()
                                          .system("http://hl7.org/fhir/v2/0203")
                                          .code("MR")
                                          .build()))
                              .build())
                      .system("http://va.gov/mvi")
                      .value(id)
                      .assigner(Reference.builder().display("Master Veteran Index").build())
                      .build(),
                  Identifier.builder()
                      .use(IdentifierUse.official)
                      .type(
                          CodeableConcept.builder()
                              .coding(
                                  List.of(
                                      Coding.builder()
                                          .system("http://hl7.org/fhir/v2/0203")
                                          .code("SB")
                                          .build()))
                              .build())
                      .system("http://hl7.org/fhir/sid/us-ssn")
                      .value("999-30-3951")
                      .assigner(
                          Reference.builder()
                              .display("United States Social Security Number")
                              .build())
                      .build()))
          .name(
              List.of(
                  HumanName.builder()
                      .use(NameUse.usual)
                      .text("Mr. Tobias236 Wolff180")
                      .family(List.of("Wolff180"))
                      .given(List.of("Tobias236"))
                      .build()))
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .system(ContactPointSystem.phone)
                      .value("5551836103")
                      .use(ContactPointUse.mobile)
                      .build(),
                  ContactPoint.builder()
                      .system(ContactPointSystem.email)
                      .value("Tobias236.Wolff180@email.example")
                      .use(ContactPointUse.home)
                      .build()))
          .gender(Gender.male)
          .birthDate("1970-11-14")
          .deceasedDateTime("2001-03-03T15:08:09Z")
          .address(
              List.of(
                  gov.va.api.health.dstu2.api.datatypes.Address.builder()
                      .line(List.of("111 MacGyver Viaduct"))
                      .city("Anchorage")
                      .state("Alaska")
                      .postalCode("99501")
                      .build()))
          .maritalStatus(
              CodeableConcept.builder()
                  .coding(
                      List.of(
                          Coding.builder()
                              .system("http://hl7.org/fhir/marital-status")
                              .code("M")
                              .display("Married")
                              .build()))
                  .build())
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class R4 {
    static gov.va.api.health.uscorer4.api.resources.Patient.Bundle asBundle(
        String basePath,
        Collection<gov.va.api.health.uscorer4.api.resources.Patient> records,
        gov.va.api.health.r4.api.bundle.BundleLink... links) {
      return gov.va.api.health.uscorer4.api.resources.Patient.Bundle.builder()
          .resourceType("Bundle")
          .type(AbstractBundle.BundleType.searchset)
          .total(records.size())
          .link(asList(links))
          .entry(
              records.stream()
                  .map(
                      c ->
                          gov.va.api.health.uscorer4.api.resources.Patient.Entry.builder()
                              .fullUrl(basePath + "/Patient/" + c.id())
                              .resource(c)
                              .search(
                                  gov.va.api.health.r4.api.bundle.AbstractEntry.Search.builder()
                                      .mode(
                                          gov.va.api.health.r4.api.bundle.AbstractEntry.SearchMode
                                              .match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static gov.va.api.health.r4.api.bundle.BundleLink link(
        gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation relation,
        String base,
        int page,
        int count) {
      return gov.va.api.health.r4.api.bundle.BundleLink.builder()
          .relation(relation)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    public gov.va.api.health.uscorer4.api.resources.Patient.PatientContact contact() {
      return gov.va.api.health.uscorer4.api.resources.Patient.PatientContact.builder()
          .relationship(
              List.of(
                  gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                      .coding(
                          List.of(
                              gov.va.api.health.r4.api.datatypes.Coding.builder()
                                  .code("emergency")
                                  .display("Emergency")
                                  .system("http://hl7.org/fhir/patient-contact-relationship")
                                  .build()))
                      .text("Emergency Contact")
                      .build()))
          .name(gov.va.api.health.r4.api.datatypes.HumanName.builder().text("Name Name").build())
          .telecom(
              List.of(
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.phone)
                      .value("5555555555")
                      .use(gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointUse.home)
                      .build(),
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.phone)
                      .value("5555555555")
                      .use(gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointUse.work)
                      .build(),
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.email)
                      .value("e@mail.com")
                      .build()))
          .address(
              gov.va.api.health.r4.api.datatypes.Address.builder()
                  .line(List.of("111 MacGyver Viaduct"))
                  .city("Anchorage")
                  .state("Alaska")
                  .postalCode("99501")
                  .build())
          .build();
    }

    public gov.va.api.health.uscorer4.api.resources.Patient patient() {
      return patient("1000003");
    }

    public gov.va.api.health.uscorer4.api.resources.Patient patient(String id) {
      return gov.va.api.health.uscorer4.api.resources.Patient.builder()
          .id(id)
          .resourceType("Patient")
          .extension(
              List.of(
                  gov.va.api.health.r4.api.elements.Extension.builder()
                      .url("https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-race")
                      .extension(
                          List.of(
                              gov.va.api.health.r4.api.elements.Extension.builder()
                                  .url("ombCategory")
                                  .valueCoding(
                                      gov.va.api.health.r4.api.datatypes.Coding.builder()
                                          .system(
                                              "https://www.hl7.org/fhir/us/core/CodeSystem-cdcrec.html")
                                          .code("1002-5")
                                          .display("American Indian or Alaska Native")
                                          .build())
                                  .build(),
                              gov.va.api.health.r4.api.elements.Extension.builder()
                                  .url("text")
                                  .valueString("American Indian or Alaska Native")
                                  .build()))
                      .build(),
                  gov.va.api.health.r4.api.elements.Extension.builder()
                      .url("http://hl7.org/fhir/us/core/StructureDefinition/us-core-birthsex")
                      .valueCode("M")
                      .build()))
          .identifier(
              List.of(
                  gov.va.api.health.r4.api.datatypes.Identifier.builder()
                      .use(gov.va.api.health.r4.api.datatypes.Identifier.IdentifierUse.usual)
                      .type(
                          gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                              .coding(
                                  List.of(
                                      gov.va.api.health.r4.api.datatypes.Coding.builder()
                                          .system("http://hl7.org/fhir/v2/0203")
                                          .code("MR")
                                          .build()))
                              .build())
                      .system("http://va.gov/mvi")
                      .value(id)
                      .assigner(
                          gov.va.api.health.r4.api.elements.Reference.builder()
                              .display("Master Veteran Index")
                              .build())
                      .build(),
                  gov.va.api.health.r4.api.datatypes.Identifier.builder()
                      .use(gov.va.api.health.r4.api.datatypes.Identifier.IdentifierUse.official)
                      .type(
                          gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                              .coding(
                                  List.of(
                                      gov.va.api.health.r4.api.datatypes.Coding.builder()
                                          .system("http://hl7.org/fhir/v2/0203")
                                          .code("SB")
                                          .build()))
                              .build())
                      .system("http://hl7.org/fhir/sid/us-ssn")
                      .value("999-30-3951")
                      .assigner(
                          gov.va.api.health.r4.api.elements.Reference.builder()
                              .display("United States Social Security Number")
                              .build())
                      .build()))
          .name(
              List.of(
                  gov.va.api.health.r4.api.datatypes.HumanName.builder()
                      .use(gov.va.api.health.r4.api.datatypes.HumanName.NameUse.usual)
                      .text("Mr. Tobias236 Wolff180")
                      .family("Wolff180")
                      .given(List.of("Tobias236"))
                      .build()))
          .telecom(
              List.of(
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.phone)
                      .value("5551836103")
                      .use(gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .build(),
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.email)
                      .value("Tobias236.Wolff180@email.example")
                      .use(gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointUse.home)
                      .build()))
          .gender(gov.va.api.health.uscorer4.api.resources.Patient.Gender.male)
          .birthDate("1970-11-14")
          .deceasedDateTime("2001-03-03T15:08:09Z")
          .address(
              List.of(
                  gov.va.api.health.r4.api.datatypes.Address.builder()
                      .line(List.of("111 MacGyver Viaduct"))
                      .city("Anchorage")
                      .state("Alaska")
                      .postalCode("99501")
                      .build()))
          .maritalStatus(
              gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                  .coding(
                      List.of(
                          gov.va.api.health.r4.api.datatypes.Coding.builder()
                              .system("http://hl7.org/fhir/marital-status")
                              .code("M")
                              .display("Married")
                              .build()))
                  .build())
          .build();
    }
  }
}
