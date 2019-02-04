package gov.va.api.health.argonaut.api.swaggerexamples;

import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerObservation {
  static final SomeClass SWAGGER_EXAMPLE_OBSERVATION =
      XXX.builder()
          .resourceType("Observation")
          .id("7889e577-88d6-5e6f-8a4d-fb6988b7b3c1")
          .status("final")
          .category(
              XXX.builder()
                  .coding(
                      asList(
                          XXX.builder()
                              .system("http://hl7.org/fhir/observation-category")
                              .code("laboratory")
                              .display("Laboratory")
                              .build()))
                  .build())
          .code(
              XXX.builder()
                  .coding(
                      asList(
                          XXX.builder()
                              .system("http://loinc.org")
                              .code("32623-1")
                              .display("Platelet mean volume [Entitic volume] in Blood by ")
                              .build()))
                  .build())
          .subject(
              XXX.builder()
                  .reference("https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                  .display("Mr. Aurelio227 Cruickshank494")
                  .build())
          .effectiveDateTime("2017-04-24T01:15:52Z")
          .issued("2017-04-24T01:15:52Z")
          .valueQuantity(
              XXX.builder()
                  .value(10.226877417360429)
                  .unit("fL")
                  .system("http://unitsofmeasure.org")
                  .code("fL")
                  .build())
          .build();

//  static final String OBSERVATION =
//      "{ "
//          + "   \"resourceType\": \"Observation\", "
//          + "   \"id\": \"7889e577-88d6-5e6f-8a4d-fb6988b7b3c1\", "
//          + "   \"status\": \"final\", "
//          + "   \"category\": { "
//          + "      \"coding\": [ "
//          + "         { "
//          + "            \"system\": \"http://hl7.org/fhir/observation-category\", "
//          + "            \"code\": \"laboratory\", "
//          + "            \"display\": \"Laboratory\" "
//          + "         } "
//          + "      ] "
//          + "   }, "
//          + "   \"code\": { "
//          + "      \"coding\": [ "
//          + "         { "
//          + "            \"system\": \"http://loinc.org\", "
//          + "            \"code\": \"32623-1\", "
//          + "            \"display\": \"Platelet mean volume [Entitic volume] in Blood by \" "
//          + "         } "
//          + "      ] "
//          + "   }, "
//          + "  \"subject\": { "
//          + "      \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/2000163\", "
//          + "      \"display\": \"Mr. Aurelio227 Cruickshank494\" "
//          + "   }, "
//          + "   \"effectiveDateTime\": \"2017-04-24T01:15:52Z\", "
//          + "   \"issued\": \"2017-04-24T01:15:52Z\", "
//          + "   \"valueQuantity\": { "
//          + "      \"value\": 10.226877417360429, "
//          + "      \"unit\": \"fL\", "
//          + "      \"system\": \"http://unitsofmeasure.org\", "
//          + "      \"code\": \"fL\" "
//          + "   } "
//          + "} ";
  
  static final SomeClass SWAGGER_EXAMPLE_OBSERVATION_BUNDLE =
      XXX.builder()
          .resourceType("Bundle")
          .type("searchset")
          .total(1)
          .link(
              asList(
                  XXX.builder()
                      .relation("self")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Observation?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("first")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Observation?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("last")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Observation?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  XXX.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/Observation/7889e577-88d6-5e6f-8a4d-fb6988b7b3c1")
                      .resource(
                          XXX.builder()
                              .fullUrl(
                                  "https://dev-api.va.gov/services/argonaut/v0/Observation/7889e577-88d6-5e6f-8a4d-fb6988b7b3c1")
                              .resource(
                                  XXX.builder()
                                      .resourceType("Observation")
                                      .id("7889e577-88d6-5e6f-8a4d-fb6988b7b3c1")
                                      .status("final")
                                      .category(
                                          XXX.builder()
                                              .coding(
                                                  asList(
                                                      XXX.builder()
                                                          .system(
                                                              "http://hl7.org/fhir/observation-category")
                                                          .code("laboratory")
                                                          .display("Laboratory")
                                                          .build()))
                                              .build())
                                      .code(
                                          XXX.builder()
                                              .coding(
                                                  asList(
                                                      XXX.builder()
                                                          .system("http://loinc.org")
                                                          .code("32623-1")
                                                          .display(
                                                              "Platelet mean volume in Blood by ")
                                                          .build()))
                                              .build())
                                      .subject(
                                          XXX.builder()
                                              .reference(
                                                  "https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                                              .display("Mr. Aurelio227 Cruickshank494")
                                              .build())
                                      .effectiveDateTime("2017-04-24T01:15:52Z")
                                      .issued("2017-04-24T01:15:52Z")
                                      .valueQuantity(
                                          XXX.builder()
                                              .value(10.226877417360429)
                                              .unit("fL")
                                              .system("http://unitsofmeasure.org")
                                              .code("fL")
                                              .build())
                                      .build())
                              .search(XXX.builder().mode("match").build())
                              .build())
                      .build()))
          .build();
  
//  static final String OBSERVATION_BUNDLE =
//	      "{ "
//	          + "    \"resourceType\": \"Bundle\", "
//	          + "    \"type\": \"searchset\", "
//	          + "    \"total\": 1, "
//	          + "    \"link\": [ "
//	          + "        { "
//	          + "            \"relation\": \"self\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Observation?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\": \"first\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Observation?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\":\"last\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Observation?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        } "
//	          + "    ], "
//	          + "    \"entry\": [ "
//	          + "        { "
//	          + "            \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Observation/7889e577-88d6-5e6f-8a4d-fb6988b7b3c1\", "
//	          + "            \"resource\": { "
//	          + "                \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Observation/7889e577-88d6-5e6f-8a4d-fb6988b7b3c1\", "
//	          + "                \"resource\": { "
//	          + "                    \"resourceType\": \"Observation\", "
//	          + "                    \"id\": \"7889e577-88d6-5e6f-8a4d-fb6988b7b3c1\", "
//	          + "                    \"status\": \"final\", "
//	          + "                    \"category\": { "
//	          + "                       \"coding\": [ "
//	          + "                          { "
//	          + "                             \"system\": \"http://hl7.org/fhir/observation-category\", "
//	          + "                             \"code\": \"laboratory\", "
//	          + "                             \"display\": \"Laboratory\" "
//	          + "                          } "
//	          + "                       ] "
//	          + "                    }, "
//	          + "                    \"code\": { "
//	          + "                       \"coding\": [ "
//	          + "                          { "
//	          + "                             \"system\": \"http://loinc.org\", "
//	          + "                             \"code\": \"32623-1\", "
//	          + "                             \"display\": \"Platelet mean volume in Blood by \" "
//	          + "                          } "
//	          + "                       ] "
//	          + "                    }, "
//	          + "                   \"subject\": { "
//	          + "                       \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/2000163\", "
//	          + "                       \"display\": \"Mr. Aurelio227 Cruickshank494\" "
//	          + "                    }, "
//	          + "                    \"effectiveDateTime\": \"2017-04-24T01:15:52Z\", "
//	          + "                    \"issued\": \"2017-04-24T01:15:52Z\", "
//	          + "                    \"valueQuantity\": { "
//	          + "                       \"value\": 10.226877417360429, "
//	          + "                       \"unit\": \"fL\", "
//	          + "                       \"system\": \"http://unitsofmeasure.org\", "
//	          + "                       \"code\": \"fL\" "
//	          + "                    } "
//	          + "                }, "
//	          + "                \"search\": { "
//	          + "                    \"mode\": \"match\" "
//	          + "                } "
//	          + "            } "
//	          + "        } "
//	          + "    ] "
//	          + "} ";
}
