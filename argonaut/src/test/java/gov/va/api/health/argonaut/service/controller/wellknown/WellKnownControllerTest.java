package gov.va.api.health.argonaut.service.controller.wellknown;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.information.WellKnown;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import lombok.SneakyThrows;
import org.junit.Test;

public class WellKnownControllerTest {

  private WellKnown actual() {
    return WellKnown.builder()
        .tokenEndpoint("https://argonaut.lighthouse.va.gov/token")
        .authorizationEndpoint("https://argonaut.lighthouse.va.gov/authorize")
        .capabilities(
            asList(
                "context-standalone-patient",
                "launch-standalone",
                "permission-offline",
                "permission-patient"))
        .responseTypeSupported(asList("code", "refresh-token"))
        .scopesSupported(
            asList("patient/DiagnosticReport.read", "patient/Patient.read", "offline_access"))
        .build();
  }

  @SneakyThrows
  private String pretty(WellKnown wellKnown) {
    return JacksonConfig.createMapper()
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(wellKnown);
  }

  private WellKnownProperties properties() {
    return WellKnownProperties.builder()
        .security(
            WellKnownProperties.SecurityProperties.builder()
                .tokenEndpoint("https://argonaut.lighthouse.va.gov/token")
                .authorizeEndpoint("https://argonaut.lighthouse.va.gov/authorize")
                .build())
        .capabilities(
            asList(
                "context-standalone-patient",
                "launch-standalone",
                "permission-offline",
                "permission-patient"))
        .responseTypeSupported(asList("code", "refresh-token"))
        .scopesSupported(
            asList("patient/DiagnosticReport.read", "patient/Patient.read", "offline_access"))
        .build();
  }

  @Test
  @SneakyThrows
  public void read() {
    WellKnownProperties properties = properties();
    WellKnownController controller = new WellKnownController(properties);
    try {
      assertThat(pretty(controller.read())).isEqualTo(pretty(actual()));
    } catch (AssertionError e) {
      System.out.println(e.getMessage());
      throw e;
    }
  }
}
