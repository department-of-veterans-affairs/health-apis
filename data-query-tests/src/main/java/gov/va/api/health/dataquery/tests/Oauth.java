package gov.va.api.health.dataquery.tests;

import static org.junit.jupiter.api.Assertions.fail;

import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.OauthRobotProperties;
import gov.va.api.health.sentinel.ServiceDefinition;
import gov.va.api.health.sentinel.SystemOauthRobot;
import gov.va.api.health.sentinel.TokenExchange;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class Oauth {
  /** Get an access token with the provided scopes associated. */
  public String exchangeToken(List<String> scopes) {
    SystemOauthRobot.Configuration config =
        OauthRobotProperties.usingSystemProperties()
            .forSystemOauth()
            .defaultScopes(scopes)
            .build()
            .systemOauthConfig();
    TokenExchange token = SystemOauthRobot.builder().config(config).build().token();
    if (token.isError()) {
      fail("Failed to get token result from oauth robot.");
    }
    return token.accessToken();
  }

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
