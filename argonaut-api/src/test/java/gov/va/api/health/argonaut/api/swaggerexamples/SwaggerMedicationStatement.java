package gov.va.api.health.argonaut.api.swaggerexamples;

import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerMedicationStatement {
  static final SomeClass SWAGGER_EXAMPLE_MEDICATION_STATEMENT =
      XXX.builder()
          .resourceType("MedicationStatement")
          .id("1f46363d-af9b-5ba5-acda-b384373a9af2")
          .patient(
              XXX.builder()
                  .reference("https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                  .display("Mr. Aurelio227 Cruickshank494")
                  .build())
          .dateAsserted("2013-04-15T01:15:52Z")
          .status("active")
          .medicationReference(
              XXX.builder()
                  .reference(
                      "https://dev-api.va.gov/services/argonaut/v0/Medication/7b550d7f-2db8-5002-bc0c-150a70d02944")
                  .display("Hydrochlorothiazide 25 MG")
                  .build())
          .dosage(
              asList(
                  XXX.builder()
                      .text("Once per day.")
                      .timing(
                          XXX.builder()
                              .code(XXX.builder().text("As directed by physician.").build())
                              .build())
                      .route(XXX.builder().text("As directed by physician.").build())
                      .build()))
          .build();

//  static final String MEDICATION_STATEMENT =
//	      "{ "
//	          + "   \"resourceType\": \"MedicationStatement\", "
//	          + "   \"id\": \"1f46363d-af9b-5ba5-acda-b384373a9af2\", "
//	          + "   \"patient\": { "
//	          + "      \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/2000163\", "
//	          + "      \"display\": \"Mr. Aurelio227 Cruickshank494\" "
//	          + "   }, "
//	          + "   \"dateAsserted\": \"2013-04-15T01:15:52Z\", "
//	          + "   \"status\": \"active\", "
//	          + "   \"medicationReference\": { "
//	          + "      \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Medication/7b550d7f-2db8-5002-bc0c-150a70d02944\", "
//	          + "      \"display\": \"Hydrochlorothiazide 25 MG\" "
//	          + "   }, "
//	          + "   \"dosage\": [ "
//	          + "      { "
//	          + "         \"text\": \"Once per day.\", "
//	          + "         \"timing\": { "
//	          + "            \"code\": { "
//	          + "               \"text\": \"As directed by physician.\" "
//	          + "            } "
//	          + "         }, "
//	          + "         \"route\": { "
//	          + "            \"text\": \"As directed by physician.\" "
//	          + "         } "
//	          + "      } "
//	          + "   ] "
//	          + "} ";
  
  static final SomeClass SWAGGER_EXAMPLE_MEDICATION_STATEMENT_BUNDLE =
      XXX.builder()
          .resourceType("Bundle")
          .type("searchset")
          .total(1)
          .link(
              asList(
                  XXX.builder()
                      .relation("self")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationStatement?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("first")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationStatement?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("last")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationStatement?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  XXX.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationStatement/1f46363d-af9b-5ba5-acda-b384373a9af2")
                      .resource(
                          XXX.builder()
                              .fullUrl(
                                  "https://dev-api.va.gov/services/argonaut/v0/MedicationStatement/1f46363d-af9b-5ba5-acda-b384373a9af2")
                              .resource(
                                  XXX.builder()
                                      .resourceType("MedicationStatement")
                                      .id("1f46363d-af9b-5ba5-acda-b384373a9af2")
                                      .patient(
                                          XXX.builder()
                                              .reference(
                                                  "https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                                              .display("Mr. Aurelio227 Cruickshank494")
                                              .build())
                                      .dateAsserted("2013-04-15T01:15:52Z")
                                      .status("active")
                                      .medicationReference(
                                          XXX.builder()
                                              .reference(
                                                  "https://dev-api.va.gov/services/argonaut/v0/Medication/7b550d7f-2db8-5002-bc0c-150a70d02944")
                                              .display("Hydrochlorothiazide 25 MG")
                                              .build())
                                      .dosage(
                                          asList(
                                              XXX.builder()
                                                  .text("Once per day.")
                                                  .timing(
                                                      XXX.builder()
                                                          .code(
                                                              XXX.builder()
                                                                  .text("As directed by physician.")
                                                                  .build())
                                                          .build())
                                                  .route(
                                                      XXX.builder()
                                                          .text("As directed by physician.")
                                                          .build())
                                                  .build()))
                                      .build())
                              .search(XXX.builder().mode("match").build())
                              .build())
                      .build()))
          .build();
  
//  static final String MEDICATION_STATEMENT_BUNDLE =
//	      "{ "
//	          + "    \"resourceType\": \"Bundle\", "
//	          + "    \"type\": \"searchset\", "
//	          + "    \"total\": 1, "
//	          + "    \"link\": [ "
//	          + "        { "
//	          + "            \"relation\": \"self\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationStatement?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\": \"first\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationStatement?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\":\"last\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationStatement?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        } "
//	          + "    ], "
//	          + "    \"entry\": [ "
//	          + "        { "
//	          + "            \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationStatement/1f46363d-af9b-5ba5-acda-b384373a9af2\", "
//	          + "            \"resource\": { "
//	          + "                \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationStatement/1f46363d-af9b-5ba5-acda-b384373a9af2\", "
//	          + "                \"resource\": { "
//	          + "                    \"resourceType\": \"MedicationStatement\", "
//	          + "                    \"id\": \"1f46363d-af9b-5ba5-acda-b384373a9af2\", "
//	          + "                    \"patient\": { "
//	          + "                       \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/2000163\", "
//	          + "                       \"display\": \"Mr. Aurelio227 Cruickshank494\" "
//	          + "                    }, "
//	          + "                    \"dateAsserted\": \"2013-04-15T01:15:52Z\", "
//	          + "                    \"status\": \"active\", "
//	          + "                    \"medicationReference\": { "
//	          + "                       \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Medication/7b550d7f-2db8-5002-bc0c-150a70d02944\", "
//	          + "                       \"display\": \"Hydrochlorothiazide 25 MG\" "
//	          + "                    }, "
//	          + "                    \"dosage\": [ "
//	          + "                       { "
//	          + "                          \"text\": \"Once per day.\", "
//	          + "                          \"timing\": { "
//	          + "                             \"code\": { "
//	          + "                                \"text\": \"As directed by physician.\" "
//	          + "                             } "
//	          + "                          }, "
//	          + "                          \"route\": { "
//	          + "                             \"text\": \"As directed by physician.\" "
//	          + "                          } "
//	          + "                       } "
//	          + "                    ] "
//	          + "                }, "
//	          + "                \"search\": { "
//	          + "                    \"mode\": \"match\" "
//	          + "                } "
//	          + "            } "
//	          + "        } "
//	          + "    ] "
//	          + "} ";
}
