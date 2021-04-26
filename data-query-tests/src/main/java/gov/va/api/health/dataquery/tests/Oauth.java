package gov.va.api.health.dataquery.tests;

import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.ServiceDefinition;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import java.util.Arrays;
import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class Oauth {

  /** A rest-assured request, with a given access-token. */
  public void test(
      ServiceDefinition sd,
      int status,
      Class<?> expected,
      String token,
      String path,
      String... params) {
    var requestPath = sd.apiPath() + path;
    log.info(
        "Oauth: Expect {} ({}) for {} {}",
        expected.getSimpleName(),
        status,
        requestPath,
        Arrays.toString(params));
    // We have to rebuild the request spec.
    // Kong does not like multiple auth headers.
    // This prevents two Authorization headers from resulting in a 400.
    RequestSpecification request =
        RestAssured.given()
            .baseUri(sd.url())
            .port(sd.port())
            .relaxedHTTPSValidation()
            .headers(Map.of("Authorization", "Bearer " + token))
            .contentType("application/json")
            .accept("application/json");
    ExpectedResponse response =
        ExpectedResponse.of(request.request(Method.GET, requestPath, (Object[]) params));
    response.expect(status).expectValid(expected);
  }
}
