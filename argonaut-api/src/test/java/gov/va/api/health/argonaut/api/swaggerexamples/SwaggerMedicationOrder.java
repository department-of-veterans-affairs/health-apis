package gov.va.api.health.argonaut.api.swaggerexamples;

import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerMedicationOrder {
  static final SomeClass SWAGGER_EXAMPLE_MEDICATION_ORDER =
      XXX.builder()
          .resourceType("MedicationOrder")
          .id("f07dd74e-844e-5463-99d4-0ca4d5cbeb41")
          .dateWritten("2013-04-14T06:00:00Z")
          .status("active")
          .patient(
              XXX.builder()
                  .reference("https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                  .display("Mr. Aurelio227 Cruickshank494")
                  .build())
          ._prescriber(
              XXX.builder()
                  .extension(
                      asList(
                          XXX.builder()
                              .url("http://hl7.org/fhir/StructureDefinition/data-absent-reason")
                              .valueCode("unsupported")
                              .build()))
                  .build())
          .medicationReference(
              XXX.builder()
                  .reference(
                      "https://dev-api.va.gov/services/argonaut/v0/Medication/7b550d7f-2db8-5002-bc0c-150a70d02944")
                  .display("Hydrochlorothiazide 25 MG")
                  .build())
          .build();

//  static final String MEDICATION_ORDER =
//	      "{ "
//	          + "   \"resourceType\": \"MedicationOrder\", "
//	          + "   \"id\": \"f07dd74e-844e-5463-99d4-0ca4d5cbeb41\", "
//	          + "   \"dateWritten\": \"2013-04-14T06:00:00Z\", "
//	          + "   \"status\": \"active\", "
//	          + "   \"patient\": { "
//	          + "      \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/2000163\", "
//	          + "      \"display\": \"Mr. Aurelio227 Cruickshank494\" "
//	          + "   }, "
//	          + "   \"_prescriber\": { "
//	          + "       \"extension\": [ "
//	          + "           { "
//	          + "               \"url\": \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\", "
//	          + "               \"valueCode\": \"unsupported\" "
//	          + "           } "
//	          + "       ] "
//	          + "   }, "
//	          + "   \"medicationReference\": { "
//	          + "       \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Medication/7b550d7f-2db8-5002-bc0c-150a70d02944\", "
//	          + "       \"display\": \"Hydrochlorothiazide 25 MG\" "
//	          + "   } "
//	          + "} ";
  
  static final SomeClass SWAGGER_EXAMPLE_MEDICATION_ORDER_BUNDLE =
      XXX.builder()
          .resourceType("Bundle")
          .type("searchset")
          .total(1)
          .link(
              asList(
                  XXX.builder()
                      .relation("self")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationOrder?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("first")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationOrder?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  XXX.builder()
                      .relation("last")
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationOrder?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  XXX.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationOrder/f07dd74e-844e-5463-99d4-0ca4d5cbeb41")
                      .resource(
                          XXX.builder()
                              .fullUrl(
                                  "https://dev-api.va.gov/services/argonaut/v0/MedicationOrder/f07dd74e-844e-5463-99d4-0ca4d5cbeb41")
                              .resource(
                                  XXX.builder()
                                      .resourceType("MedicationOrder")
                                      .id("f07dd74e-844e-5463-99d4-0ca4d5cbeb41")
                                      .dateWritten("2013-04-14T06:00:00Z")
                                      .status("active")
                                      .patient(
                                          XXX.builder()
                                              .reference(
                                                  "https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                                              .display("Mr. Aurelio227 Cruickshank494")
                                              .build())
                                      ._prescriber(
                                          XXX.builder()
                                              .extension(
                                                  asList(
                                                      XXX.builder()
                                                          .url(
                                                              "http://hl7.org/fhir/StructureDefinition/data-absent-reason")
                                                          .valueCode("unsupported")
                                                          .build()))
                                              .build())
                                      .medicationReference(
                                          XXX.builder()
                                              .reference(
                                                  "https://dev-api.va.gov/services/argonaut/v0/Medication/7b550d7f-2db8-5002-bc0c-150a70d02944")
                                              .display("Hydrochlorothiazide 25 MG")
                                              .build())
                                      .build())
                              .search(XXX.builder().mode("match").build())
                              .build())
                      .build()))
          .build();
  
  static final String MEDICATION_ORDER_BUNDLE =
//	      "{ "
//	          + "    \"resourceType\": \"Bundle\", "
//	          + "    \"type\": \"searchset\", "
//	          + "    \"total\": 1, "
//	          + "    \"link\": [ "
//	          + "        { "
//	          + "            \"relation\": \"self\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationOrder?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\": \"first\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationOrder?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        }, "
//	          + "        { "
//	          + "            \"relation\":\"last\", "
//	          + "            \"url\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationOrder?patient=1017283148V813263&page=1&_count=15\" "
//	          + "        } "
//	          + "    ], "
//	          + "    \"entry\": [ "
//	          + "        { "
//	          + "            \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationOrder/f07dd74e-844e-5463-99d4-0ca4d5cbeb41\", "
//	          + "            \"resource\": { "
//	          + "                \"fullUrl\": \"https://dev-api.va.gov/services/argonaut/v0/MedicationOrder/f07dd74e-844e-5463-99d4-0ca4d5cbeb41\", "
//	          + "                \"resource\": { "
//	          + "                    \"resourceType\": \"MedicationOrder\", "
//	          + "                    \"id\": \"f07dd74e-844e-5463-99d4-0ca4d5cbeb41\", "
//	          + "                    \"dateWritten\": \"2013-04-14T06:00:00Z\", "
//	          + "                    \"status\": \"active\", "
//	          + "                    \"patient\": { "
//	          + "                       \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Patient/2000163\", "
//	          + "                       \"display\": \"Mr. Aurelio227 Cruickshank494\" "
//	          + "                    }, "
//	          + "                    \"_prescriber\": { "
//	          + "                        \"extension\": [ "
//	          + "                            { "
//	          + "                                \"url\": \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\", "
//	          + "                                \"valueCode\": \"unsupported\" "
//	          + "                            } "
//	          + "                        ] "
//	          + "                    }, "
//	          + "                    \"medicationReference\": { "
//	          + "                        \"reference\": \"https://dev-api.va.gov/services/argonaut/v0/Medication/7b550d7f-2db8-5002-bc0c-150a70d02944\", "
//	          + "                        \"display\": \"Hydrochlorothiazide 25 MG\" "
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
