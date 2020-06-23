package gov.va.api.health.dataquery.service.controller.wellknown;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.metadata.MetadataProperties;
import gov.va.api.health.dstu2.api.information.WellKnown;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

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

  private MetadataProperties conformanceProperties() {
    return MetadataProperties.builder()
        .security(
            MetadataProperties.SecurityProperties.builder()
                .authorizeEndpoint("https://argonaut.lighthouse.va.gov/authorize")
                .tokenEndpoint("https://argonaut.lighthouse.va.gov/token")
                .build())
        .build();
  }

  @SneakyThrows
  private String pretty(WellKnown wellKnown) {
    return JacksonConfig.createMapper()
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(wellKnown);
  }

  @Test
  @SneakyThrows
  public void read() {
    WellKnownController controller =
        new WellKnownController(wellKnownProperties(), conformanceProperties());
    assertThat(pretty(controller.read())).isEqualTo(pretty(actual()));
  }

  private WellKnownProperties wellKnownProperties() {
    return WellKnownProperties.builder()
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
}
