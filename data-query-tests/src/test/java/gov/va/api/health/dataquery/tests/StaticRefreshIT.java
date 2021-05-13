package gov.va.api.health.dataquery.tests;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.selenium.VaOauthRobot;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
public class StaticRefreshIT {

  private static Stream<Arguments> staticTokenRefresh() {
    return Stream.of(
        arguments(SystemDefinitions.systemDefinition().dstu2DataQuery().urlWithApiPath()),
        arguments(SystemDefinitions.systemDefinition().r4DataQuery().urlWithApiPath()));
  }

  private VaOauthRobot.Configuration configuration(String tokenUrl) {
    return VaOauthRobot.Configuration.builder()
        .authorization(
            VaOauthRobot.Configuration.Authorization.builder()
                .authorizeUrl("not-used")
                .redirectUrl("not-used")
                .clientId("not-used")
                .clientSecret("not-used")
                .state("not-used")
                .aud("not-used")
                .scopes(Set.of("not-used"))
                .build())
        .tokenUrl(tokenUrl)
        .user(
            VaOauthRobot.Configuration.UserCredentials.builder()
                .id("not-used")
                .password("not-used")
                .build())
        .build();
  }

  @ParameterizedTest
  @MethodSource
  void staticTokenRefresh(String tokenUrlBasePath) {
    assumeEnvironmentNotIn(Environment.LOCAL);
    var tokenUrl = tokenUrlBasePath + "token";
    log.info("Verify token refresh at {}", tokenUrl);
    var robot = VaOauthRobot.of(configuration(tokenUrl));
    VaOauthRobot.TokenExchange tokenToRefresh =
        VaOauthRobot.TokenExchange.builder()
            .patient(DataQueryResourceVerifier.ids().patient())
            .refreshToken(System.getProperty("static-refresh-token"))
            .build();
    var newToken = robot.refreshUserAccessToken(tokenToRefresh, false);
    assertThat(newToken.isError()).isFalse();
  }
}
