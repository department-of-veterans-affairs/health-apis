package gov.va.health.api.sentinel;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.Conformance;
import gov.va.health.api.sentinel.categories.LabArgo;
import gov.va.health.api.sentinel.categories.LabCargo;
import gov.va.health.api.sentinel.categories.Local;
import gov.va.health.api.sentinel.categories.ProdArgo;
import gov.va.health.api.sentinel.categories.ProdCargo;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ConformanceStatementIT {

  private final String apiPath() {
    return Sentinel.get().system().clients().argonaut().service().apiPath();
  }

  @Test
  @Category({Local.class, LabArgo.class, LabCargo.class, ProdArgo.class, ProdCargo.class})
  public void conformanceStatementIsValid() {
    ExpectedResponse response = Sentinel.get().clients().argonaut().get(apiPath() + "metadata");
    response.expect(200).expectValid(Conformance.class);
    String rawJson = response.response().asString();
    assertThat(rawJson)
        .withFailMessage("Tabs and newlines break our customer.")
        .doesNotContain("\n", "\t");
  }
}
