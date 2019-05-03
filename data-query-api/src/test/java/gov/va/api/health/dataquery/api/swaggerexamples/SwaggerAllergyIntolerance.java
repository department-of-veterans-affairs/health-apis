package gov.va.api.health.dataquery.api.swaggerexamples;

import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry.Search;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dataquery.api.datatypes.Annotation;
import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Coding;
import gov.va.api.health.dataquery.api.elements.Reference;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerAllergyIntolerance {
  static final AllergyIntolerance SWAGGER_EXAMPLE_ALLERGY_INTOLERANCE =
      AllergyIntolerance.builder()
          .resourceType("AllergyIntolerance")
          .id("6f9a021b-07d5-53c8-8cce-b49a694d4ad9")
          .onset("1995-04-30T01:15:52Z")
          .patient(
              Reference.builder()
                  .reference("https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                  .display("Mr. Aurelio227 Cruickshank494")
                  .build())
          .substance(CodeableConcept.builder().text("Allergy to peanuts").build())
          .status(AllergyIntolerance.Status.active)
          .type(AllergyIntolerance.Type.allergy)
          .category(AllergyIntolerance.Category.food)
          .note(
              Annotation.builder().time("1995-04-30T01:15:52Z").text("Allergy to peanuts").build())
          .reaction(
              asList(
                  AllergyIntolerance.Reaction.builder()
                      .manifestation(
                          asList(
                              CodeableConcept.builder()
                                  .coding(
                                      asList(
                                          Coding.builder()
                                              .display("Inflammation of Skin")
                                              .system("urn:oid:2.16.840.1.113883.6.233")
                                              .code("2000001")
                                              .build()))
                                  .text("Inflammation of Skin")
                                  .build()))
                      .certainty(AllergyIntolerance.Certainty.likely)
                      .build()))
          .build();

  static final AllergyIntolerance.Bundle SWAGGER_EXAMPLE_ALLERGY_INTOLERANCE_BUNDLE =
      AllergyIntolerance.Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(1)
          .link(
              asList(
                  BundleLink.builder()
                      .relation(LinkRelation.self)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/AllergyIntolerance?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.first)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/AllergyIntolerance?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.last)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/AllergyIntolerance?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  AllergyIntolerance.Entry.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/AllergyIntolerance/6f9a021b-07d5-53c8-8cce-b49a694d4ad9")
                      .resource(
                          AllergyIntolerance.builder()
                              .resourceType("AllergyIntolerance")
                              .id("e2019e0c-fa38-596d-b966-9b86926959a7")
                              .onset("1995-04-30T01:15:52Z")
                              .patient(
                                  Reference.builder()
                                      .reference(
                                          "https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                                      .display("Mr. Aurelio227 Cruickshank494")
                                      .build())
                              .substance(
                                  CodeableConcept.builder().text("Allergy to bee venom").build())
                              .status(AllergyIntolerance.Status.active)
                              .type(AllergyIntolerance.Type.allergy)
                              .note(
                                  Annotation.builder()
                                      .time("1995-04-30T01:15:52Z")
                                      .text("Allergy to bee venom")
                                      .build())
                              .reaction(
                                  asList(
                                      AllergyIntolerance.Reaction.builder()
                                          .manifestation(
                                              asList(
                                                  CodeableConcept.builder()
                                                      .coding(
                                                          asList(
                                                              Coding.builder()
                                                                  .display("Sneezing and Coughing")
                                                                  .system("urn:oid:2.16.840.1.233")
                                                                  .code("2000004")
                                                                  .build()))
                                                      .text("Sneezing and Coughing")
                                                      .build()))
                                          .build()))
                              .build())
                      .search(Search.builder().mode(SearchMode.match).build())
                      .build()))
          .build();
}
