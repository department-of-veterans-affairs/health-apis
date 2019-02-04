package gov.va.api.health.argonaut.api.swaggerexamples;

import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerOrganization {
  static final SomeClass SWAGGER_EXAMPLE_ORGANIZATON =
      XXX.builder()
          .resourceType("Organization")
          .id("6a96677d-f487-52bb-befd-6c90c7f49fa6")
          .active("true")
          .type(
              XXX.builder()
                  .coding(
                      asList(
                          XXX.builder()
                              .system("http://hl7.org/fhir/organization-type")
                              .code("prov")
                              .display("Healthcare Provider")
                              .build()))
                  .build())
          .name("MANILA-RO")
          .address(
              asList(
                  XXX.builder()
                      .line(asList("1501 ROXAS BLVD"))
                      .city("PASAY CITY, METRO MANILA")
                      .state("PH")
                      .postalCode("96515-1100")
                      .build()))
          .partOf(
              XXX.builder()
                  .reference(
                      "https://api.va.gov/services/argonaut/v0/Organization/966f5985-6db7-5c0a-b809-54fcf73d3e1d")
                  .display("VA")
                  .build())
          .build();

//  static final String ORGANZIATON =
//	      "{ "
//	          + "    \"resourceType\": \"Organization\", "
//	          + "    \"id\": \"6a96677d-f487-52bb-befd-6c90c7f49fa6\", "
//	          + "    \"active\": \"true\", "
//	          + "    \"type\": { "
//	          + "        \"coding\": [ "
//	          + "            { "
//	          + "                \"system\": \"http://hl7.org/fhir/organization-type\", "
//	          + "                \"code\": \"prov\", "
//	          + "                \"display\": \"Healthcare Provider\" "
//	          + "            } "
//	          + "        ] "
//	          + "    }, "
//	          + "    \"name\": \"MANILA-RO\", "
//	          + "    \"address\": [ "
//	          + "        { "
//	          + "            \"line\": [ "
//	          + "                \"1501 ROXAS BLVD\" "
//	          + "            ], "
//	          + "            \"city\": \"PASAY CITY, METRO MANILA\", "
//	          + "            \"state\": \"PH\", "
//	          + "            \"postalCode\": \"96515-1100\" "
//	          + "        } "
//	          + "    ], "
//	          + "    \"partOf\": { "
//	          + "        \"reference\": \"https://api.va.gov/services/argonaut/v0/Organization/966f5985-6db7-5c0a-b809-54fcf73d3e1d\", "
//	          + "        \"display\": \"VA\" "
//	          + "    } "
//	          + "} ";
  
  static final SomeClass SWAGGER_EXAMPLE_ORGANIZATION_BUNDLE =
      XXX.builder()
          .resourceType("Bundle")
          .type("searchset")
          .total(1)
          .link(
              asList(
                  XXX.builder()
                      .relation("self")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Organization?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("first")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Organization?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("last")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Organization?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  XXX.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/Organization/6a96677d-f487-52bb-befd-6c90c7f49fa6")
                      .resource(
                          XXX.builder()
                              .fullUrl(
                                  "https://dev-api.va.gov/services/argonaut/v0/Organization/6a96677d-f487-52bb-befd-6c90c7f49fa6")
                              .resource(
                                  XXX.builder()
                                      .resourceType("Organization")
                                      .id("6a96677d-f487-52bb-befd-6c90c7f49fa6")
                                      .active("true")
                                      .type(
                                          XXX.builder()
                                              .coding(
                                                  asList(
                                                      XXX.builder()
                                                          .system(
                                                              "http://hl7.org/fhir/organization-type")
                                                          .code("prov")
                                                          .display("Healthcare Provider")
                                                          .build()))
                                              .build())
                                      .name("MANILA-RO")
                                      .address(
                                          asList(
                                              XXX.builder()
                                                  .line(asList("1501 ROXAS BLVD"))
                                                  .city("PASAY CITY, METRO MANILA")
                                                  .state("PH")
                                                  .postalCode("96515-1100")
                                                  .build()))
                                      .partOf(
                                          XXX.builder()
                                              .reference(
                                                  "https://api.va.gov/services/argonaut/v0/Organization/966f5985-6db7-5c0a-b809-54fcf73d3e1d")
                                              .display("VA")
                                              .build())
                                      .build())
                              .search(XXX.builder().mode("match").build())
                              .build())
                      .build()))
          .build();
  
//  static final String ORGANZIATION_BUNDLE =
//	      "{ "
//	          + "    \"resourceType\": \"Bundle\", "
//	          + "    \"type\": \"searchset\", "
//	          + "    \"total\": 1, "
//	          + "    \"link\": [ "
//	          + "        { "
//	          + "            \"relation\": \"self\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Organization?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\": \"first\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Organization?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\":\"last\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Organization?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        } "
//	          + "    ], "
//	          + "    \"entry\": [ "
//	          + "        { "
//	          + "            \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Organization/6a96677d-f487-52bb-befd-6c90c7f49fa6\", "
//	          + "            \"resource\": { "
//	          + "                \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Organization/6a96677d-f487-52bb-befd-6c90c7f49fa6\", "
//	          + "                \"resource\": { "
//	          + "                    \"resourceType\": \"Organization\", "
//	          + "                    \"id\": \"6a96677d-f487-52bb-befd-6c90c7f49fa6\", "
//	          + "                    \"active\": \"true\", "
//	          + "                    \"type\": { "
//	          + "                        \"coding\": [ "
//	          + "                            { "
//	          + "                                \"system\": \"http://hl7.org/fhir/organization-type\", "
//	          + "                                \"code\": \"prov\", "
//	          + "                                \"display\": \"Healthcare Provider\" "
//	          + "                            } "
//	          + "                        ] "
//	          + "                    }, "
//	          + "                    \"name\": \"MANILA-RO\", "
//	          + "                    \"address\": [ "
//	          + "                        { "
//	          + "                            \"line\": [ "
//	          + "                                \"1501 ROXAS BLVD\" "
//	          + "                            ], "
//	          + "                            \"city\": \"PASAY CITY, METRO MANILA\", "
//	          + "                            \"state\": \"PH\", "
//	          + "                            \"postalCode\": \"96515-1100\" "
//	          + "                        } "
//	          + "                    ], "
//	          + "                    \"partOf\": { "
//	          + "                        \"reference\": \"https://api.va.gov/services/argonaut/v0/Organization/966f5985-6db7-5c0a-b809-54fcf73d3e1d\", "
//	          + "                        \"display\": \"VA\" "
//	          + "                    } "
//	          + "                }, "
//	          + "            \"search\": { "
//	          + "                \"mode\": \"match\" "
//	          + "            } "
//	          + "        } "
//	          + "    } "
//	          + "  ] "
//	          + "}";
}
