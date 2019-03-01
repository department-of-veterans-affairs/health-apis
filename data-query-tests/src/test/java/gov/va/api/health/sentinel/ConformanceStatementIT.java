package gov.va.api.health.sentinel;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.Conformance;
import gov.va.api.health.sentinel.categories.LabDataQueryPatient;
import gov.va.api.health.sentinel.categories.LabDataQueryClinician;
import gov.va.api.health.sentinel.categories.Local;
import gov.va.api.health.sentinel.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.categories.ProdDataQueryClinician;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ConformanceStatementIT {
  private final String apiPath() {
    return TestClients.argonaut().service().apiPath();
  }

  @Test
  @Category({Local.class, LabDataQueryPatient.class, LabDataQueryClinician.class, ProdDataQueryPatient.class, ProdDataQueryClinician.class})
  public void conformanceStatementIsValid() {
    ExpectedResponse response = TestClients.argonaut().get(apiPath() + "metadata");
    response.expect(200).expectValid(Conformance.class);
    String rawJson = response.response().asString();
    assertThat(rawJson)
        .withFailMessage("Tabs and newlines break our customer.")
        .doesNotContain("\n", "\t");
  }
}
