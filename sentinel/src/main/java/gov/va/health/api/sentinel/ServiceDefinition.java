package gov.va.health.api.sentinel;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/** Defines particulars for interacting with a specific service. */
@Slf4j
@Value
@Builder
@AllArgsConstructor
public class ServiceDefinition {
  static {
    log.info(
        "Using jargonaut header is {} (Override -Djargonaut=true|false)",
        System.getProperty("jargonaut", "unset"));
  }

  String url;
  int port;
  @NonNull String apiPath;
  Supplier<Optional<String>> accessToken;

  RequestSpecification requestSpecification() {
    RequestSpecification spec =
        RestAssured.given()
            .baseUri(url())
            .port(port())
            .relaxedHTTPSValidation()
            .log()
            .ifValidationFails();
    String jargonaut = System.getProperty("jargonaut");
    if (isNotBlank(jargonaut)) {
      spec.header("jargonaut", jargonaut);
    }
    Optional<String> token = accessToken.get();
    if (token.isPresent()) {
      spec = spec.header("Authorization", "Bearer " + token.get());
    }
    return spec;
  }
}
