package gov.va.api.health.dataquery.tests;

import gov.va.api.health.dataquery.tests.SystemDefinitions;
import gov.va.api.health.sentinel.*;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
@UtilityClass
public class Oauth {
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
        ExpectedResponse.of(request.request(Method.GET, requestPath, params));

    response.expect(status).expectValid(expected);
  }

  public String exchangeToken(List<String> scopes) {
    SystemOauthRobot.Configuration config =
        OauthRobotProperties.usingSystemProperties()
            .forSystemOauth()
            // Loading ClientId, ClientSecret, Audience, and TokenUrl will be done via properties
            .defaultScopes(scopes)
            .build()
            .systemOauthConfig();
    TokenExchange token = SystemOauthRobot.builder().config(config).build().token();
    if (token.isError()) {
      fail("Failed to get token result from oauth robot.");
    }
    return token.accessToken();
  }
}
