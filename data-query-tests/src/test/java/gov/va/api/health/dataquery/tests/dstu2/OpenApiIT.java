package gov.va.api.health.dataquery.tests.dstu2;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dataquery.tests.TestClients;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.categories.Local;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class OpenApiIT {

  private final String apiPath() {
    return TestClients.dstu2DataQuery().service().apiPath();
  }

  @Test
  @Category({Local.class, LabDataQueryPatient.class, ProdDataQueryPatient.class})
  public void openApiIsValid() {
    requestOpenApi("json");
    requestOpenApi("yaml");
  }

  public void requestOpenApi(String fileExtension) {
    Response response =
        RestAssured.given()
            .spec(ResourceVerifier.dstu2().dataQuery().service().requestSpecification())
            .get(apiPath() + "openapi." + fileExtension);
    assertThat(response.getStatusCode()).isEqualTo(200);
  }
}
