package gov.va.api.health.argonaut.api.swaggerexamples;

import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerPatient {
  static final SomeClass SWAGGER_EXAMPLE_PATIENT =
      XXX.builder()
          .id("2000163")
          .resourceType("Patient")
          .extension(
              asList(
                  XXX.builder()
                      .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
                      .extension(
                          asList(
                              XXX.builder()
                                  .url("ombCategory")
                                  .valueCoding(
                                      XXX.builder()
                                          .system("http://hl7.org/fhir/v3/Race")
                                          .code("2016-3")
                                          .display("White")
                                          .build())
                                  .build(),
                              XXX.builder().url("text").valueString("White").build()))
                      .build(),
                  XXX.builder()
                      .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
                      .extension(
                          asList(
                              XXX.builder()
                                  .url("ombCategory")
                                  .valueCoding(
                                      XXX.builder()
                                          .system("http://hl7.org/fhir/ValueSet/v3-Ethnicity")
                                          .code("2186-5")
                                          .display("Not Hispanic or Latino")
                                          .build())
                                  .build(),
                              XXX.builder()
                                  .url("text")
                                  .valueString("Not Hispanic or Latino")
                                  .build()))
                      .build(),
                  XXX.builder()
                      .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-birthsex")
                      .valueCode("M")
                      .build()))
          .identifier(
              asList(
                  XXX.builder()
                      .use("usual")
                      .type(
                          XXX.builder()
                              .coding(
                                  asList(
                                      XXX.builder()
                                          .system("http://hl7.org/fhir/v2/0203")
                                          .code("MR")
                                          .build()))
                              .build())
                      .system("http://va.gov/mvi")
                      .value("2000163")
                      .build(),
                  XXX.builder()
                      .use("official")
                      .type(
                          XXX.builder()
                              .coding(
                                  asList(
                                      XXX.builder()
                                          .system("http://hl7.org/fhir/v2/0203")
                                          .code("SB")
                                          .build()))
                              .build())
                      .system("http://hl7.org/fhir/sid/us-ssn")
                      .value("999-61-4803")
                      .build()))
          .name(
              asList(
                  XXX.builder()
                      .use("usual")
                      .text("Mr. Aurelio227 Cruickshank494")
                      .family(asList("Cruickshank494"))
                      .given(asList("Aurelio227"))
                      .build()))
          .telecom(
              asList(
                  XXX.builder().system("phone").value("5555191065").use("mobile").build(),
                  XXX.builder()
                      .system("email")
                      .value("Aurelio227.Cruickshank494@email.example")
                      .build()))
          .gender("male")
          .birthDate("1995-02-06")
          .deceasedBoolean("false")
          .address(
              asList(
                  XXX.builder()
                      .line(asList("909 Rohan Highlands"))
                      .city("Mesa")
                      .state("Arizona")
                      .postalCode("85120")
                      .build()))
          .maritalStatus(
              XXX.builder()
                  .coding(
                      asList(
                          XXX.builder()
                              .system("http://hl7.org/fhir/v3/NullFlavor")
                              .code("UNK")
                              .display("unknown")
                              .build()))
                  .build())
          .build();
//
//  static final String PATIENT =
//	      "{ "
//	          + "    \"id\": \"2000163\", "
//	          + "    \"resourceType\": \"Patient\", "
//	          + "    \"extension\": [ "
//	          + "        { "
//	          + "            \"url\": \"http://fhir.org/guides/argonaut/StructureDefinition/argo-race\", "
//	          + "            \"extension\": [ "
//	          + "                { "
//	          + "                    \"url\": \"ombCategory\", "
//	          + "                    \"valueCoding\": { "
//	          + "                        \"system\": \"http://hl7.org/fhir/v3/Race\", "
//	          + "                        \"code\": \"2016-3\", "
//	          + "                        \"display\": \"White\" "
//	          + "                    } "
//	          + "                }, "
//	          + "                { "
//	          + "                    \"url\": \"text\", "
//	          + "                    \"valueString\": \"White\" "
//	          + "                } "
//	          + "            ] "
//	          + "        }, "
//	          + "        { "
//	          + "            \"url\": \"http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity\", "
//	          + "            \"extension\": [ "
//	          + "                { "
//	          + "                    \"url\": \"ombCategory\", "
//	          + "                    \"valueCoding\": { "
//	          + "                        \"system\": \"http://hl7.org/fhir/ValueSet/v3-Ethnicity\", "
//	          + "                        \"code\": \"2186-5\", "
//	          + "                        \"display\": \"Not Hispanic or Latino\" "
//	          + "                    } "
//	          + "                }, "
//	          + "                { "
//	          + "                    \"url\": \"text\", "
//	          + "                    \"valueString\": \"Not Hispanic or Latino\" "
//	          + "                } "
//	          + "            ] "
//	          + "        }, "
//	          + "        { "
//	          + "            \"url\": \"http://fhir.org/guides/argonaut/StructureDefinition/argo-birthsex\", "
//	          + "            \"valueCode\": \"M\" "
//	          + "        } "
//	          + "    ], "
//	          + "    \"identifier\": [ "
//	          + "        { "
//	          + "            \"use\": \"usual\", "
//	          + "            \"type\": { "
//	          + "                \"coding\": [ "
//	          + "                    { "
//	          + "                        \"system\": \"http://hl7.org/fhir/v2/0203\", "
//	          + "                        \"code\": \"MR\" "
//	          + "                    } "
//	          + "                ] "
//	          + "            }, "
//	          + "            \"system\": \"http://va.gov/mvi\", "
//	          + "            \"value\": \"2000163\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"use\": \"official\", "
//	          + "            \"type\": { "
//	          + "                \"coding\": [ "
//	          + "                    { "
//	          + "                        \"system\": \"http://hl7.org/fhir/v2/0203\", "
//	          + "                        \"code\": \"SB\" "
//	          + "                    } "
//	          + "                ] "
//	          + "            }, "
//	          + "            \"system\": \"http://hl7.org/fhir/sid/us-ssn\", "
//	          + "            \"value\": \"999-61-4803\" "
//	          + "        } "
//	          + "    ], "
//	          + "    \"name\": [ "
//	          + "        { "
//	          + "            \"use\": \"usual\", "
//	          + "            \"text\": \"Mr. Aurelio227 Cruickshank494\", "
//	          + "            \"family\": [ "
//	          + "                \"Cruickshank494\" "
//	          + "            ], "
//	          + "            \"given\": [ "
//	          + "                \"Aurelio227\" "
//	          + "            ] "
//	          + "        } "
//	          + "    ], "
//	          + "    \"telecom\": [ "
//	          + "        { "
//	          + "            \"system\": \"phone\", "
//	          + "            \"value\": \"5555191065\", "
//	          + "            \"use\": \"mobile\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"system\": \"email\", "
//	          + "            \"value\": \"Aurelio227.Cruickshank494@email.example\" "
//	          + "        } "
//	          + "    ], "
//	          + "    \"gender\": \"male\", "
//	          + "    \"birthDate\": \"1995-02-06\", "
//	          + "    \"deceasedBoolean\": \"false\", "
//	          + "    \"address\": [ "
//	          + "        { "
//	          + "            \"line\": [ "
//	          + "                \"909 Rohan Highlands\" "
//	          + "            ], "
//	          + "            \"city\": \"Mesa\", "
//	          + "            \"state\": \"Arizona\", "
//	          + "            \"postalCode\": \"85120\" "
//	          + "        } "
//	          + "    ], "
//	          + "    \"maritalStatus\": { "
//	          + "        \"coding\": [ "
//	          + "            { "
//	          + "                \"system\": \"http://hl7.org/fhir/v3/NullFlavor\", "
//	          + "                \"code\": \"UNK\", "
//	          + "                \"display\": \"unknown\" "
//	          + "            } "
//	          + "        ] "
//	          + "    } "
//	          + "} ";
  
  static final SomeClass SWAGGER_EXAMPLE_PATIENT_BUNDLE =
      XXX.builder()
          .resourceType("Bundle")
          .type("searchset")
          .total(1)
          .link(
              asList(
                  XXX.builder()
                      .relation("self")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Patient?_id=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("first")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Patient?_id=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("last")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Patient?_id=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  XXX.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/Patient/1017283148V813263")
                      .resource(
                          XXX.builder()
                              .fullUrl(
                                  "https://dev-api.va.gov/services/argonaut/v0/Patient/1017283148V813263")
                              .resource(
                                  XXX.builder()
                                      .id("2000163")
                                      .resourceType("Patient")
                                      .extension(
                                          asList(
                                              XXX.builder()
                                                  .url(
                                                      "http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
                                                  .extension(
                                                      asList(
                                                          XXX.builder()
                                                              .url("ombCategory")
                                                              .valueCoding(
                                                                  XXX.builder()
                                                                      .system(
                                                                          "http://hl7.org/fhir/v3/Race")
                                                                      .code("2016-3")
                                                                      .display("White")
                                                                      .build())
                                                              .build(),
                                                          XXX.builder()
                                                              .url("text")
                                                              .valueString("White")
                                                              .build()))
                                                  .build(),
                                              XXX.builder()
                                                  .url(
                                                      "http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
                                                  .extension(
                                                      asList(
                                                          XXX.builder()
                                                              .url("ombCategory")
                                                              .valueCoding(
                                                                  XXX.builder()
                                                                      .system(
                                                                          "http://hl7.org/fhir/ValueSet/v3-Ethnicity")
                                                                      .code("2186-5")
                                                                      .display(
                                                                          "Not Hispanic or Latino")
                                                                      .build())
                                                              .build(),
                                                          XXX.builder()
                                                              .url("text")
                                                              .valueString("Not Hispanic or Latino")
                                                              .build()))
                                                  .build(),
                                              XXX.builder()
                                                  .url(
                                                      "http://fhir.org/guides/argonaut/StructureDefinition/argo-birthsex")
                                                  .valueCode("M")
                                                  .build()))
                                      .identifier(
                                          asList(
                                              XXX.builder()
                                                  .use("usual")
                                                  .type(
                                                      XXX.builder()
                                                          .coding(
                                                              asList(
                                                                  XXX.builder()
                                                                      .system(
                                                                          "http://hl7.org/fhir/v2/0203")
                                                                      .code("MR")
                                                                      .build()))
                                                          .build())
                                                  .system("http://va.gov/mvi")
                                                  .value("2000163")
                                                  .build(),
                                              XXX.builder()
                                                  .use("official")
                                                  .type(
                                                      XXX.builder()
                                                          .coding(
                                                              asList(
                                                                  XXX.builder()
                                                                      .system(
                                                                          "http://hl7.org/fhir/v2/0203")
                                                                      .code("SB")
                                                                      .build()))
                                                          .build())
                                                  .system("http://hl7.org/fhir/sid/us-ssn")
                                                  .value("999-61-4803")
                                                  .build()))
                                      .name(
                                          asList(
                                              XXX.builder()
                                                  .use("usual")
                                                  .text("Mr. Aurelio227 Cruickshank494")
                                                  .family(asList("Cruickshank494"))
                                                  .given(asList("Aurelio227"))
                                                  .build()))
                                      .telecom(
                                          asList(
                                              XXX.builder()
                                                  .system("phone")
                                                  .value("5555191065")
                                                  .use("mobile")
                                                  .build(),
                                              XXX.builder()
                                                  .system("email")
                                                  .value("Aurelio227.Cruickshank494@email.example")
                                                  .build()))
                                      .gender("male")
                                      .birthDate("1995-02-06")
                                      .deceasedBoolean("false")
                                      .address(
                                          asList(
                                              XXX.builder()
                                                  .line(asList("909 Rohan Highlands"))
                                                  .city("Mesa")
                                                  .state("Arizona")
                                                  .postalCode("85120")
                                                  .build()))
                                      .maritalStatus(
                                          XXX.builder()
                                              .coding(
                                                  asList(
                                                      XXX.builder()
                                                          .system(
                                                              "http://hl7.org/fhir/v3/NullFlavor")
                                                          .code("UNK")
                                                          .display("unknown")
                                                          .build()))
                                              .build())
                                      .search(XXX.builder().mode("match").build())
                                      .build())
                              .build())
                      .build()))
          .build();
//  
//  static final String PATIENT_BUNDLE =
//	      "{ "
//	          + "    \"resourceType\": \"Bundle\", "
//	          + "    \"type\": \"searchset\", "
//	          + "    \"total\": 1, "
//	          + "    \"link\": [ "
//	          + "        { "
//	          + "            \"relation\": \"self\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Patient?_id=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\": \"first\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Patient?_id=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\":\"last\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Patient?_id=1017283148V813263&page=1&_count=15\" "
//	          + "        } "
//	          + "    ], "
//	          + "    \"entry\": [ "
//	          + "        { "
//	          + "            \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/1017283148V813263\", "
//	          + "            \"resource\": { "
//	          + "                \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/1017283148V813263\", "
//	          + "                \"resource\": { "
//	          + "                    \"id\": \"2000163\", "
//	          + "                    \"resourceType\": \"Patient\", "
//	          + "                    \"extension\": [ "
//	          + "                        { "
//	          + "                            \"url\": \"http://fhir.org/guides/argonaut/StructureDefinition/argo-race\", "
//	          + "                            \"extension\": [ "
//	          + "                                { "
//	          + "                                    \"url\": \"ombCategory\", "
//	          + "                                    \"valueCoding\": { "
//	          + "                                        \"system\": \"http://hl7.org/fhir/v3/Race\", "
//	          + "                                        \"code\": \"2016-3\", "
//	          + "                                        \"display\": \"White\" "
//	          + "                                    } "
//	          + "                                }, "
//	          + "                                { "
//	          + "                                    \"url\": \"text\", "
//	          + "                                    \"valueString\": \"White\" "
//	          + "                                } "
//	          + "                            ] "
//	          + "                        }, "
//	          + "                        { "
//	          + "                            \"url\": \"http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity\", "
//	          + "                            \"extension\": [ "
//	          + "                                { "
//	          + "                                    \"url\": \"ombCategory\", "
//	          + "                                    \"valueCoding\": { "
//	          + "                                        \"system\": \"http://hl7.org/fhir/ValueSet/v3-Ethnicity\", "
//	          + "                                        \"code\": \"2186-5\", "
//	          + "                                        \"display\": \"Not Hispanic or Latino\" "
//	          + "                                    } "
//	          + "                                }, "
//	          + "                                { "
//	          + "                                    \"url\": \"text\", "
//	          + "                                    \"valueString\": \"Not Hispanic or Latino\" "
//	          + "                                } "
//	          + "                            ] "
//	          + "                        }, "
//	          + "                        { "
//	          + "                            \"url\": \"http://fhir.org/guides/argonaut/StructureDefinition/argo-birthsex\", "
//	          + "                            \"valueCode\": \"M\" "
//	          + "                        } "
//	          + "                    ], "
//	          + "                    \"identifier\": [ "
//	          + "                        { "
//	          + "                            \"use\": \"usual\", "
//	          + "                            \"type\": { "
//	          + "                                \"coding\": [ "
//	          + "                                    { "
//	          + "                                        \"system\": \"http://hl7.org/fhir/v2/0203\", "
//	          + "                                        \"code\": \"MR\" "
//	          + "                                    } "
//	          + "                                ] "
//	          + "                            }, "
//	          + "                            \"system\": \"http://va.gov/mvi\", "
//	          + "                            \"value\": \"2000163\" "
//	          + "                        }, "
//	          + "                        { "
//	          + "                            \"use\": \"official\", "
//	          + "                            \"type\": { "
//	          + "                                \"coding\": [ "
//	          + "                                    { "
//	          + "                                        \"system\": \"http://hl7.org/fhir/v2/0203\", "
//	          + "                                        \"code\": \"SB\" "
//	          + "                                    } "
//	          + "                                ] "
//	          + "                            }, "
//	          + "                            \"system\": \"http://hl7.org/fhir/sid/us-ssn\", "
//	          + "                            \"value\": \"999-61-4803\" "
//	          + "                        } "
//	          + "                    ], "
//	          + "                    \"name\": [ "
//	          + "                        { "
//	          + "                            \"use\": \"usual\", "
//	          + "                            \"text\": \"Mr. Aurelio227 Cruickshank494\", "
//	          + "                            \"family\": [ "
//	          + "                                \"Cruickshank494\" "
//	          + "                            ], "
//	          + "                            \"given\": [ "
//	          + "                                \"Aurelio227\" "
//	          + "                            ] "
//	          + "                        } "
//	          + "                    ], "
//	          + "                    \"telecom\": [ "
//	          + "                        { "
//	          + "                            \"system\": \"phone\", "
//	          + "                            \"value\": \"5555191065\", "
//	          + "                            \"use\": \"mobile\" "
//	          + "                        }, "
//	          + "                        { "
//	          + "                            \"system\": \"email\", "
//	          + "                            \"value\": \"Aurelio227.Cruickshank494@email.example\" "
//	          + "                        } "
//	          + "                    ], "
//	          + "                    \"gender\": \"male\", "
//	          + "                    \"birthDate\": \"1995-02-06\", "
//	          + "                    \"deceasedBoolean\": \"false\", "
//	          + "                    \"address\": [ "
//	          + "                        { "
//	          + "                            \"line\": [ "
//	          + "                                \"909 Rohan Highlands\" "
//	          + "                            ], "
//	          + "                            \"city\": \"Mesa\", "
//	          + "                            \"state\": \"Arizona\", "
//	          + "                            \"postalCode\": \"85120\" "
//	          + "                        } "
//	          + "                    ], "
//	          + "                    \"maritalStatus\": { "
//	          + "                        \"coding\": [ "
//	          + "                            { "
//	          + "                                \"system\": \"http://hl7.org/fhir/v3/NullFlavor\", "
//	          + "                                \"code\": \"UNK\", "
//	          + "                                \"display\": \"unknown\" "
//	          + "                            } "
//	          + "                        ] "
//	          + "                }, "
//	          + "                \"search\": { "
//	          + "                    \"mode\": \"match\" "
//	          + "                } "
//	          + "            } "
//	          + "          } "
//	          + "        } "
//	          + "    ] "
//	          + "} ";
}
