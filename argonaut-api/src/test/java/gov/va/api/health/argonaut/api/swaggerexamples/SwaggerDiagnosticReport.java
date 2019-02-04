package gov.va.api.health.argonaut.api.swaggerexamples;

import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerDiagnosticReport {
  static final SomeClass SWAGGER_EXAMPLE_DIAGNOSTIC_REPORT =
      XXX.builder()
          .resourceType("DiagnosticReport")
          .id("0757389a-6e06-51bd-aac0-bd0244e51e46")
          .status("final")
          .category(
              XXX.builder()
                  .coding(
                      asList(
                          XXX.builder()
                              .system("http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
                              .code("LAB")
                              .display("Laboratory")
                              .build()))
                  .build())
          .code(XXX.builder().text("panel").build())
          .effectiveDateTime("2011-04-04T01:15:52Z")
          .issued("2011-04-04T01:15:52Z")
          .subject(
              XXX.builder()
                  .reference("https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                  .display("Mr. Aurelio227 Cruickshank494")
                  .build())
          .build();
//
//  static final String DIAGNOSTIC_REPORT =
//	      "{ "
//	          + "   \"resourceType\": \"DiagnosticReport\", "
//	          + "   \"id\": \"0757389a-6e06-51bd-aac0-bd0244e51e46\", "
//	          + "   \"status\": \"final\", "
//	          + "   \"category\": { "
//	          + "      \"coding\": [ "
//	          + "         { "
//	          + "            \"system\": \"http://hl7.org/fhir/ValueSet/diagnostic-service-sections\", "
//	          + "            \"code\": \"LAB\", "
//	          + "            \"display\": \"Laboratory\" "
//	          + "         } "
//	          + "      ] "
//	          + "   }, "
//	          + "   \"code\": { "
//	          + "      \"text\": \"panel\" "
//	          + "   }, "
//	          + "   \"effectiveDateTime\": \"2011-04-04T01:15:52Z\", "
//	          + "   \"issued\": \"2011-04-04T01:15:52Z\", "
//	          + "   \"subject\": { "
//	          + "      \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/2000163\", "
//	          + "      \"display\": \"Mr. Aurelio227 Cruickshank494\" "
//	          + "   } "
//	          + "} ";
  
  static final SomeClass SWAGGER_EXAMPLE_DIAGNOSTIC_REPORT_BUNDLE =
      XXX.builder()
          .resourceType("Bundle")
          .type("searchset")
          .total(1)
          .link(
              asList(
                  XXX.builder()
                      .relation("self")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/DiagnosticReport?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("first")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/DiagnosticReport?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("last")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/DiagnosticReport?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  XXX.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/DiagnosticReport/0757389a-6e06-51bd-aac0-bd0244e51e46")
                      .resource(
                          XXX.builder()
                              .fullUrl(
                                  "https://dev-api.va.gov/services/argonaut/v0/DiagnosticReport/0757389a-6e06-51bd-aac0-bd0244e51e46")
                              .resource(
                                  XXX.builder()
                                      .resourceType("DiagnosticReport")
                                      .id("0757389a-6e06-51bd-aac0-bd0244e51e46")
                                      .status("final")
                                      .category(
                                          XXX.builder()
                                              .coding(
                                                  asList(
                                                      XXX.builder()
                                                          .system(
                                                              "http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
                                                          .code("LAB")
                                                          .display("Laboratory")
                                                          .build()))
                                              .build())
                                      .code(XXX.builder().text("panel").build())
                                      .effectiveDateTime("2011-04-04T01:15:52Z")
                                      .issued("2011-04-04T01:15:52Z")
                                      .subject(
                                          XXX.builder()
                                              .reference(
                                                  "https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                                              .display("Mr. Aurelio227 Cruickshank494")
                                              .build())
                                      .build())
                              .search(XXX.builder().mode("match").build())
                              .build())
                      .build()))
          .build();
  
//  static final String DIAGNOSTIC_REPORT_BUNDLE =
//	      "{ "
//	          + "    \"resourceType\": \"Bundle\", "
//	          + "    \"type\": \"searchset\", "
//	          + "    \"total\": 1, "
//	          + "    \"link\": [ "
//	          + "        { "
//	          + "            \"relation\": \"self\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/DiagnosticReport?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\": \"first\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/DiagnosticReport?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\":\"last\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/DiagnosticReport?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        } "
//	          + "    ], "
//	          + "    \"entry\": [ "
//	          + "        { "
//	          + "            \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/DiagnosticReport/0757389a-6e06-51bd-aac0-bd0244e51e46\", "
//	          + "            \"resource\": { "
//	          + "                \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/DiagnosticReport/0757389a-6e06-51bd-aac0-bd0244e51e46\", "
//	          + "                \"resource\": { "
//	          + "                    \"resourceType\": \"DiagnosticReport\", "
//	          + "                    \"id\": \"0757389a-6e06-51bd-aac0-bd0244e51e46\", "
//	          + "                    \"status\": \"final\", "
//	          + "                    \"category\": { "
//	          + "                        \"coding\": [ "
//	          + "                            { "
//	          + "                                \"system\": \"http://hl7.org/fhir/ValueSet/diagnostic-service-sections\", "
//	          + "                                \"code\": \"LAB\", "
//	          + "                                \"display\": \"Laboratory\" "
//	          + "                            } "
//	          + "                        ] "
//	          + "                    }, "
//	          + "                    \"code\": { "
//	          + "                        \"text\": \"panel\" "
//	          + "                    }, "
//	          + "                    \"effectiveDateTime\": \"2011-04-04T01:15:52Z\", "
//	          + "                    \"issued\": \"2011-04-04T01:15:52Z\", "
//	          + "                    \"subject\": { "
//	          + "                        \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/2000163\", "
//	          + "                        \"display\": \"Mr. Aurelio227 Cruickshank494\" "
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
