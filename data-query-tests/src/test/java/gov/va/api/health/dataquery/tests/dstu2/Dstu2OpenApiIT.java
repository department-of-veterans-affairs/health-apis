package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.dataquery.tests.TestAssumptionUtility.assumeAllButLocal;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
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
    assumeAllButLocal();
    log.info("Verify {}openapi.json is valid (200)", apiPath());
    assertThat(
            RestAssured.given()
                .spec(ResourceVerifier.dstu2().dataQuery().service().requestSpecification())
                .get(apiPath() + "openapi.json")
                .statusCode())
        .isEqualTo(200);
  }
}
