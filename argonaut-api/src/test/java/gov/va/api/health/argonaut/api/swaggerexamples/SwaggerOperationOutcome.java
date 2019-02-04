package gov.va.api.health.argonaut.api.swaggerexamples;

import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerOperationOutcome {
  static final SomeClass SWAGGER_EXAMPLE_OPERATION_OUTCOME =
      XXX.builder()
          .resourceType("OperationOutcome")
          .issue(
              asList(
                  XXX.builder()
                      .severity("error")
                      .code("forbidden")
                      .details(
                          XXX.builder().text("Token not allowed access to this patient.").build())
                      .build()))
          .build();
  
//  static final String OPERATION_OUTCOME =
//	      "{ "
//	          + "    \"resourceType\": \"OperationOutcome\", "
//	          + "    \"issue\": [ "
//	          + "        { "
//	          + "            \"severity\": \"error\", "
//	          + "            \"code\": \"forbidden\", "
//	          + "            \"details\": { "
//	          + "                \"text\": \"Token not allowed access to this patient.\" "
//	          + "            } "
//	          + "        } "
//	          + "    ] "
//	          + "} ";
}
