package gov.va.api.health.dataquery.api.swaggerexamples;

import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry.Search;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dataquery.api.datatypes.Address;
import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Coding;
import gov.va.api.health.dataquery.api.datatypes.ContactPoint;
import gov.va.api.health.dataquery.api.datatypes.HumanName;
import gov.va.api.health.dataquery.api.datatypes.Identifier;
import gov.va.api.health.dataquery.api.elements.Extension;
import gov.va.api.health.dataquery.api.resources.Patient;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerPatient {
  static final Patient SWAGGER_EXAMPLE_PATIENT =
      Patient.builder()
          .id("2000163")
          .resourceType("Patient")
          .extension(
              asList(
                  Extension.builder()
                      .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
                      .extension(
                          asList(
                              Extension.builder()
                                  .url("ombCategory")
                                  .valueCoding(
                                      Coding.builder()
                                          .system("http://hl7.org/fhir/v3/Race")
                                          .code("2016-3")
                                          .display("White")
                                          .build())
                                  .build(),
                              Extension.builder().url("text").valueString("White").build()))
                      .build(),
                  Extension.builder()
                      .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
                      .extension(
                          asList(
                              Extension.builder()
                                  .url("ombCategory")
                                  .valueCoding(
                                      Coding.builder()
                                          .system("http://hl7.org/fhir/ValueSet/v3-Ethnicity")
                                          .code("2186-5")
                                          .display("Not Hispanic or Latino")
                                          .build())
                                  .build(),
                              Extension.builder()
                                  .url("text")
                                  .valueString("Not Hispanic or Latino")
                                  .build()))
                      .build(),
                  Extension.builder()
                      .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-birthsex")
                      .valueCode("M")
                      .build()))
          .identifier(
              asList(
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
                      .value("2000163")
                      .build(),
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
                      .value("999-61-4803")
                      .build()))
          .name(
              asList(
                  HumanName.builder()
                      .use(HumanName.NameUse.usual)
                      .text("Mr. Aurelio227 Cruickshank494")
                      .family(asList("Cruickshank494"))
                      .given(asList("Aurelio227"))
                      .build()))
          .telecom(
              asList(
                  ContactPoint.builder()
                      .system(ContactPoint.ContactPointSystem.phone)
                      .value("5555191065")
                      .use(ContactPoint.ContactPointUse.mobile)
                      .build(),
                  ContactPoint.builder()
                      .system(ContactPoint.ContactPointSystem.email)
                      .value("Aurelio227.Cruickshank494@email.example")
                      .build()))
          .gender(Patient.Gender.male)
          .birthDate("1995-02-06")
          .deceasedBoolean(false)
          .address(
              asList(
                  Address.builder()
                      .line(asList("909 Rohan Highlands"))
                      .city("Mesa")
                      .state("Arizona")
                      .postalCode("85120")
                      .build()))
          .maritalStatus(
              CodeableConcept.builder()
                  .coding(
                      asList(
                          Coding.builder()
                              .system("http://hl7.org/fhir/v3/NullFlavor")
                              .code("UNK")
                              .display("unknown")
                              .build()))
                  .build())
          .build();

  static final Patient.Bundle SWAGGER_EXAMPLE_PATIENT_BUNDLE =
      Patient.Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(1)
          .link(
              asList(
                  BundleLink.builder()
                      .relation(LinkRelation.self)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Patient?_id=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.first)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Patient?_id=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.last)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Patient?_id=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  Patient.Entry.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/Patient/1017283148V813263")
                      .resource(
                          Patient.builder()
                              .id("2000163")
                              .resourceType("Patient")
                              .extension(
                                  asList(
                                      Extension.builder()
                                          .url(
                                              "http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
                                          .extension(
                                              asList(
                                                  Extension.builder()
                                                      .url("ombCategory")
                                                      .valueCoding(
                                                          Coding.builder()
                                                              .system("http://hl7.org/fhir/v3/Race")
                                                              .code("2016-3")
                                                              .display("White")
                                                              .build())
                                                      .build(),
                                                  Extension.builder()
                                                      .url("text")
                                                      .valueString("White")
                                                      .build()))
                                          .build(),
                                      Extension.builder()
                                          .url(
                                              "http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
                                          .extension(
                                              asList(
                                                  Extension.builder()
                                                      .url("ombCategory")
                                                      .valueCoding(
                                                          Coding.builder()
                                                              .system(
                                                                  "http://hl7.org/fhir/ValueSet/v3-Ethnicity")
                                                              .code("2186-5")
                                                              .display("Not Hispanic or Latino")
                                                              .build())
                                                      .build(),
                                                  Extension.builder()
                                                      .url("text")
                                                      .valueString("Not Hispanic or Latino")
                                                      .build()))
                                          .build(),
                                      Extension.builder()
                                          .url(
                                              "http://fhir.org/guides/argonaut/StructureDefinition/argo-birthsex")
                                          .valueCode("M")
                                          .build()))
                              .identifier(
                                  asList(
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
                                          .value("2000163")
                                          .build(),
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
                                          .value("999-61-4803")
                                          .build()))
                              .name(
                                  asList(
                                      HumanName.builder()
                                          .use(HumanName.NameUse.usual)
                                          .text("Mr. Aurelio227 Cruickshank494")
                                          .family(asList("Cruickshank494"))
                                          .given(asList("Aurelio227"))
                                          .build()))
                              .telecom(
                                  asList(
                                      ContactPoint.builder()
                                          .system(ContactPoint.ContactPointSystem.phone)
                                          .value("5555191065")
                                          .use(ContactPoint.ContactPointUse.mobile)
                                          .build(),
                                      ContactPoint.builder()
                                          .system(ContactPoint.ContactPointSystem.email)
                                          .value("Aurelio227.Cruickshank494@email.example")
                                          .build()))
                              .gender(Patient.Gender.male)
                              .birthDate("1995-02-06")
                              .deceasedBoolean(false)
                              .address(
                                  asList(
                                      Address.builder()
                                          .line(asList("909 Rohan Highlands"))
                                          .city("Mesa")
                                          .state("Arizona")
                                          .postalCode("85120")
                                          .build()))
                              .maritalStatus(
                                  CodeableConcept.builder()
                                      .coding(
                                          asList(
                                              Coding.builder()
                                                  .system("http://hl7.org/fhir/v3/NullFlavor")
                                                  .code("UNK")
                                                  .display("unknown")
                                                  .build()))
                                      .build())
                              .build())
                      .search(Search.builder().mode(SearchMode.match).build())
                      .build()))
          .build();
}
