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
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.resources.Organization;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerOrganization {
  static final Organization SWAGGER_EXAMPLE_ORGANIZATION =
      Organization.builder()
          .resourceType("Organization")
          .id("6a96677d-f487-52bb-befd-6c90c7f49fa6")
          .active(true)
          .type(
              CodeableConcept.builder()
                  .coding(
                      asList(
                          Coding.builder()
                              .system("http://hl7.org/fhir/organization-type")
                              .code("prov")
                              .display("Healthcare Provider")
                              .build()))
                  .build())
          .name("MANILA-RO")
          .address(
              asList(
                  Address.builder()
                      .line(asList("1501 ROXAS BLVD"))
                      .city("PASAY CITY, METRO MANILA")
                      .state("PH")
                      .postalCode("96515-1100")
                      .build()))
          .partOf(
              Reference.builder()
                  .reference(
                      "https://api.va.gov/services/argonaut/v0/Organization/966f5985-6db7-5c0a-b809-54fcf73d3e1d")
                  .display("VA")
                  .build())
          .build();

  static final Organization.Bundle SWAGGER_EXAMPLE_ORGANIZATION_BUNDLE =
      Organization.Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(1)
          .link(
              asList(
                  BundleLink.builder()
                      .relation(LinkRelation.self)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Organization?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.first)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Organization?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.last)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Organization?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  Organization.Entry.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/Organization/6a96677d-f487-52bb-befd-6c90c7f49fa6")
                      .resource(
                          Organization.builder()
                              .resourceType("Organization")
                              .id("6a96677d-f487-52bb-befd-6c90c7f49fa6")
                              .active(true)
                              .type(
                                  CodeableConcept.builder()
                                      .coding(
                                          asList(
                                              Coding.builder()
                                                  .system("http://hl7.org/fhir/organization-type")
                                                  .code("prov")
                                                  .display("Healthcare Provider")
                                                  .build()))
                                      .build())
                              .name("MANILA-RO")
                              .address(
                                  asList(
                                      Address.builder()
                                          .line(asList("1501 ROXAS BLVD"))
                                          .city("PASAY CITY, METRO MANILA")
                                          .state("PH")
                                          .postalCode("96515-1100")
                                          .build()))
                              .partOf(
                                  Reference.builder()
                                      .reference(
                                          "https://api.va.gov/services/argonaut/v0/Organization/966f5985-6db7-5c0a-b809-54fcf73d3e1d")
                                      .display("VA")
                                      .build())
                              .build())
                      .search(Search.builder().mode(SearchMode.match).build())
                      .build()))
          .build();
}
