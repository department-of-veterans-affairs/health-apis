package gov.va.api.health.dataquery.tests;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.tests.categories.LabDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.dstu2.api.resources.Conformance;
import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.categories.Local;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ConformanceStatementIT {
  private final String apiPath() {
    return TestClients.dataQuery().service().apiPath();
  }

  @Test
  @Category({
    Local.class,
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void conformanceStatementIsValid() {
    ExpectedResponse response = TestClients.dataQuery().get(apiPath() + "metadata");
    response.expect(200).expectValid(Conformance.class);
    String rawJson = response.response().asString();
    assertThat(rawJson)
        .withFailMessage("Tabs and newlines break our customer.")
        .doesNotContain("\n", "\t");
  }
}
