package gov.va.api.health.argonaut.api.swaggerexamples;

import gov.va.api.health.argonaut.api.resources.Condition;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerCondition {
  static final Condition SWAGGER_EXAMPLE_CONDITION =
      XXX.builder()
          .resourceType("Condition")
          .id("b34bacd3-42b6-5613-b1c2-1abafe1248ba")
          .patient(
              XXX.builder()
                  .reference("https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                  .display("Mr. Aurelio227 Cruickshank494")
                  .build())
          .code(
              XXX.builder()
                  .coding(
                      asList(
                          XXX.builder()
                              .code("38341003")
                              .system("https://www.snomed.org/snomed-ct")
                              .display("Hypertension")
                              .build()))
                  .text("Hypertension")
                  .build())
          .category(
              XXX.builder()
                  .coding(
                      asList(
                          XXX.builder().system("http://argonaut.hl7.org").code("problem").build()))
                  .build())
          .clinicalStatus("active")
          .verificationStatus("unknown")
          .dateRecorded("2013-04-14")
          .onsetDateTime("2013-04-15T01:15:52Z")
          .build();

  //	 static final String CONDITION =
  //		      "{ "
  //		          + "    \"resourceType\": \"Condition\", "
  //		          + "    \"id\": \"b34bacd3-42b6-5613-b1c2-1abafe1248ba\", "
  //		          + "    \"patient\": { "
  //		          + "        \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/2000163\", "
  //		          + "        \"display\": \"Mr. Aurelio227 Cruickshank494\" "
  //		          + "    }, "
  //		          + "    \"code\": { "
  //		          + "        \"coding\": [ "
  //		          + "            { "
  //		          + "                \"code\": \"38341003\", "
  //		          + "                \"system\": \"https://www.snomed.org/snomed-ct\", "
  //		          + "                \"display\": \"Hypertension\" "
  //		          + "            } "
  //		          + "        ], "
  //		          + "        \"text\": \"Hypertension\" "
  //		          + "    }, "
  //		          + "    \"category\": { "
  //		          + "        \"coding\": [ "
  //		          + "            { "
  //		          + "                \"system\": \"http://argonaut.hl7.org\", "
  //		          + "                \"code\": \"problem\" "
  //		          + "            } "
  //		          + "        ] "
  //		          + "    }, "
  //		          + "    \"clinicalStatus\": \"active\", "
  //		          + "    \"verificationStatus\": \"unknown\", "
  //		          + "    \"dateRecorded\": \"2013-04-14\", "
  //		          + "    \"onsetDateTime\": \"2013-04-15T01:15:52Z\" "
  //		          + "} ";

  static final Condition.Bundle SWAGGER_EXAMPLE_CONDITION_BUNDLE =
      XXX.builder()
          .resourceType("Bundle")
          .type("searchset")
          .total(1)
          .link(
              asList(
                  XXX.builder()
                      .relation("self")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Condition?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("first")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Condition?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("last")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Condition?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  XXX.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/Condition/b34bacd3-42b6-5613-b1c2-1abafe1248ba")
                      .resource(
                          XXX.builder()
                              .fullUrl(
                                  "https://dev-api.va.gov/services/argonaut/v0/Condition/b34bacd3-42b6-5613-b1c2-1abafe1248ba")
                              .resource(
                                  XXX.builder()
                                      .resourceType("Condition")
                                      .id("b34bacd3-42b6-5613-b1c2-1abafe1248ba")
                                      .patient(
                                          XXX.builder()
                                              .reference(
                                                  "https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                                              .display("Mr. Aurelio227 Cruickshank494")
                                              .build())
                                      .code(
                                          XXX.builder()
                                              .coding(
                                                  asList(
                                                      XXX.builder()
                                                          .code("38341003")
                                                          .system(
                                                              "https://www.snomed.org/snomed-ct")
                                                          .display("Hypertension")
                                                          .build()))
                                              .text("Hypertension")
                                              .build())
                                      .category(
                                          XXX.builder()
                                              .coding(
                                                  asList(
                                                      XXX.builder()
                                                          .system("http://argonaut.hl7.org")
                                                          .code("problem")
                                                          .build()))
                                              .build())
                                      .clinicalStatus("active")
                                      .verificationStatus("unknown")
                                      .dateRecorded("2013-04-14")
                                      .onsetDateTime("2013-04-15T01:15:52Z")
                                      .build())
                              .search(XXX.builder().mode("match").build())
                              .build())
                      .build()))
          .build();
  
//  static final String CONDITION_BUNDLE =
//	      "{ "
//	          + "    \"resourceType\": \"Bundle\", "
//	          + "    \"type\": \"searchset\", "
//	          + "    \"total\": 1, "
//	          + "    \"link\": [ "
//	          + "        { "
//	          + "            \"relation\": \"self\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Condition?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\": \"first\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Condition?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\":\"last\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Condition?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        } "
//	          + "    ], "
//	          + "    \"entry\": [ "
//	          + "        { "
//	          + "            \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Condition/b34bacd3-42b6-5613-b1c2-1abafe1248ba\", "
//	          + "            \"resource\": { "
//	          + "                \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Condition/b34bacd3-42b6-5613-b1c2-1abafe1248ba\", "
//	          + "                \"resource\": { "
//	          + "                    \"resourceType\": \"Condition\", "
//	          + "                    \"id\": \"b34bacd3-42b6-5613-b1c2-1abafe1248ba\", "
//	          + "                    \"patient\": { "
//	          + "                        \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/2000163\", "
//	          + "                        \"display\": \"Mr. Aurelio227 Cruickshank494\" "
//	          + "                    }, "
//	          + "                    \"code\": { "
//	          + "                        \"coding\": [ "
//	          + "                           { "
//	          + "                               \"code\": \"38341003\", "
//	          + "                               \"system\": \"https://www.snomed.org/snomed-ct\", "
//	          + "                               \"display\": \"Hypertension\" "
//	          + "                           } "
//	          + "                        ], "
//	          + "                        \"text\": \"Hypertension\" "
//	          + "                    }, "
//	          + "                    \"category\": { "
//	          + "                        \"coding\": [ "
//	          + "                            { "
//	          + "                                \"system\": \"http://argonaut.hl7.org\", "
//	          + "                                \"code\": \"problem\" "
//	          + "                            } "
//	          + "                        ] "
//	          + "                    }, "
//	          + "                    \"clinicalStatus\": \"active\", "
//	          + "                    \"verificationStatus\": \"unknown\", "
//	          + "                    \"dateRecorded\": \"2013-04-14\", "
//	          + "                    \"onsetDateTime\": \"2013-04-15T01:15:52Z\" "
//	          + "                }, "
//	          + "                \"search\": { "
//	          + "                    \"mode\": \"match\" "
//	          + "                } "
//	          + "            } "
//	          + "        } "
//	          + "    ] "
//	          + "} ";
}
