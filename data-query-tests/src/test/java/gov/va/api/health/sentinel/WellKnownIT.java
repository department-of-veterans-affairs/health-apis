package gov.va.api.health.sentinel;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.sentinel.categories.LabDataQueryClinician;
import gov.va.api.health.sentinel.categories.Local;
import gov.va.api.health.sentinel.categories.ProdDataQueryClinician;
import gov.va.api.health.sentinel.categories.ProdDataQueryPatient;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class WellKnownIT {
  private final String apiPath() {
    return TestClients.dataQuery().service().apiPath();
  }

  @Test
  @Category({
    Local.class,
    // TODO ADQ-35 logged. Lab is missing this route. Put LabDataQueryPatient back after added.
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void wellKnownIsValid() {
    ExpectedResponse response =
        TestClients.dataQuery().get(apiPath() + ".well-known/smart-configuration");
    response.expect(200);
    String rawJson = response.response().asString();
    assertThat(rawJson)
        .withFailMessage("Tabs and newlines break our customer.")
        .doesNotContain("\n", "\t");
  }
}
