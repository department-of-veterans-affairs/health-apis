package gov.va.api.health.dataquery.service.api.swaggerexamples;

import static java.util.Arrays.asList;

import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry.Search;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerImmunization {
  static final Immunization SWAGGER_EXAMPLE_IMMUNIZATION =
      Immunization.builder()
          .resourceType("Immunization")
          .id("1fd82e3a-a95b-5c04-9a68-c8ddf740ea0c")
          .status(Immunization.Status.completed)
          .date("2017-04-24T01:15:52Z")
          .vaccineCode(
              CodeableConcept.builder()
                  .text("meningococcal MCV4P")
                  .coding(
                      asList(
                          Coding.builder()
                              .system("http://hl7.org/fhir/sid/cvx")
                              .code("114")
                              .build()))
                  .build())
          .patient(
              Reference.builder()
                  .reference("https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                  .display("Mr. Aurelio227 Cruickshank494")
                  .build())
          .wasNotGiven(false)
          ._reported(
              Extension.builder()
                  .extension(
                      asList(
                          Extension.builder()
                              .url("http://hl7.org/fhir/StructureDefinition/data-absent-reason")
                              .valueCode("unsupported")
                              .build()))
                  .build())
          .reaction(
              asList(
                  Immunization.Reaction.builder()
                      .detail(Reference.builder().display("Lethargy").build())
                      .build()))
          .build();

  static final Immunization.Bundle SWAGGER_EXAMPLE_IMMUNIZATION_BUNDLE =
      Immunization.Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(1)
          .link(
              asList(
                  BundleLink.builder()
                      .relation(LinkRelation.self)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Immunization?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.first)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Immunization?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.last)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Immunization?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  Immunization.Entry.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/Immunization/1fd82e3a-a95b-5c04-9a68-c8ddf740ea0c")
                      .resource(
                          Immunization.builder()
                              .resourceType("Immunization")
                              .id("1fd82e3a-a95b-5c04-9a68-c8ddf740ea0c")
                              .status(Immunization.Status.completed)
                              .date("2017-04-24T01:15:52Z")
                              .vaccineCode(
                                  CodeableConcept.builder()
                                      .text("meningococcal MCV4P")
                                      .coding(
                                          asList(
                                              Coding.builder()
                                                  .system("http://hl7.org/fhir/sid/cvx")
                                                  .code("114")
                                                  .build()))
                                      .build())
                              .patient(
                                  Reference.builder()
                                      .reference(
                                          "https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                                      .display("Mr. Aurelio227 Cruickshank494")
                                      .build())
                              .wasNotGiven(false)
                              ._reported(
                                  Extension.builder()
                                      .extension(
                                          asList(
                                              Extension.builder()
                                                  .url(
                                                      "http://hl7.org/fhir/StructureDefinition/data-absent-reason")
                                                  .valueCode("unsupported")
                                                  .build()))
                                      .build())
                              .reaction(
                                  asList(
                                      Immunization.Reaction.builder()
                                          .detail(Reference.builder().display("Lethargy").build())
                                          .build()))
                              .build())
                      .search(Search.builder().mode(SearchMode.match).build())
                      .build()))
          .build();
}
