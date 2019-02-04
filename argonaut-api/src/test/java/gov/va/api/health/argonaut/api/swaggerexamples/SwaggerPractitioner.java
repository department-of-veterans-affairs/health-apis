package gov.va.api.health.argonaut.api.swaggerexamples;

import static java.util.Arrays.asList;

import gov.va.api.health.argonaut.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.argonaut.api.bundle.AbstractEntry.Search;
import gov.va.api.health.argonaut.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.argonaut.api.bundle.BundleLink;
import gov.va.api.health.argonaut.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.argonaut.api.datatypes.CodeableConcept;
import gov.va.api.health.argonaut.api.datatypes.Coding;
import gov.va.api.health.argonaut.api.datatypes.ContactPoint;
import gov.va.api.health.argonaut.api.datatypes.HumanName;
import gov.va.api.health.argonaut.api.elements.Reference;
import gov.va.api.health.argonaut.api.resources.Practitioner;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerPractitioner {
  static final Practitioner SWAGGER_EXAMPLE_PRACTITIONER =
      Practitioner.builder()
          .resourceType("Practitioner")
          .id("9e8531cb-8069-5328-b737-938fa044a4e2")
          .active(true)
          .name(
              HumanName.builder()
                  .use(HumanName.NameUse.usual)
                  .text("CARUNGIN,CHAD REY V")
                  .family(asList("CARUNGIN"))
                  .given(asList("CHAD REY"))
                  .suffix(asList("V"))
                  .build())
          .telecom(
              asList(
                  ContactPoint.builder()
                      .system(ContactPoint.ContactPointSystem.phone)
                      .value("318-8387")
                      .use(ContactPoint.ContactPointUse.work)
                      .build(),
                  ContactPoint.builder()
                      .system(ContactPoint.ContactPointSystem.phone)
                      .value("638334566")
                      .use(ContactPoint.ContactPointUse.home)
                      .build()))
          .gender(Practitioner.Gender.male)
          .birthDate("1964-02-16")
          .practitionerRole(
              asList(
                  Practitioner.PractitionerRole.builder()
                      .managingOrganization(
                          Reference.builder()
                              .reference(
                                  "https://api.va.gov/services/argonaut/v0/Organization/a29c7d77-fbae-5852-a905-1b18385b0339")
                              .display("MANILA-RO")
                              .build())
                      .role(
                          CodeableConcept.builder()
                              .coding(
                                  asList(
                                      Coding.builder()
                                          .system("http://hl7.org/fhir/practitioner-role")
                                          .code("doctor")
                                          .display("Doctor")
                                          .build()))
                              .build())
                      .location(
                          asList(
                              Reference.builder()
                                  .reference(
                                      "https://api.va.gov/services/argonaut/v0/Location/fb2a6389-0512-5ed1-9b49-811d5f3fdf3d")
                                  .display("PULMO CONSULT")
                                  .build(),
                              Reference.builder()
                                  .reference(
                                      "https://api.va.gov/services/argonaut/v0/Location/be4abb7c-87ea-57de-9347-df849897ea28")
                                  .display("ZZORANGE")
                                  .build(),
                              Reference.builder()
                                  .reference(
                                      "https://api.va.gov/services/argonaut/v0/Location/c7c5e616-4304-5956-804f-36038889f712")
                                  .display("ZZZ MAN PACT ORANGE")
                                  .build(),
                              Reference.builder()
                                  .reference(
                                      "https://api.va.gov/services/argonaut/v0/Location/2a560bee-9f00-5758-aaa3-21c0f929a7f6")
                                  .display("ZZORANGE INTERNAL MED CLINIC")
                                  .build(),
                              Reference.builder()
                                  .reference(
                                      "https://api.va.gov/services/argonaut/v0/Location/ada6bbf5-f121-582e-b832-13387fe4feff")
                                  .display("MAN PACT WALK-IN")
                                  .build(),
                              Reference.builder()
                                  .reference(
                                      "https://api.va.gov/services/argonaut/v0/Location/d05e1bc2-9527-51e9-bc35-0132f2af3c53")
                                  .display("E-CONSULT PULMO")
                                  .build(),
                              Reference.builder()
                                  .reference(
                                      "https://api.va.gov/services/argonaut/v0/Location/1cdb2958-e0fb-5711-8907-d582662e1be7")
                                  .display("MNL-SECMSG-PULMO")
                                  .build()))
                      .healthcareService(
                          asList(
                              Reference.builder()
                                  .display("PROFESSIONAL SERVICES DIVISION")
                                  .build()))
                      .build()))
          .build();

  static final Practitioner.Bundle SWAGGER_EXAMPLE_PRACTITIONER_BUNDLE =
      Practitioner.Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(1)
          .link(
              asList(
                  BundleLink.builder()
                      .relation(LinkRelation.self)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Practitioner?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.first)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Practitioner?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.last)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Practitioner?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  Practitioner.Entry.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/Practitioner/9e8531cb-8069-5328-b737-938fa044a4e2")
                      .resource(
                          Practitioner.builder()
                              .resourceType("Practitioner")
                              .id("9e8531cb-8069-5328-b737-938fa044a4e2")
                              .active(true)
                              .name(
                                  HumanName.builder()
                                      .use(HumanName.NameUse.usual)
                                      .text("CARUNGIN,CHAD REY V")
                                      .family(asList("CARUNGIN"))
                                      .given(asList("CHAD REY"))
                                      .suffix(asList("V"))
                                      .build())
                              .telecom(
                                  asList(
                                      ContactPoint.builder()
                                          .system(ContactPoint.ContactPointSystem.phone)
                                          .value("318-8387")
                                          .use(ContactPoint.ContactPointUse.work)
                                          .build(),
                                      ContactPoint.builder()
                                          .system(ContactPoint.ContactPointSystem.phone)
                                          .value("638334566")
                                          .use(ContactPoint.ContactPointUse.home)
                                          .build()))
                              .gender(Practitioner.Gender.male)
                              .birthDate("1964-02-16")
                              .practitionerRole(
                                  asList(
                                      Practitioner.PractitionerRole.builder()
                                          .managingOrganization(
                                              Reference.builder()
                                                  .reference(
                                                      "https://api.va.gov/services/argonaut/v0/Organization/a29c7d77-fbae-5852-a905-1b18385b0339")
                                                  .display("MANILA-RO")
                                                  .build())
                                          .role(
                                              CodeableConcept.builder()
                                                  .coding(
                                                      asList(
                                                          Coding.builder()
                                                              .system(
                                                                  "http://hl7.org/fhir/practitioner-role")
                                                              .code("doctor")
                                                              .display("Doctor")
                                                              .build()))
                                                  .build())
                                          .location(
                                              asList(
                                                  Reference.builder()
                                                      .reference(
                                                          "https://api.va.gov/services/argonaut/v0/Location/fb2a6389-0512-5ed1-9b49-811d5f3fdf3d")
                                                      .display("PULMO CONSULT")
                                                      .build(),
                                                  Reference.builder()
                                                      .reference(
                                                          "https://api.va.gov/services/argonaut/v0/Location/be4abb7c-87ea-57de-9347-df849897ea28")
                                                      .display("ZZORANGE")
                                                      .build(),
                                                  Reference.builder()
                                                      .reference(
                                                          "https://api.va.gov/services/argonaut/v0/Location/c7c5e616-4304-5956-804f-36038889f712")
                                                      .display("ZZZ MAN PACT ORANGE")
                                                      .build(),
                                                  Reference.builder()
                                                      .reference(
                                                          "https://api.va.gov/services/argonaut/v0/Location/2a560bee-9f00-5758-aaa3-21c0f929a7f6")
                                                      .display("ZZORANGE INTERNAL MED CLINIC")
                                                      .build(),
                                                  Reference.builder()
                                                      .reference(
                                                          "https://api.va.gov/services/argonaut/v0/Location/ada6bbf5-f121-582e-b832-13387fe4feff")
                                                      .display("MAN PACT WALK-IN")
                                                      .build(),
                                                  Reference.builder()
                                                      .reference(
                                                          "https://api.va.gov/services/argonaut/v0/Location/d05e1bc2-9527-51e9-bc35-0132f2af3c53")
                                                      .display("E-CONSULT PULMO")
                                                      .build(),
                                                  Reference.builder()
                                                      .reference(
                                                          "https://api.va.gov/services/argonaut/v0/Location/1cdb2958-e0fb-5711-8907-d582662e1be7")
                                                      .display("MNL-SECMSG-PULMO")
                                                      .build()))
                                          .healthcareService(
                                              asList(
                                                  Reference.builder()
                                                      .display("PROFESSIONAL SERVICES DIVISION")
                                                      .build()))
                                          .build()))
                              .build())
                      .search(Search.builder().mode(SearchMode.match).build())
                      .build()))
          .build();
}
