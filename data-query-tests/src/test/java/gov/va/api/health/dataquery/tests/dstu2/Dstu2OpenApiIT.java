package gov.va.api.health.dataquery.tests.dstu2;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestClients;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class Dstu2OpenApiIT {

  private final String apiPath() {
    return TestClients.dstu2DataQuery().service().apiPath();
  }

  @Test
  public void openApiIsValid() {
    log.info("Verify {}openapi.json is valid (200)", apiPath());
    assertThat(
            RestAssured.given()
                .spec(
                    DataQueryResourceVerifier.dstu2().testClient().service().requestSpecification())
                .get(apiPath() + "openapi.json")
                .statusCode())
        .isEqualTo(200);
  }
}
