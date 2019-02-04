package gov.va.api.health.argonaut.api.swaggerexamples;

import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerImmunization {
  static final SomeClass SWAGGER_EXAMPLE_IMMUNIZATION =
      XXX.builder()
          .resourceType("Immunization")
          .id("1fd82e3a-a95b-5c04-9a68-c8ddf740ea0c")
          .status("completed")
          .date("2017-04-24T01:15:52Z")
          .vaccineCode(
              XXX.builder()
                  .text("meningococcal MCV4P")
                  .coding(
                      asList(
                          XXX.builder().system("http://hl7.org/fhir/sid/cvx").code("114").build()))
                  .build())
          .patient(
              XXX.builder()
                  .reference("https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                  .display("Mr. Aurelio227 Cruickshank494")
                  .build())
          .wasNotGiven("false")
          ._reported(
              XXX.builder()
                  .extension(
                      asList(
                          XXX.builder()
                              .url("http://hl7.org/fhir/StructureDefinition/data-absent-reason")
                              .valueCode("unsupported")
                              .build()))
                  .build())
          .reaction(asList(XXX.builder().detail(XXX.builder().display("Lethargy").build()).build()))
          .build();


//  static final String IMMUNIZATION =
//      "{ "
//          + "   \"resourceType\": \"Immunization\", "
//          + "   \"id\": \"1fd82e3a-a95b-5c04-9a68-c8ddf740ea0c\", "
//          + "   \"status\": \"completed\", "
//          + "   \"date\": \"2017-04-24T01:15:52Z\", "
//          + "   \"vaccineCode\": { "
//          + "      \"text\": \"meningococcal MCV4P\", "
//          + "      \"coding\": [ "
//          + "         { "
//          + "            \"system\": \"http://hl7.org/fhir/sid/cvx\", "
//          + "            \"code\": \"114\" "
//          + "         } "
//          + "      ] "
//          + "   }, "
//          + "   \"patient\": { "
//          + "      \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/2000163\", "
//          + "      \"display\": \"Mr. Aurelio227 Cruickshank494\" "
//          + "   }, "
//          + "   \"wasNotGiven\": \"false\", "
//          + "   \"_reported\": { "
//          + "      \"extension\": [ "
//          + "         { "
//          + "            \"url\": \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\", "
//          + "            \"valueCode\": \"unsupported\" "
//          + "         } "
//          + "      ] "
//          + "   }, "
//          + "   \"reaction\": [ "
//          + "      { "
//          + "            \"detail\": { "
//          + "            \"display\": \"Lethargy\" "
//          + "         } "
//          + "      } "
//          + "   ] "
//          + "} ";
  
  static final SomeClass SWAGGER_EXAMPLE_IMMUNIZATION_BUNDLE =
      XXX.builder()
          .resourceType("Bundle")
          .type("searchset")
          .total(1)
          .link(
              asList(
                  XXX.builder()
                      .relation("self")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Immunization?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("first")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Immunization?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("last")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Immunization?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  XXX.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/Immunization/1fd82e3a-a95b-5c04-9a68-c8ddf740ea0c")
                      .resource(
                          XXX.builder()
                              .fullUrl(
                                  "https://dev-api.va.gov/services/argonaut/v0/Immunization/1fd82e3a-a95b-5c04-9a68-c8ddf740ea0c")
                              .resource(
                                  XXX.builder()
                                      .resourceType("Immunization")
                                      .id("1fd82e3a-a95b-5c04-9a68-c8ddf740ea0c")
                                      .status("completed")
                                      .date("2017-04-24T01:15:52Z")
                                      .vaccineCode(
                                          XXX.builder()
                                              .text("meningococcal MCV4P")
                                              .coding(
                                                  asList(
                                                      XXX.builder()
                                                          .system("http://hl7.org/fhir/sid/cvx")
                                                          .code("114")
                                                          .build()))
                                              .build())
                                      .patient(
                                          XXX.builder()
                                              .reference(
                                                  "https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                                              .display("Mr. Aurelio227 Cruickshank494")
                                              .build())
                                      .wasNotGiven("false")
                                      ._reported(
                                          XXX.builder()
                                              .extension(
                                                  asList(
                                                      XXX.builder()
                                                          .url(
                                                              "http://hl7.org/fhir/StructureDefinition/data-absent-reason")
                                                          .valueCode("unsupported")
                                                          .build()))
                                              .build())
                                      .reaction(
                                          asList(
                                              XXX.builder()
                                                  .detail(XXX.builder().display("Lethargy").build())
                                                  .build()))
                                      .build())
                              .search(XXX.builder().mode("match").build())
                              .build())
                      .build()))
          .build();

//  static final String IMMUNIZATION_BUNDLE =
//      "{ "
//          + "    \"resourceType\": \"Bundle\", "
//          + "    \"type\": \"searchset\", "
//          + "    \"total\": 1, "
//          + "    \"link\": [ "
//          + "        { "
//          + "            \"relation\": \"self\", "
//          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Immunization?patient=1017283148V813263&page=1&_count=15\" "
//          + "        }, "
//          + "        { "
//          + "            \"relation\": \"first\", "
//          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Immunization?patient=1017283148V813263&page=1&_count=15\" "
//          + "        }, "
//          + "        { "
//          + "            \"relation\":\"last\", "
//          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Immunization?patient=1017283148V813263&page=1&_count=15\" "
//          + "        } "
//          + "    ], "
//          + "    \"entry\": [ "
//          + "        { "
//          + "            \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Immunization/1fd82e3a-a95b-5c04-9a68-c8ddf740ea0c\", "
//          + "            \"resource\": { "
//          + "                \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Immunization/1fd82e3a-a95b-5c04-9a68-c8ddf740ea0c\", "
//          + "                \"resource\": { "
//          + "                    \"resourceType\": \"Immunization\", "
//          + "                    \"id\": \"1fd82e3a-a95b-5c04-9a68-c8ddf740ea0c\", "
//          + "                    \"status\": \"completed\", "
//          + "                    \"date\": \"2017-04-24T01:15:52Z\", "
//          + "                    \"vaccineCode\": { "
//          + "                       \"text\": \"meningococcal MCV4P\", "
//          + "                       \"coding\": [ "
//          + "                          { "
//          + "                             \"system\": \"http://hl7.org/fhir/sid/cvx\", "
//          + "                             \"code\": \"114\" "
//          + "                          } "
//          + "                       ] "
//          + "                    }, "
//          + "                    \"patient\": { "
//          + "                       \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/2000163\", "
//          + "                       \"display\": \"Mr. Aurelio227 Cruickshank494\" "
//          + "                    }, "
//          + "                    \"wasNotGiven\": \"false\", "
//          + "                    \"_reported\": { "
//          + "                       \"extension\": [ "
//          + "                          { "
//          + "                             \"url\": \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\", "
//          + "                             \"valueCode\": \"unsupported\" "
//          + "                          } "
//          + "                       ] "
//          + "                    }, "
//          + "                    \"reaction\": [ "
//          + "                       { "
//          + "                           \"detail\": { "
//          + "                               \"display\": \"Lethargy\" "
//          + "                           } "
//          + "                       } "
//          + "                    ] "
//          + "                }, "
//          + "                \"search\": { "
//          + "                    \"mode\": \"match\" "
//          + "                } "
//          + "            } "
//          + "        } "
//          + "    ] "
//          + "} ";
}
