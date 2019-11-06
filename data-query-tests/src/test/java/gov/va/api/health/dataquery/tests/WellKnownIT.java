package gov.va.api.health.dataquery.tests;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.tests.categories.LabDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.categories.Local;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class WellKnownIT {

  private final String apiPath() {
    return TestClients.dataQuery().service().apiPath();
  }

  /**
   * She's not the prettiest, but local tests dont remove the dstu2 from the apiPath (done by the
   * ingress otherwise).
   */
  @Test
  @Category({Local.class})
  public void localWellKnownIsValid() {
    requestWellKnown("/");
  }

  public void requestWellKnown(String apiPath) {
    ExpectedResponse response =
        TestClients.dataQuery().get(apiPath + ".well-known/smart-configuration");
    response.expect(200);
    String rawJson = response.response().asString();
    assertThat(rawJson)
        .withFailMessage("Tabs and newlines break our customer.")
        .doesNotContain("\n", "\t");
  }

  @Test
  @Category({
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void wellKnownIsValid() {
    requestWellKnown(apiPath());
  }
}
