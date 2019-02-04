package gov.va.api.health.argonaut.api.swaggerexamples;

import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerMedication {
  static final SomeClass SWAGGER_EXAMPLE_MEDICATION =
      XXX.builder()
          .resourceType("Medication")
          .id("f4163f35-1565-552b-a1b9-a2f8870e6f4a")
          .code(
              XXX.builder()
                  .coding(
                      asList(
                          XXX.builder()
                              .system("https://www.nlm.nih.gov/research/umls/rxnorm/")
                              .code("895994")
                              .display("120 Fluticasone propionate .044 MG/ACTUAT Inhaler")
                              .build()))
                  .text("120 ACTUAT Fluticasone propionate .044 MG/ACTUAT Inhaler")
                  .build())
          .product(
              XXX.builder()
                  .id("4024655")
                  .form(XXX.builder().text("1 dose(s) 1 time(s) per 1 days").build())
                  .build())
          .build();

//  static final String MEDICATION =
//	      "{ "
//	          + "    \"resourceType\": \"Medication\", "
//	          + "    \"id\": \"f4163f35-1565-552b-a1b9-a2f8870e6f4a\", "
//	          + "    \"code\": { "
//	          + "        \"coding\": [ "
//	          + "            { "
//	          + "                \"system\": \"https://www.nlm.nih.gov/research/umls/rxnorm/\", "
//	          + "                \"code\": \"895994\", "
//	          + "                \"display\": \"120 Fluticasone propionate .044 MG/ACTUAT Inhaler\" "
//	          + "            } "
//	          + "        ], "
//	          + "        \"text\": \"120 ACTUAT Fluticasone propionate .044 MG/ACTUAT Inhaler\" "
//	          + "    }, "
//	          + "    \"product\": { "
//	          + "        \"id\": \"4024655\", "
//	          + "        \"form\": { "
//	          + "            \"text\": \"1 dose(s) 1 time(s) per 1 days\" "
//	          + "        } "
//	          + "    } "
//	          + "} ";
  
  static final SomeClass SWAGGER_EXAMPLE_MEDICATION_BUNDLE =
      XXX.builder()
          .resourceType("Bundle")
          .type("searchset")
          .total(1)
          .link(
              asList(
                  XXX.builder()
                      .relation("self")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Medication?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("first")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Medication?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("last")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Medication?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  XXX.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/Medication/f4163f35-1565-552b-a1b9-a2f8870e6f4a")
                      .resource(
                          XXX.builder()
                              .fullUrl(
                                  "https://dev-api.va.gov/services/argonaut/v0/Medication/f4163f35-1565-552b-a1b9-a2f8870e6f4a")
                              .resource(
                                  XXX.builder()
                                      .resourceType("Medication")
                                      .id("f4163f35-1565-552b-a1b9-a2f8870e6f4a")
                                      .code(
                                          XXX.builder()
                                              .coding(
                                                  asList(
                                                      XXX.builder()
                                                          .system(
                                                              "https://www.nlm.nih.gov/research/umls/rxnorm/")
                                                          .code("895994")
                                                          .display(
                                                              "120 Fluticasone propionate .044 MG/ACTUAT Inhaler")
                                                          .build()))
                                              .text(
                                                  "120 ACTUAT Fluticasone propionate .044 MG/ACTUAT Inhaler")
                                              .build())
                                      .product(
                                          XXX.builder()
                                              .id("4024655")
                                              .form(
                                                  XXX.builder()
                                                      .text("1 dose(s) 1 time(s) per 1 days")
                                                      .build())
                                              .build())
                                      .build())
                              .search(XXX.builder().mode("match").build())
                              .build())
                      .build()))
          .build();

//  static final String MEDICATION_BUNDLE =
//      "{ "
//          + "    \"resourceType\": \"Bundle\", "
//          + "    \"type\": \"searchset\", "
//          + "    \"total\": 1, "
//          + "    \"link\": [ "
//          + "        { "
//          + "            \"relation\": \"self\", "
//          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Medication?patient=1017283148V813263&page=1&_count=15\" "
//          + "        }, "
//          + "        { "
//          + "            \"relation\": \"first\", "
//          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Medication?patient=1017283148V813263&page=1&_count=15\" "
//          + "        }, "
//          + "        { "
//          + "            \"relation\":\"last\", "
//          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Medication?patient=1017283148V813263&page=1&_count=15\" "
//          + "        } "
//          + "    ], "
//          + "    \"entry\": [ "
//          + "        { "
//          + "            \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Medication/f4163f35-1565-552b-a1b9-a2f8870e6f4a\", "
//          + "            \"resource\": { "
//          + "                \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Medication/f4163f35-1565-552b-a1b9-a2f8870e6f4a\", "
//          + "                \"resource\": { "
//          + "                    \"resourceType\": \"Medication\", "
//          + "                    \"id\": \"f4163f35-1565-552b-a1b9-a2f8870e6f4a\", "
//          + "                    \"code\": { "
//          + "                        \"coding\": [ "
//          + "                            { "
//          + "                                \"system\": \"https://www.nlm.nih.gov/research/umls/rxnorm/\", "
//          + "                                \"code\": \"895994\", "
//          + "                                \"display\": \"120 Fluticasone propionate .044 MG/ACTUAT Inhaler\" "
//          + "                            } "
//          + "                        ], "
//          + "                        \"text\": \"120 ACTUAT Fluticasone propionate .044 MG/ACTUAT Inhaler\" "
//          + "                    }, "
//          + "                    \"product\": { "
//          + "                        \"id\": \"4024655\", "
//          + "                        \"form\": { "
//          + "                            \"text\": \"1 dose(s) 1 time(s) per 1 days\" "
//          + "                        } "
//          + "                    } "
//          + "                }, "
//          + "                \"search\": { "
//          + "                    \"mode\": \"match\" "
//          + "                } "
//          + "        } "
//          + "    } "
//          + "  ] "
//          + "}";
}
