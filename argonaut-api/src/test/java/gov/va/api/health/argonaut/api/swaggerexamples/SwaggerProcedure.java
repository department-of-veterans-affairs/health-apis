package gov.va.api.health.argonaut.api.swaggerexamples;

import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerProcedure {
  static final SomeClass SWAGGER_EXAMPLE_PROCEDURE =
      XXX.builder()
          .resourceType("Procedure")
          .id("532070f1-cb7b-582e-9380-9e0ef27bc817")
          .subject(
              XXX.builder()
                  .reference("https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                  .display("Mr. Aurelio227 Cruickshank494")
                  .build())
          .status("completed")
          .code(
              XXX.builder()
                  .coding(
                      asList(
                          XXX.builder()
                              .display("Documentation of current medications")
                              .system("http://www.ama-assn.org/go/cpt")
                              .code("XXXXX")
                              .build()))
                  .build())
          .notPerformed("false")
          .performedDateTime("2017-04-24T01:15:52Z")
          .build();

//  static final String PROCEDURE =
//	      "{ "
//	          + "   \"resourceType\": \"Procedure\", "
//	          + "   \"id\": \"532070f1-cb7b-582e-9380-9e0ef27bc817\", "
//	          + "   \"subject\": { "
//	          + "      \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/2000163\", "
//	          + "      \"display\": \"Mr. Aurelio227 Cruickshank494\" "
//	          + "   }, "
//	          + "   \"status\": \"completed\", "
//	          + "   \"code\": { "
//	          + "      \"coding\": [ "
//	          + "         { "
//	          + "            \"display\": \"Documentation of current medications\", "
//	          + "            \"system\": \"http://www.ama-assn.org/go/cpt\", "
//	          + "            \"code\": \"XXXXX\" "
//	          + "         } "
//	          + "      ] "
//	          + "   }, "
//	          + "   \"notPerformed\": \"false\", "
//	          + "   \"performedDateTime\": \"2017-04-24T01:15:52Z\" "
//	          + "} ";
  
  static final SomeClass SWAGGER_EXAMPLE_PROCEDURE_BUNDLE =
	      XXX.builder()
	          .resourceType("Bundle")
	          .type("searchset")
	          .total(1)
	          .link(
	              asList(
	                  XXX.builder()
	                      .relation("self")
	                      .url(
	                          "https://dev-api.va.gov/services/argonaut/v0/Procedure?patient=1017283148V813263&page=1&_count=15")
	                      .build(),
	                  XXX.builder()
	                      .relation("first")
	                      .url(
	                          "https://dev-api.va.gov/services/argonaut/v0/Procedure?patient=1017283148V813263&page=1&_count=15")
	                      .build(),
	                  XXX.builder()
	                      .relation("last")
	                      .url(
	                          "https://dev-api.va.gov/services/argonaut/v0/Procedure?patient=1017283148V813263&page=1&_count=15")
	                      .build()))
	          .entry(
	              asList(
	                  XXX.builder()
	                      .fullUrl(
	                          "https://dev-api.va.gov/services/argonaut/v0/Procedure/532070f1-cb7b-582e-9380-9e0ef27bc817")
	                      .resource(
	                          XXX.builder()
	                              .fullUrl(
	                                  "https://dev-api.va.gov/services/argonaut/v0/Procedure/532070f1-cb7b-582e-9380-9e0ef27bc817")
	                              .resource(
	                                  XXX.builder()
	                                      .resourceType("Procedure")
	                                      .id("532070f1-cb7b-582e-9380-9e0ef27bc817")
	                                      .subject(
	                                          XXX.builder()
	                                              .reference(
	                                                  "https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
	                                              .display("Mr. Aurelio227 Cruickshank494")
	                                              .build())
	                                      .status("completed")
	                                      .code(
	                                          XXX.builder()
	                                              .coding(
	                                                  asList(
	                                                      XXX.builder()
	                                                          .display(
	                                                              "Documentation of current medications")
	                                                          .system("http://www.ama-assn.org/go/cpt")
	                                                          .code("XXXXX")
	                                                          .build()))
	                                              .build())
	                                      .notPerformed("false")
	                                      .performedDateTime("2017-04-24T01:15:52Z")
	                                      .build())
	                              .search(XXX.builder().mode("match").build())
	                              .build())
	                      .build()))
	          .build();
  
//  static final String PROCEDURE_BUNDLE =
//	      "{ "
//	          + "    \"resourceType\": \"Bundle\", "
//	          + "    \"type\": \"searchset\", "
//	          + "    \"total\": 1, "
//	          + "    \"link\": [ "
//	          + "        { "
//	          + "            \"relation\": \"self\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Procedure?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\": \"first\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Procedure?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\":\"last\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Procedure?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        } "
//	          + "    ], "
//	          + "    \"entry\": [ "
//	          + "        { "
//	          + "            \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Procedure/532070f1-cb7b-582e-9380-9e0ef27bc817\", "
//	          + "            \"resource\": { "
//	          + "                \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Procedure/532070f1-cb7b-582e-9380-9e0ef27bc817\", "
//	          + "                \"resource\": { "
//	          + "                    \"resourceType\": \"Procedure\", "
//	          + "                    \"id\": \"532070f1-cb7b-582e-9380-9e0ef27bc817\", "
//	          + "                    \"subject\": { "
//	          + "                       \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/2000163\", "
//	          + "                       \"display\": \"Mr. Aurelio227 Cruickshank494\" "
//	          + "                    }, "
//	          + "                    \"status\": \"completed\", "
//	          + "                    \"code\": { "
//	          + "                       \"coding\": [ "
//	          + "                          { "
//	          + "                             \"display\": \"Documentation of current medications\", "
//	          + "                             \"system\": \"http://www.ama-assn.org/go/cpt\", "
//	          + "                             \"code\": \"XXXXX\" "
//	          + "                          } "
//	          + "                       ] "
//	          + "                    }, "
//	          + "                    \"notPerformed\": \"false\", "
//	          + "                    \"performedDateTime\": \"2017-04-24T01:15:52Z\" "
//	          + "                }, "
//	          + "                \"search\": { "
//	          + "                    \"mode\": \"match\" "
//	          + "                } "
//	          + "            } "
//	          + "        } "
//	          + "    ] "
//	          + "} ";

}
