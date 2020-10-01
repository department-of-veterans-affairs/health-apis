package gov.va.api.health.dataquery.tests;

import static gov.va.api.health.dataquery.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableSet;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.ServiceDefinition;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import java.time.Instant;
import java.util.Set;
import lombok.Data;
import lombok.NonNull;
import org.junit.jupiter.api.Test;

public class LatestResourceEtlStatusIT {
  private static final String ETL_STATUS_PATH = "etl-status";

  @Test
  void checkCaching() {
    assumeEnvironmentIn(Environment.LOCAL);
    Set<Instant> times =
        ImmutableSet.of(timeOf(ETL_STATUS_PATH), timeOf(ETL_STATUS_PATH), timeOf(ETL_STATUS_PATH));
    assertThat(times.size()).isLessThan(3);
  }

  @Test
  void etlStatusIsAvailable() {
    assertThat(timeOf(ETL_STATUS_PATH)).isNotNull();
  }

  private Instant timeOf(@NonNull String path) {
    ServiceDefinition svc = systemDefinition().internalDataQuery();

    Health health =
        ExpectedResponse.of(
                RestAssured.given()
                    .baseUri(svc.url())
                    .port(svc.port())
                    .relaxedHTTPSValidation()
                    .request(Method.GET, svc.urlWithApiPath() + path))
            .expectValid(Health.class);

    Instant time = health.details.time;
    assertThat(time).isNotNull();
    return time;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static final class Details {
    Instant time;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static final class Health {
    Details details;
  }
}
