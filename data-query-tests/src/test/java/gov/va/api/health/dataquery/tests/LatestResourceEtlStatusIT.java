package gov.va.api.health.dataquery.tests;

import static gov.va.api.health.dataquery.tests.SystemDefinitions.systemDefinition;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableSet;
import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.ServiceDefinition;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class LatestResourceEtlStatusIT {

  @Test
  void checkCaching() {
    String path = "/etl-status";
    Set<Instant> times = ImmutableSet.of(timeOf(path), timeOf(path), timeOf(path));
    assertThat(times.size()).isLessThan(3);
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
  private static final class Health {
    Details details;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static final class Details {
    Instant time;
  }

}
