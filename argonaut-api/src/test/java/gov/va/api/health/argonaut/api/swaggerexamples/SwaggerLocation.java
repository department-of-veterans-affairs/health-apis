package gov.va.api.health.argonaut.api.swaggerexamples;

import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerLocation {
  static final SomeClass SWAGGER_EXAMPLE_LOCATION =
      XXX.builder()
          .resourceType("Location")
          .id("96aee2f5-a2ce-588f-8352-f6ea61f0959d")
          .status("active")
          .name("VAMC ALBANY")
          .description("VAMC ALBANY")
          .mode("instance")
          .address(
              XXX.builder()
                  .line(asList("113 Holland Avenue"))
                  .city("ALBANY")
                  .state("NEW YORK")
                  .build())
          .managingOrganization(
              XXX.builder()
                  .reference(
                      "https://www.freedomstream.io/Argonaut/api/Organization/e207c621-f467-5983-a6d1-868f33cefa95")
                  .display("ZZ ALBANY")
                  .build())
          .build();

//  static final String LOCATION =
//	      "{ "
//	          + "  \"resourceType\": \"Location\", "
//	          + "  \"id\": \"96aee2f5-a2ce-588f-8352-f6ea61f0959d\", "
//	          + "  \"status\": \"active\", "
//	          + "  \"name\": \"VAMC ALBANY\", "
//	          + "  \"description\": \"VAMC ALBANY\", "
//	          + "  \"mode\": \"instance\", "
//	          + "  \"address\": { "
//	          + "    \"line\": [ "
//	          + "      \"113 Holland Avenue\" "
//	          + "    ], "
//	          + "    \"city\": \"ALBANY\", "
//	          + "    \"state\": \"NEW YORK\" "
//	          + "  }, "
//	          + "  \"managingOrganization\": { "
//	          + "    \"reference\": \"https://www.freedomstream.io/Argonaut/api/Organization/e207c621-f467-5983-a6d1-868f33cefa95\", "
//	          + "    \"display\": \"ZZ ALBANY\" "
//	          + "  } "
//	          + "} ";
  
  static final SomeClass SWAGGER_EXAMPLE_LOCATION_BUNDLE =
      XXX.builder()
          .resourceType("Bundle")
          .type("searchset")
          .total(1)
          .link(
              asList(
                  XXX.builder()
                      .relation("self")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Location?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("first")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Location?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("last")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Location?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  XXX.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/Location/96aee2f5-a2ce-588f-8352-f6ea61f0959d")
                      .resource(
                          XXX.builder()
                              .fullUrl(
                                  "https://dev-api.va.gov/services/argonaut/v0/Location/96aee2f5-a2ce-588f-8352-f6ea61f0959d")
                              .resource(
                                  XXX.builder()
                                      .resourceType("Location")
                                      .id("96aee2f5-a2ce-588f-8352-f6ea61f0959d")
                                      .status("active")
                                      .name("VAMC ALBANY")
                                      .description("VAMC ALBANY")
                                      .mode("instance")
                                      .address(
                                          XXX.builder()
                                              .line(asList("113 Holland Avenue"))
                                              .city("ALBANY")
                                              .state("NEW YORK")
                                              .build())
                                      .managingOrganization(
                                          XXX.builder()
                                              .reference(
                                                  "https://www.freedomstream.io/Argonaut/api/Organization/e207c621-f467-5983-a6d1-868f33cefa95")
                                              .display("ZZ ALBANY")
                                              .build())
                                      .build())
                              .search(XXX.builder().mode("match").build())
                              .build())
                      .build()))
          .build();
  
//  static final String LOCATION_BUNDLE =
//	      "{ "
//	          + "    \"resourceType\": \"Bundle\", "
//	          + "    \"type\": \"searchset\", "
//	          + "    \"total\": 1, "
//	          + "    \"link\": [ "
//	          + "        { "
//	          + "            \"relation\": \"self\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Location?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\": \"first\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Location?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\":\"last\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Location?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        } "
//	          + "    ], "
//	          + "    \"entry\": [ "
//	          + "        { "
//	          + "            \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Location/96aee2f5-a2ce-588f-8352-f6ea61f0959d\", "
//	          + "            \"resource\": { "
//	          + "                \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Location/96aee2f5-a2ce-588f-8352-f6ea61f0959d\", "
//	          + "                \"resource\": { "
//	          + "                  \"resourceType\": \"Location\", "
//	          + "                  \"id\": \"96aee2f5-a2ce-588f-8352-f6ea61f0959d\", "
//	          + "                  \"status\": \"active\", "
//	          + "                  \"name\": \"VAMC ALBANY\", "
//	          + "                  \"description\": \"VAMC ALBANY\", "
//	          + "                  \"mode\": \"instance\", "
//	          + "                  \"address\": { "
//	          + "                    \"line\": [ "
//	          + "                      \"113 Holland Avenue\" "
//	          + "                    ], "
//	          + "                    \"city\": \"ALBANY\", "
//	          + "                    \"state\": \"NEW YORK\" "
//	          + "                  }, "
//	          + "                  \"managingOrganization\": { "
//	          + "                    \"reference\": \"https://www.freedomstream.io/Argonaut/api/Organization/e207c621-f467-5983-a6d1-868f33cefa95\", "
//	          + "                    \"display\": \"ZZ ALBANY\" "
//	          + "                  } "
//	          + "                }, "
//	          + "                \"search\": { "
//	          + "                    \"mode\": \"match\" "
//	          + "                } "
//	          + "        } "
//	          + "    } "
//	          + "  ] "
//	          + "}";
}
