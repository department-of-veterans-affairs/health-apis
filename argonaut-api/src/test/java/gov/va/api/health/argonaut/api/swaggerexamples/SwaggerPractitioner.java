package gov.va.api.health.argonaut.api.swaggerexamples;

import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerPractitioner {
  static final SomeClass SWAGGER_EXAMPLE_PRACTITIONER =
      XXX.builder()
          .resourceType("Practitioner")
          .id("9e8531cb-8069-5328-b737-938fa044a4e2")
          .active("true")
          .name(
              XXX.builder()
                  .use("usual")
                  .text("CARUNGIN,CHAD REY V")
                  .family(asList("CARUNGIN"))
                  .given(asList("CHAD REY"))
                  .suffix(asList("V"))
                  .build())
          .telecom(
              asList(
                  XXX.builder().system("phone").value("318-8387").use("work").build(),
                  XXX.builder().system("phone").value("638334566").use("home").build()))
          .gender("male")
          .birthDate("1964-02-16")
          .practitionerRole(
              asList(
                  XXX.builder()
                      .managingOrganization(
                          XXX.builder()
                              .reference(
                                  "https://api.va.gov/services/argonaut/v0/Organization/a29c7d77-fbae-5852-a905-1b18385b0339")
                              .display("MANILA-RO")
                              .build())
                      .role(
                          XXX.builder()
                              .coding(
                                  asList(
                                      XXX.builder()
                                          .system("http://hl7.org/fhir/practitioner-role")
                                          .code("doctor")
                                          .display("Doctor")
                                          .build()))
                              .build())
                      .location(
                          asList(
                              XXX.builder()
                                  .reference(
                                      "https://api.va.gov/services/argonaut/v0/Location/fb2a6389-0512-5ed1-9b49-811d5f3fdf3d")
                                  .display("PULMO CONSULT")
                                  .build(),
                              XXX.builder()
                                  .reference(
                                      "https://api.va.gov/services/argonaut/v0/Location/be4abb7c-87ea-57de-9347-df849897ea28")
                                  .display("ZZORANGE")
                                  .build(),
                              XXX.builder()
                                  .reference(
                                      "https://api.va.gov/services/argonaut/v0/Location/c7c5e616-4304-5956-804f-36038889f712")
                                  .display("ZZZ MAN PACT ORANGE")
                                  .build(),
                              XXX.builder()
                                  .reference(
                                      "https://api.va.gov/services/argonaut/v0/Location/2a560bee-9f00-5758-aaa3-21c0f929a7f6")
                                  .display("ZZORANGE INTERNAL MED CLINIC")
                                  .build(),
                              XXX.builder()
                                  .reference(
                                      "https://api.va.gov/services/argonaut/v0/Location/ada6bbf5-f121-582e-b832-13387fe4feff")
                                  .display("MAN PACT WALK-IN")
                                  .build(),
                              XXX.builder()
                                  .reference(
                                      "https://api.va.gov/services/argonaut/v0/Location/d05e1bc2-9527-51e9-bc35-0132f2af3c53")
                                  .display("E-CONSULT PULMO")
                                  .build(),
                              XXX.builder()
                                  .reference(
                                      "https://api.va.gov/services/argonaut/v0/Location/1cdb2958-e0fb-5711-8907-d582662e1be7")
                                  .display("MNL-SECMSG-PULMO")
                                  .build()))
                      .healthcareService(
                          asList(XXX.builder().display("PROFESSIONAL SERVICES DIVISION").build()))
                      .build()))
          .build();

//  static final String PRACTITIONER =
//      "{ "
//          + "    \"resourceType\": \"Practitioner\", "
//          + "    \"id\": \"9e8531cb-8069-5328-b737-938fa044a4e2\", "
//          + "    \"active\": \"true\", "
//          + "    \"name\": { "
//          + "        \"use\": \"usual\", "
//          + "        \"text\": \"CARUNGIN,CHAD REY V\", "
//          + "        \"family\": [ "
//          + "            \"CARUNGIN\" "
//          + "        ], "
//          + "        \"given\": [ "
//          + "            \"CHAD REY\" "
//          + "        ], "
//          + "        \"suffix\": [ "
//          + "            \"V\" "
//          + "        ] "
//          + "    }, "
//          + "    \"telecom\": [ "
//          + "        { "
//          + "            \"system\": \"phone\", "
//          + "            \"value\": \"318-8387\", "
//          + "            \"use\": \"work\" "
//          + "        }, "
//          + "        { "
//          + "            \"system\": \"phone\", "
//          + "            \"value\": \"638334566\", "
//          + "            \"use\": \"home\" "
//          + "        } "
//          + "    ], "
//          + "    \"gender\": \"male\", "
//          + "    \"birthDate\": \"1964-02-16\", "
//          + "    \"practitionerRole\": [ "
//          + "        { "
//          + "            \"managingOrganization\": { "
//          + "                \"reference\": \"https://api.va.gov/services/argonaut/v0/Organization/a29c7d77-fbae-5852-a905-1b18385b0339\", "
//          + "                \"display\": \"MANILA-RO\" "
//          + "            }, "
//          + "            \"role\": { "
//          + "                \"coding\": [ "
//          + "                    { "
//          + "                        \"system\": \"http://hl7.org/fhir/practitioner-role\", "
//          + "                        \"code\": \"doctor\", "
//          + "                        \"display\": \"Doctor\" "
//          + "                    } "
//          + "                ] "
//          + "            }, "
//          + "            \"location\": [ "
//          + "                { "
//          + "                    \"reference\": \"https://api.va.gov/services/argonaut/v0/Location/fb2a6389-0512-5ed1-9b49-811d5f3fdf3d\", "
//          + "                    \"display\": \"PULMO CONSULT\" "
//          + "                }, "
//          + "                { "
//          + "                    \"reference\": \"https://api.va.gov/services/argonaut/v0/Location/be4abb7c-87ea-57de-9347-df849897ea28\", "
//          + "                    \"display\": \"ZZORANGE\" "
//          + "                }, "
//          + "                { "
//          + "                    \"reference\": \"https://api.va.gov/services/argonaut/v0/Location/c7c5e616-4304-5956-804f-36038889f712\", "
//          + "                    \"display\": \"ZZZ MAN PACT ORANGE\" "
//          + "                }, "
//          + "                { "
//          + "                    \"reference\": \"https://api.va.gov/services/argonaut/v0/Location/2a560bee-9f00-5758-aaa3-21c0f929a7f6\", "
//          + "                    \"display\": \"ZZORANGE INTERNAL MED CLINIC\" "
//          + "                }, "
//          + "                { "
//          + "                    \"reference\": \"https://api.va.gov/services/argonaut/v0/Location/ada6bbf5-f121-582e-b832-13387fe4feff\", "
//          + "                    \"display\": \"MAN PACT WALK-IN\" "
//          + "                }, "
//          + "                { "
//          + "                    \"reference\": \"https://api.va.gov/services/argonaut/v0/Location/d05e1bc2-9527-51e9-bc35-0132f2af3c53\", "
//          + "                    \"display\": \"E-CONSULT PULMO\" "
//          + "                }, "
//          + "                { "
//          + "                    \"reference\": \"https://api.va.gov/services/argonaut/v0/Location/1cdb2958-e0fb-5711-8907-d582662e1be7\", "
//          + "                    \"display\": \"MNL-SECMSG-PULMO\" "
//          + "                } "
//          + "            ], "
//          + "            \"healthcareService\": [ "
//          + "                { "
//          + "                    \"display\": \"PROFESSIONAL SERVICES DIVISION\" "
//          + "                } "
//          + "            ] "
//          + "        } "
//          + "    ] "
//          + "} ";
  
  static final SomeClass SWAGGER_EXAMPLE_PRACTITIONER_BUNDLE =
      XXX.builder()
          .resourceType("Bundle")
          .type("searchset")
          .total(1)
          .link(
              asList(
                  XXX.builder()
                      .relation("self")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Practitioner?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("first")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Practitioner?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("last")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Practitioner?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  XXX.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/Practitioner/9e8531cb-8069-5328-b737-938fa044a4e2")
                      .resource(
                          XXX.builder()
                              .fullUrl(
                                  "https://dev-api.va.gov/services/argonaut/v0/Practitioner/9e8531cb-8069-5328-b737-938fa044a4e2")
                              .resource(
                                  XXX.builder()
                                      .resourceType("Practitioner")
                                      .id("9e8531cb-8069-5328-b737-938fa044a4e2")
                                      .active("true")
                                      .name(
                                          XXX.builder()
                                              .use("usual")
                                              .text("CARUNGIN,CHAD REY V")
                                              .family(asList("CARUNGIN"))
                                              .given(asList("CHAD REY"))
                                              .suffix(asList("V"))
                                              .build())
                                      .telecom(
                                          asList(
                                              XXX.builder()
                                                  .system("phone")
                                                  .value("318-8387")
                                                  .use("work")
                                                  .build(),
                                              XXX.builder()
                                                  .system("phone")
                                                  .value("638334566")
                                                  .use("home")
                                                  .build()))
                                      .gender("male")
                                      .birthDate("1964-02-16")
                                      .practitionerRole(
                                          asList(
                                              XXX.builder()
                                                  .managingOrganization(
                                                      XXX.builder()
                                                          .reference(
                                                              "https://api.va.gov/services/argonaut/v0/Organization/a29c7d77-fbae-5852-a905-1b18385b0339")
                                                          .display("MANILA-RO")
                                                          .build())
                                                  .role(
                                                      XXX.builder()
                                                          .coding(
                                                              asList(
                                                                  XXX.builder()
                                                                      .system(
                                                                          "http://hl7.org/fhir/practitioner-role")
                                                                      .code("doctor")
                                                                      .display("Doctor")
                                                                      .build()))
                                                          .build())
                                                  .location(
                                                      asList(
                                                          XXX.builder()
                                                              .reference(
                                                                  "https://api.va.gov/services/argonaut/v0/Location/fb2a6389-0512-5ed1-9b49-811d5f3fdf3d")
                                                              .display("PULMO CONSULT")
                                                              .build(),
                                                          XXX.builder()
                                                              .reference(
                                                                  "https://api.va.gov/services/argonaut/v0/Location/be4abb7c-87ea-57de-9347-df849897ea28")
                                                              .display("ZZORANGE")
                                                              .build(),
                                                          XXX.builder()
                                                              .reference(
                                                                  "https://api.va.gov/services/argonaut/v0/Location/c7c5e616-4304-5956-804f-36038889f712")
                                                              .display("ZZZ MAN PACT ORANGE")
                                                              .build(),
                                                          XXX.builder()
                                                              .reference(
                                                                  "https://api.va.gov/services/argonaut/v0/Location/2a560bee-9f00-5758-aaa3-21c0f929a7f6")
                                                              .display(
                                                                  "ZZORANGE INTERNAL MED CLINIC")
                                                              .build(),
                                                          XXX.builder()
                                                              .reference(
                                                                  "https://api.va.gov/services/argonaut/v0/Location/ada6bbf5-f121-582e-b832-13387fe4feff")
                                                              .display("MAN PACT WALK-IN")
                                                              .build(),
                                                          XXX.builder()
                                                              .reference(
                                                                  "https://api.va.gov/services/argonaut/v0/Location/d05e1bc2-9527-51e9-bc35-0132f2af3c53")
                                                              .display("E-CONSULT PULMO")
                                                              .build(),
                                                          XXX.builder()
                                                              .reference(
                                                                  "https://api.va.gov/services/argonaut/v0/Location/1cdb2958-e0fb-5711-8907-d582662e1be7")
                                                              .display("MNL-SECMSG-PULMO")
                                                              .build()))
                                                  .healthcareService(
                                                      asList(
                                                          XXX.builder()
                                                              .display(
                                                                  "PROFESSIONAL SERVICES DIVISION")
                                                              .build()))
                                                  .build()))
                                      .build())
                              .search(XXX.builder().mode("match").build())
                              .build())
                      .build()))
          .build();

//  static final String PRACTITIONER_BUNDLE =
//      "{ "
//          + "    \"resourceType\": \"Bundle\", "
//          + "    \"type\": \"searchset\", "
//          + "    \"total\": 1, "
//          + "    \"link\": [ "
//          + "        { "
//          + "            \"relation\": \"self\", "
//          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Practitioner?patient=1017283148V813263&page=1&_count=15\" "
//          + "        }, "
//          + "        { "
//          + "            \"relation\": \"first\", "
//          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Practitioner?patient=1017283148V813263&page=1&_count=15\" "
//          + "        }, "
//          + "        { "
//          + "            \"relation\":\"last\", "
//          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/Practitioner?patient=1017283148V813263&page=1&_count=15\" "
//          + "        } "
//          + "    ], "
//          + "    \"entry\": [ "
//          + "        { "
//          + "            \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Practitioner/9e8531cb-8069-5328-b737-938fa044a4e2\", "
//          + "            \"resource\": { "
//          + "                \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/Practitioner/9e8531cb-8069-5328-b737-938fa044a4e2\", "
//          + "                \"resource\": { "
//          + "                    \"resourceType\": \"Practitioner\", "
//          + "                    \"id\": \"9e8531cb-8069-5328-b737-938fa044a4e2\", "
//          + "                    \"active\": \"true\", "
//          + "                    \"name\": { "
//          + "                        \"use\": \"usual\", "
//          + "                        \"text\": \"CARUNGIN,CHAD REY V\", "
//          + "                        \"family\": [ "
//          + "                            \"CARUNGIN\" "
//          + "                        ], "
//          + "                        \"given\": [ "
//          + "                            \"CHAD REY\" "
//          + "                        ], "
//          + "                        \"suffix\": [ "
//          + "                            \"V\" "
//          + "                        ] "
//          + "                    }, "
//          + "                    \"telecom\": [ "
//          + "                        { "
//          + "                            \"system\": \"phone\", "
//          + "                            \"value\": \"318-8387\", "
//          + "                            \"use\": \"work\" "
//          + "                        }, "
//          + "                        { "
//          + "                            \"system\": \"phone\", "
//          + "                            \"value\": \"638334566\", "
//          + "                            \"use\": \"home\" "
//          + "                        } "
//          + "                    ], "
//          + "                    \"gender\": \"male\", "
//          + "                    \"birthDate\": \"1964-02-16\", "
//          + "                    \"practitionerRole\": [ "
//          + "                        { "
//          + "                            \"managingOrganization\": { "
//          + "                                \"reference\": \"https://api.va.gov/services/argonaut/v0/Organization/a29c7d77-fbae-5852-a905-1b18385b0339\", "
//          + "                                \"display\": \"MANILA-RO\" "
//          + "                            }, "
//          + "                            \"role\": { "
//          + "                                \"coding\": [ "
//          + "                                    { "
//          + "                                        \"system\": \"http://hl7.org/fhir/practitioner-role\", "
//          + "                                        \"code\": \"doctor\", "
//          + "                                        \"display\": \"Doctor\" "
//          + "                                    } "
//          + "                                ] "
//          + "                            }, "
//          + "                            \"location\": [ "
//          + "                                { "
//          + "                                    \"reference\": \"https://api.va.gov/services/argonaut/v0/Location/fb2a6389-0512-5ed1-9b49-811d5f3fdf3d\", "
//          + "                                    \"display\": \"PULMO CONSULT\" "
//          + "                                }, "
//          + "                                { "
//          + "                                    \"reference\": \"https://api.va.gov/services/argonaut/v0/Location/be4abb7c-87ea-57de-9347-df849897ea28\", "
//          + "                                    \"display\": \"ZZORANGE\" "
//          + "                                }, "
//          + "                                { "
//          + "                                    \"reference\": \"https://api.va.gov/services/argonaut/v0/Location/c7c5e616-4304-5956-804f-36038889f712\", "
//          + "                                    \"display\": \"ZZZ MAN PACT ORANGE\" "
//          + "                                }, "
//          + "                                { "
//          + "                                    \"reference\": \"https://api.va.gov/services/argonaut/v0/Location/2a560bee-9f00-5758-aaa3-21c0f929a7f6\", "
//          + "                                    \"display\": \"ZZORANGE INTERNAL MED CLINIC\" "
//          + "                                }, "
//          + "                                { "
//          + "                                    \"reference\": \"https://api.va.gov/services/argonaut/v0/Location/ada6bbf5-f121-582e-b832-13387fe4feff\", "
//          + "                                    \"display\": \"MAN PACT WALK-IN\" "
//          + "                                }, "
//          + "                                { "
//          + "                                    \"reference\": \"https://api.va.gov/services/argonaut/v0/Location/d05e1bc2-9527-51e9-bc35-0132f2af3c53\", "
//          + "                                    \"display\": \"E-CONSULT PULMO\" "
//          + "                                }, "
//          + "                                { "
//          + "                                    \"reference\": \"https://api.va.gov/services/argonaut/v0/Location/1cdb2958-e0fb-5711-8907-d582662e1be7\", "
//          + "                                   \"display\": \"MNL-SECMSG-PULMO\" "
//          + "                                } "
//          + "                            ], "
//          + "                            \"healthcareService\": [ "
//          + "                                { "
//          + "                                    \"display\": \"PROFESSIONAL SERVICES DIVISION\" "
//          + "                                } "
//          + "                            ] "
//          + "                        } "
//          + "                    ] "
//          + "                }, "
//          + "                \"search\": { "
//          + "                    \"mode\": \"match\" "
//          + "                } "
//          + "        } "
//          + "    } "
//          + "  ] "
//          + "}";
}
