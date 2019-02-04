package gov.va.api.health.argonaut.api.swaggerexamples;

import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerMedicationDispense {
  static final SomeClass SWAGGER_EXAMPLE_MEDICATION_DISPENSE =
      XXX.builder()
          .resourceType("MedicationDispense")
          .id("3ba8c63c-bac2-5f98-b7cd-161792919216")
          .identifier(
              XXX.builder().use("usual").system("http://va.gov/cdw").value("185601V825290").build())
          .status("completed")
          .patient(
              XXX.builder()
                  .reference("https://dev-api.va.gov/services/argonaut/v0/Patient/185601V825290")
                  .display("VETERAN,JOHN Q")
                  .build())
          .dispenser(
              XXX.builder()
                  .reference(
                      "https://dev-api.va.gov/services/argonaut/v0/Practitioner/a98d6c9c-c5bd-58a5-a5d0-0da31cafd37c")
                  .display("SMITH,ATTENDING C")
                  .build())
          .authorizingPrescription(
              asList(
                  XXX.builder()
                      .reference(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationOrder/8c95153c-817b-53f3-8166-55b1e01ac2a2")
                      .display("OUTPATIENT PHARMACY")
                      .build()))
          .type(
              XXX.builder()
                  .coding(
                      asList(
                          XXX.builder()
                              .system("http://hl7.org/fhir/v3/ActCode")
                              .code("FF")
                              .display("First Fill")
                              .build()))
                  .build())
          .quantity(XXX.builder().value(240).unit("ML").build())
          .daysSupply(
              XXX.builder()
                  .value(15)
                  .unit("Day")
                  .system("http://unitsofmeasure.org")
                  .code("D")
                  .build())
          .medicationReference(
              XXX.builder()
                  .reference(
                      "https://dev-api.va.gov/services/argonaut/v0/Medication/b07873cb-24cb-5aa7-a0be-78b7ac5aee22")
                  .display("CODEINE 10/GG 100MG/5ML (ALC-F/SF) LIQ")
                  .build())
          .whenPrepared("2015-04-15T04:00:00Z")
          .dosageInstruction(
              asList(
                  XXX.builder()
                      .text(
                          "TAKE 1 TEASPOONFUL BY MOUTH EVERY 6 HOURS AS NEEDED FOR COUGH CAN TAKE  2 TEASPOONS AT BEDTIME IF NEEDED. USE SPARINGLY")
                      .additionalInstructions(
                          XXX.builder()
                              .text(
                                  "FOR COUGH can take 2 teaspoons at bedtime if needed. Use sparingly")
                              .build())
                      .timing(XXX.builder().code(XXX.builder().text("PRN").build()).build())
                      .asNeededBoolean(true)
                      .route(XXX.builder().text("ORAL").build())
                      .doseQuantity(XXX.builder().value(1).build())
                      .build()))
          .build();

//  static final String MEDICATION_DISPENSE =
//  "{\n"
//      + "    \"resourceType\": \"MedicationDispense\",\n"
//      + "    \"id\": \"3ba8c63c-bac2-5f98-b7cd-161792919216\",\n"
//      + "    \"identifier\": {\n"
//      + "        \"use\": \"usual\",\n"
//      + "        \"system\": \"http://va.gov/cdw\",\n"
//      + "        \"value\": \"185601V825290\"\n"
//      + "    },\n"
//      + "    \"status\": \"completed\",\n"
//      + "    \"patient\": {\n"
//      + "        \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/185601V825290\",\n"
//      + "        \"display\": \"VETERAN,JOHN Q\"\n"
//      + "    },\n"
//      + "    \"dispenser\": {\n"
//      + "        \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Practitioner/a98d6c9c-c5bd-58a5-a5d0-0da31cafd37c\",\n"
//      + "        \"display\": \"SMITH,ATTENDING C\"\n"
//      + "    },\n"
//      + "    \"authorizingPrescription\": [\n"
//      + "        {\n"
//      + "            \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationOrder/8c95153c-817b-53f3-8166-55b1e01ac2a2\",\n"
//      + "            \"display\": \"OUTPATIENT PHARMACY\"\n"
//      + "        }\n"
//      + "    ],\n"
//      + "    \"type\": {\n"
//      + "        \"coding\": [\n"
//      + "            {\n"
//      + "                \"system\": \"http://hl7.org/fhir/v3/ActCode\",\n"
//      + "                \"code\": \"FF\",\n"
//      + "                \"display\": \"First Fill\"\n"
//      + "            }\n"
//      + "        ]\n"
//      + "    },\n"
//      + "    \"quantity\": {\n"
//      + "        \"value\": 240,\n"
//      + "        \"unit\": \"ML\"\n"
//      + "    },\n"
//      + "    \"daysSupply\": {\n"
//      + "        \"value\": 15,\n"
//      + "        \"unit\": \"Day\",\n"
//      + "        \"system\": \"http://unitsofmeasure.org\",\n"
//      + "        \"code\": \"D\"\n"
//      + "    },\n"
//      + "    \"medicationReference\": {\n"
//      + "        \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Medication/b07873cb-24cb-5aa7-a0be-78b7ac5aee22\",\n"
//      + "        \"display\": \"CODEINE 10/GG 100MG/5ML (ALC-F/SF) LIQ\"\n"
//      + "    },\n"
//      + "    \"whenPrepared\": \"2015-04-15T04:00:00Z\",\n"
//      + "    \"dosageInstruction\": [\n"
//      + "        {\n"
//      + "            \"text\": \"TAKE 1 TEASPOONFUL BY MOUTH EVERY 6 HOURS AS NEEDED FOR COUGH CAN TAKE  2 TEASPOONS AT BEDTIME IF NEEDED. USE SPARINGLY\",\n"
//      + "            \"additionalInstructions\": {\n"
//      + "                \"text\": \"FOR COUGH can take 2 teaspoons at bedtime if needed. Use sparingly\"\n"
//      + "            },\n"
//      + "            \"timing\": {\n"
//      + "                \"code\": {\n"
//      + "                    \"text\": \"PRN\"\n"
//      + "                }\n"
//      + "            },\n"
//      + "            \"asNeededBoolean\": true,\n"
//      + "            \"route\": {\n"
//      + "                \"text\": \"ORAL\"\n"
//      + "            },\n"
//      + "            \"doseQuantity\": {\n"
//      + "                \"value\": 1\n"
//      + "            }\n"
//      + "        }\n"
//      + "    ]\n"
//      + "}";
  
  static final SomeClass SWAGGER_EXAMPLE_MEDICATION_DISPENSE_BUNDLE =
      XXX.builder()
          .resourceType("Bundle")
          .type("searchset")
          .total(1155)
          .link(
              asList(
                  XXX.builder()
                      .relation("first")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationDispense?patient=185601V825290&page=1&_count=1")
                      .build(),
                  XXX.builder()
                      .relation("self")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationDispense?patient=185601V825290&page=1&_count=1")
                      .build(),
                  XXX.builder()
                      .relation("next")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationDispense?patient=185601V825290&page=2&_count=1")
                      .build(),
                  XXX.builder()
                      .relation("last")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationDispense?patient=185601V825290&page=1155&_count=1")
                      .build()))
          .entry(
              asList(
                  XXX.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationDispense/2f587c16-5182-59a5-bdcb-518c7c501f37")
                      .resource(
                          XXX.builder()
                              .resourceType("MedicationDispense")
                              .id("2f587c16-5182-59a5-bdcb-518c7c501f37")
                              .identifier(
                                  XXX.builder()
                                      .use("usual")
                                      .system("http://va.gov/cdw")
                                      .value("185601V825290")
                                      .build())
                              .status("completed")
                              .patient(
                                  XXX.builder()
                                      .reference(
                                          "https://dev-api.va.gov/services/argonaut/v0/Patient/185601V825290")
                                      .display("VETERAN,JOHN Q")
                                      .build())
                              .dispenser(
                                  XXX.builder()
                                      .reference(
                                          "https://dev-api.va.gov/services/argonaut/v0/Practitioner/f3276418-063d-52d8-a7f9-5fcaadc2f22b")
                                      .display("BONES,ATTENDING C")
                                      .build())
                              .authorizingPrescription(
                                  asList(
                                      XXX.builder()
                                          .reference(
                                              "https://dev-api.va.gov/services/argonaut/v0/MedicationOrder/e4638ac7-7af1-587c-8f93-811abf8158c7")
                                          .display("OUTPATIENT PHARMACY")
                                          .build()))
                              .type(
                                  XXX.builder()
                                      .coding(
                                          asList(
                                              XXX.builder()
                                                  .system("http://hl7.org/fhir/v3/ActCode")
                                                  .code("FF")
                                                  .display("First Fill")
                                                  .build()))
                                      .build())
                              .quantity(XXX.builder().value(2).unit("EA").build())
                              .daysSupply(
                                  XXX.builder()
                                      .value(30)
                                      .unit("Day")
                                      .system("http://unitsofmeasure.org")
                                      .code("D")
                                      .build())
                              .medicationReference(
                                  XXX.builder()
                                      .reference(
                                          "https://dev-api.va.gov/services/argonaut/v0/Medication/8ab05080-af33-5724-a090-e9f76386ce30")
                                      .display("ALBUTEROL 90MCG (CFC-F) 200D ORAL INHL")
                                      .build())
                              .whenPrepared("2015-04-15T04:00:00Z")
                              .dosageInstruction(
                                  asList(
                                      XXX.builder()
                                          .text(
                                              "INHALE 2 PUFFS BY MOUTH EVERY 4 HOURS AS NEEDED FOR SHORTNESS OF  BREATH")
                                          .timing(
                                              XXX.builder()
                                                  .code(XXX.builder().text("Q4H PRN").build())
                                                  .build())
                                          .asNeededBoolean(true)
                                          .route(XXX.builder().text("INHALATION ORAL").build())
                                          .doseQuantity(XXX.builder().value(2).build())
                                          .build()))
                              .build())
                      .search(XXX.builder().mode("match").build())
                      .build()))
          .build();

//  static final String MEDICATION_DISPENSE_BUNDLE =
//      "{\n"
//          + "    \"resourceType\": \"Bundle\",\n"
//          + "    \"type\": \"searchset\",\n"
//          + "    \"total\": 1155,\n"
//          + "    \"link\": [\n"
//          + "        {\n"
//          + "            \"relation\": \"first\",\n"
//          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationDispense?patient=185601V825290&page=1&_count=1\"\n"
//          + "        },\n"
//          + "        {\n"
//          + "            \"relation\": \"self\",\n"
//          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationDispense?patient=185601V825290&page=1&_count=1\"\n"
//          + "        },\n"
//          + "        {\n"
//          + "            \"relation\": \"next\",\n"
//          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationDispense?patient=185601V825290&page=2&_count=1\"\n"
//          + "        },\n"
//          + "        {\n"
//          + "            \"relation\": \"last\",\n"
//          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationDispense?patient=185601V825290&page=1155&_count=1\"\n"
//          + "        }\n"
//          + "    ],\n"
//          + "    \"entry\": [\n"
//          + "        {\n"
//          + "            \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationDispense/2f587c16-5182-59a5-bdcb-518c7c501f37\",\n"
//          + "            \"resource\": {\n"
//          + "                \"resourceType\": \"MedicationDispense\",\n"
//          + "                \"id\": \"2f587c16-5182-59a5-bdcb-518c7c501f37\",\n"
//          + "                \"identifier\": {\n"
//          + "                    \"use\": \"usual\",\n"
//          + "                    \"system\": \"http://va.gov/cdw\",\n"
//          + "                    \"value\": \"185601V825290\"\n"
//          + "                },\n"
//          + "                \"status\": \"completed\",\n"
//          + "                \"patient\": {\n"
//          + "                    \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/185601V825290\",\n"
//          + "                    \"display\": \"VETERAN,JOHN Q\"\n"
//          + "                },\n"
//          + "                \"dispenser\": {\n"
//          + "                    \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Practitioner/f3276418-063d-52d8-a7f9-5fcaadc2f22b\",\n"
//          + "                    \"display\": \"BONES,ATTENDING C\"\n"
//          + "                },\n"
//          + "                \"authorizingPrescription\": [\n"
//          + "                    {\n"
//          + "                        \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationOrder/e4638ac7-7af1-587c-8f93-811abf8158c7\",\n"
//          + "                        \"display\": \"OUTPATIENT PHARMACY\"\n"
//          + "                    }\n"
//          + "                ],\n"
//          + "                \"type\": {\n"
//          + "                    \"coding\": [\n"
//          + "                        {\n"
//          + "                            \"system\": \"http://hl7.org/fhir/v3/ActCode\",\n"
//          + "                            \"code\": \"FF\",\n"
//          + "                            \"display\": \"First Fill\"\n"
//          + "                        }\n"
//          + "                    ]\n"
//          + "                },\n"
//          + "                \"quantity\": {\n"
//          + "                    \"value\": 2,\n"
//          + "                    \"unit\": \"EA\"\n"
//          + "                },\n"
//          + "                \"daysSupply\": {\n"
//          + "                    \"value\": 30,\n"
//          + "                    \"unit\": \"Day\",\n"
//          + "                    \"system\": \"http://unitsofmeasure.org\",\n"
//          + "                    \"code\": \"D\"\n"
//          + "                },\n"
//          + "                \"medicationReference\": {\n"
//          + "                    \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Medication/8ab05080-af33-5724-a090-e9f76386ce30\",\n"
//          + "                    \"display\": \"ALBUTEROL 90MCG (CFC-F) 200D ORAL INHL\"\n"
//          + "                },\n"
//          + "                \"whenPrepared\": \"2015-04-15T04:00:00Z\",\n"
//          + "                \"dosageInstruction\": [\n"
//          + "                    {\n"
//          + "                        \"text\": \"INHALE 2 PUFFS BY MOUTH EVERY 4 HOURS AS NEEDED FOR SHORTNESS OF  BREATH\",\n"
//          + "                        \"timing\": {\n"
//          + "                            \"code\": {\n"
//          + "                                \"text\": \"Q4H PRN\"\n"
//          + "                            }\n"
//          + "                        },\n"
//          + "                        \"asNeededBoolean\": true,\n"
//          + "                        \"route\": {\n"
//          + "                            \"text\": \"INHALATION ORAL\"\n"
//          + "                        },\n"
//          + "                        \"doseQuantity\": {\n"
//          + "                            \"value\": 2\n"
//          + "                        }\n"
//          + "                    }\n"
//          + "                ]\n"
//          + "            },\n"
//          + "            \"search\": {\n"
//          + "                \"mode\": \"match\"\n"
//          + "            }\n"
//          + "        }\n"
//          + "    ]\n"
//          + "}";
}
