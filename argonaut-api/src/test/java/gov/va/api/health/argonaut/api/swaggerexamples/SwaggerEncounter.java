package gov.va.api.health.argonaut.api.swaggerexamples;

import gov.va.api.health.argonaut.api.resources.Encounter;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerEncounter {
  static final Encounter SWAGGER_EXAMPLE_ENCOUNTER =
      XXX.builder()
          .resourceType("Encounter")
          .id("dc7f6fcc-41f9-5be9-a364-0b0aa938917a")
          .status("finished")
          .encounterClass("emergency")
          .patient(
              XXX.builder()
                  .reference("https://www.freedomstream.io/Argonaut/api/Patient/185601V825290")
                  .display("VETERAN,JOHN Q")
                  .build())
          .participant(
              asList(
                  XXX.builder()
                      .individual(
                          XXX.builder()
                              .reference(
                                  "https://www.freedomstream.io/Argonaut/api/Practitioner/d3f56fac-c7c7-547b-a1e9-de3173d4b628")
                              .display("CARTER,DOC F")
                              .build())
                      .build(),
                  XXX.builder()
                      .individual(
                          XXX.builder()
                              .reference(
                                  "https://www.freedomstream.io/Argonaut/api/Practitioner/93e4e3c3-8d8c-5b53-996f-6047d0232231")
                              .display("JONES,DOC B")
                              .build())
                      .build(),
                  XXX.builder()
                      .individual(
                          XXX.builder()
                              .reference(
                                  "https://www.freedomstream.io/Argonaut/api/Practitioner/5e27c469-82e4-5725-babb-49cf7eee948f")
                              .display("CARTER,DOC F")
                              .build())
                      .build(),
                  XXX.builder()
                      .individual(
                          XXX.builder()
                              .reference(
                                  "https://www.freedomstream.io/Argonaut/api/Practitioner/1b489f04-19e7-5c7d-9140-ce773c158edd")
                              .display("SMITH,DOC A")
                              .build())
                      .build()))
          .appointment(
              XXX.builder()
                  .reference(
                      "https://www.freedomstream.io/Argonaut/api/Appointment/615f31df-f0c7-5100-ac42-7fb952c630d0")
                  .build())
          .indication(
              asList(
                  XXX.builder()
                      .reference(
                          "https://www.freedomstream.io/Argonaut/api/Condition/37d89dc5-45f5-5a2e-9db9-2b17c0d7f318")
                      .display("Chronic asthmatic bronchitis (SNOMED CT 195949008)")
                      .build()))
          .location(
              asList(
                  XXX.builder()
                      .location(
                          XXX.builder()
                              .reference(
                                  "https://www.freedomstream.io/Argonaut/api/Location/eb094a51-ad31-5b6b-b627-96aac4b02b1c")
                              .display("GNV ED")
                              .build())
                      .build()))
          .serviceProvider(
              XXX.builder()
                  .reference(
                      "https://www.freedomstream.io/Argonaut/api/Institution/ed3f9a41-397a-5100-b177-ae3815e5c370")
                  .display("N. FLORIDA/S. GEORGIA HCS")
                  .build())
          .build();


//  static final String ENCOUNTER =
//      "{ "
//          + "  \"resourceType\": \"Encounter\", "
//          + "  \"id\": \"dc7f6fcc-41f9-5be9-a364-0b0aa938917a\", "
//          + "  \"status\": \"finished\", "
//          + "  \"class\": \"emergency\", "
//          + "  \"patient\": { "
//          + "    \"reference\": \"https://www.freedomstream.io/Argonaut/api/Patient/185601V825290\", "
//          + "    \"display\": \"VETERAN,JOHN Q\" "
//          + "  }, "
//          + "  \"participant\": [ "
//          + "    { "
//          + "      \"individual\": { "
//          + "        \"reference\": \"https://www.freedomstream.io/Argonaut/api/Practitioner/d3f56fac-c7c7-547b-a1e9-de3173d4b628\", "
//          + "        \"display\": \"CARTER,DOC F\" "
//          + "      } "
//          + "    }, "
//          + "    { "
//          + "      \"individual\": { "
//          + "        \"reference\": \"https://www.freedomstream.io/Argonaut/api/Practitioner/93e4e3c3-8d8c-5b53-996f-6047d0232231\", "
//          + "        \"display\": \"JONES,DOC B\" "
//          + "      } "
//          + "    }, "
//          + "    { "
//          + "      \"individual\": { "
//          + "        \"reference\": \"https://www.freedomstream.io/Argonaut/api/Practitioner/5e27c469-82e4-5725-babb-49cf7eee948f\", "
//          + "        \"display\": \"CARTER,DOC F\" "
//          + "      } "
//          + "    }, "
//          + "    { "
//          + "      \"individual\": { "
//          + "        \"reference\": \"https://www.freedomstream.io/Argonaut/api/Practitioner/1b489f04-19e7-5c7d-9140-ce773c158edd\", "
//          + "        \"display\": \"SMITH,DOC A\" "
//          + "      } "
//          + "    } "
//          + "  ], "
//          + "  \"appointment\": { "
//          + "    \"reference\": \"https://www.freedomstream.io/Argonaut/api/Appointment/615f31df-f0c7-5100-ac42-7fb952c630d0\" "
//          + "  }, "
//          + "  \"indication\": [ "
//          + "    { "
//          + "      \"reference\": \"https://www.freedomstream.io/Argonaut/api/Condition/37d89dc5-45f5-5a2e-9db9-2b17c0d7f318\", "
//          + "      \"display\": \"Chronic asthmatic bronchitis (SNOMED CT 195949008)\" "
//          + "    } "
//          + "  ], "
//          + "  \"location\": [ "
//          + "    { "
//          + "      \"location\": { "
//          + "        \"reference\": \"https://www.freedomstream.io/Argonaut/api/Location/eb094a51-ad31-5b6b-b627-96aac4b02b1c\", "
//          + "        \"display\": \"GNV ED\" "
//          + "      } "
//          + "    } "
//          + "  ], "
//          + "  \"serviceProvider\": { "
//          + "    \"reference\": \"https://www.freedomstream.io/Argonaut/api/Institution/ed3f9a41-397a-5100-b177-ae3815e5c370\", "
//          + "    \"display\": \"N. FLORIDA/S. GEORGIA HCS\" "
//          + "  } "
//          + "} ";
  
  static final Encounter.Bundle SWAGGER_EXAMPLE_ENCOUNTER_BUNDLE =
      XXX.builder()
          .resourceType("Bundle")
          .type("searchset")
          .total(1)
          .link(
              asList(
                  XXX.builder()
                      .relation("self")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Encounter?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("first")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Encounter?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("last")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Encounter?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  XXX.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/Encounter/dc7f6fcc-41f9-5be9-a364-0b0aa938917a")
                      .resource(
                          XXX.builder()
                              .fullUrl(
                                  "https://dev-api.va.gov/services/argonaut/v0/Encounter/dc7f6fcc-41f9-5be9-a364-0b0aa938917a")
                              .resource(
                                  XXX.builder()
                                      .resourceType("Encounter")
                                      .id("dc7f6fcc-41f9-5be9-a364-0b0aa938917a")
                                      .status("finished")
                                      .encounterClass("emergency")
                                      .patient(
                                          XXX.builder()
                                              .reference(
                                                  "https://www.freedomstream.io/Argonaut/api/Patient/185601V825290")
                                              .display("VETERAN,JOHN Q")
                                              .build())
                                      .participant(
                                          asList(
                                              XXX.builder()
                                                  .individual(
                                                      XXX.builder()
                                                          .reference(
                                                              "https://www.freedomstream.io/Argonaut/api/Practitioner/d3f56fac-c7c7-547b-a1e9-de3173d4b628")
                                                          .display("CARTER,DOC F")
                                                          .build())
                                                  .build(),
                                              XXX.builder()
                                                  .individual(
                                                      XXX.builder()
                                                          .reference(
                                                              "https://www.freedomstream.io/Argonaut/api/Practitioner/93e4e3c3-8d8c-5b53-996f-6047d0232231")
                                                          .display("JONES,DOC B")
                                                          .build())
                                                  .build(),
                                              XXX.builder()
                                                  .individual(
                                                      XXX.builder()
                                                          .reference(
                                                              "https://www.freedomstream.io/Argonaut/api/Practitioner/5e27c469-82e4-5725-babb-49cf7eee948f")
                                                          .display("CARTER,DOC F")
                                                          .build())
                                                  .build(),
                                              XXX.builder()
                                                  .individual(
                                                      XXX.builder()
                                                          .reference(
                                                              "https://www.freedomstream.io/Argonaut/api/Practitioner/1b489f04-19e7-5c7d-9140-ce773c158edd")
                                                          .display("SMITH,DOC A")
                                                          .build())
                                                  .build()))
                                      .appointment(
                                          XXX.builder()
                                              .reference(
                                                  "https://www.freedomstream.io/Argonaut/api/Appointment/615f31df-f0c7-5100-ac42-7fb952c630d0")
                                              .build())
                                      .indication(
                                          asList(
                                              XXX.builder()
                                                  .reference(
                                                      "https://www.freedomstream.io/Argonaut/api/Condition/37d89dc5-45f5-5a2e-9db9-2b17c0d7f318")
                                                  .display(
                                                      "Chronic asthmatic bronchitis (SNOMED CT 195949008)")
                                                  .build()))
                                      .location(
                                          asList(
                                              XXX.builder()
                                                  .location(
                                                      XXX.builder()
                                                          .reference(
                                                              "https://www.freedomstream.io/Argonaut/api/Location/eb094a51-ad31-5b6b-b627-96aac4b02b1c")
                                                          .display("GNV ED")
                                                          .build())
                                                  .build()))
                                      .serviceProvider(
                                          XXX.builder()
                                              .reference(
                                                  "https://www.freedomstream.io/Argonaut/api/Institution/ed3f9a41-397a-5100-b177-ae3815e5c370")
                                              .display("N. FLORIDA/S. GEORGIA HCS")
                                              .build())
                                      .build())
                              .search(XXX.builder().mode("match").build())
                              .build())
                      .build()))
          .build();
  
//  static final String ENCOUNTER_BUNDLE =
//	      "{ "
//	          + "    \"resourceType\": \"Bundle\", "
//	          + "    \"type\": \"searchset\", "
//	          + "    \"total\": 1, "
//	          + "    \"link\": [ "
//	          + "        { "
//	          + "            \"relation\": \"self\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Encounter?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\": \"first\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Encounter?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\":\"last\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Encounter?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        } "
//	          + "    ], "
//	          + "    \"entry\": [ "
//	          + "        { "
//	          + "            \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Encounter/dc7f6fcc-41f9-5be9-a364-0b0aa938917a\", "
//	          + "            \"resource\": { "
//	          + "                \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Encounter/dc7f6fcc-41f9-5be9-a364-0b0aa938917a\", "
//	          + "                \"resource\": { "
//	          + "                   \"resourceType\": \"Encounter\", "
//	          + "                   \"id\": \"dc7f6fcc-41f9-5be9-a364-0b0aa938917a\", "
//	          + "                   \"status\": \"finished\", "
//	          + "                   \"class\": \"emergency\", "
//	          + "                   \"patient\": { "
//	          + "                     \"reference\": \"https://www.freedomstream.io/Argonaut/api/Patient/185601V825290\", "
//	          + "                     \"display\": \"VETERAN,JOHN Q\" "
//	          + "                   }, "
//	          + "                   \"participant\": [ "
//	          + "                     { "
//	          + "                       \"individual\": { "
//	          + "                         \"reference\": \"https://www.freedomstream.io/Argonaut/api/Practitioner/d3f56fac-c7c7-547b-a1e9-de3173d4b628\", "
//	          + "                         \"display\": \"CARTER,DOC F\" "
//	          + "                       } "
//	          + "                     }, "
//	          + "                     { "
//	          + "                       \"individual\": { "
//	          + "                         \"reference\": \"https://www.freedomstream.io/Argonaut/api/Practitioner/93e4e3c3-8d8c-5b53-996f-6047d0232231\", "
//	          + "                         \"display\": \"JONES,DOC B\" "
//	          + "                       } "
//	          + "                     }, "
//	          + "                     { "
//	          + "                       \"individual\": { "
//	          + "                         \"reference\": \"https://www.freedomstream.io/Argonaut/api/Practitioner/5e27c469-82e4-5725-babb-49cf7eee948f\", "
//	          + "                         \"display\": \"CARTER,DOC F\" "
//	          + "                       } "
//	          + "                     }, "
//	          + "                     { "
//	          + "                       \"individual\": { "
//	          + "                         \"reference\": \"https://www.freedomstream.io/Argonaut/api/Practitioner/1b489f04-19e7-5c7d-9140-ce773c158edd\", "
//	          + "                         \"display\": \"SMITH,DOC A\" "
//	          + "                       } "
//	          + "                     } "
//	          + "                   ], "
//	          + "                   \"appointment\": { "
//	          + "                     \"reference\": \"https://www.freedomstream.io/Argonaut/api/Appointment/615f31df-f0c7-5100-ac42-7fb952c630d0\" "
//	          + "                   }, "
//	          + "                   \"indication\": [ "
//	          + "                     { "
//	          + "                       \"reference\": \"https://www.freedomstream.io/Argonaut/api/Condition/37d89dc5-45f5-5a2e-9db9-2b17c0d7f318\", "
//	          + "                       \"display\": \"Chronic asthmatic bronchitis (SNOMED CT 195949008)\" "
//	          + "                     } "
//	          + "                   ], "
//	          + "                   \"location\": [ "
//	          + "                     { "
//	          + "                       \"location\": { "
//	          + "                         \"reference\": \"https://www.freedomstream.io/Argonaut/api/Location/eb094a51-ad31-5b6b-b627-96aac4b02b1c\", "
//	          + "                         \"display\": \"GNV ED\" "
//	          + "                       } "
//	          + "                     } "
//	          + "                   ], "
//	          + "                   \"serviceProvider\": { "
//	          + "                     \"reference\": \"https://www.freedomstream.io/Argonaut/api/Institution/ed3f9a41-397a-5100-b177-ae3815e5c370\", "
//	          + "                     \"display\": \"N. FLORIDA/S. GEORGIA HCS\" "
//	          + "                   } "
//	          + "            }, "
//	          + "            \"search\": { "
//	          + "                \"mode\": \"match\" "
//	          + "            } "
//	          + "        } "
//	          + "    } "
//	          + " ] "
//	          + "}";
}
