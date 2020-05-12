package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.TestClients;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.TestClient;
import gov.va.api.health.sentinel.categories.Local;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Slf4j
public class R4OpenApiIT {
  final TestClient internal = TestClients.internalDataQuery();

  private String apiPath() {
    return internal.service().apiPath();
  }

  @Test
  @Category({Local.class, LabDataQueryPatient.class, ProdDataQueryPatient.class})
  public void openApiIsValid() {
    // Ultimately r4 is expected to be unified.
    // Until then, use the internal path for testing.
    log.info("Verify {}r4-openapi.json is valid (200)", apiPath());
    internal.get(apiPath() + "r4-openapi.json").expect(200);
  }
}
