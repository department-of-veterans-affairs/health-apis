package gov.va.api.health.dataquery.tests.dstu2;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dataquery.tests.TestClients;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.categories.Local;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Slf4j
public class Dstu2OpenApiIT {

  private final String apiPath() {
    return TestClients.dstu2DataQuery().service().apiPath();
  }

  @Test
  @Category({Local.class, LabDataQueryPatient.class, ProdDataQueryPatient.class})
  public void openApiIsValid() {
    log.info("Verify {}openapi.json is valid (200)", apiPath());
    assertThat(RestAssured.given()
            .spec(ResourceVerifier.dstu2().dataQuery().service().requestSpecification())
            .get(apiPath() + "openapi.json").statusCode()).isEqualTo(200);
  }
}
