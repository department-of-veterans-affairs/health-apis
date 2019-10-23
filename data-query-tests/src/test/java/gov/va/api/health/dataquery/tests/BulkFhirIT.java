package gov.va.api.health.dataquery.tests;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.BulkFhirCount;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.categories.Local;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class BulkFhirIT {
  private final String apiPath() {
    return TestClients.dataQuery().service().apiPath();
  }

  /**
   * Ensures the response is valid and the count is non-trivial. The count can change between
   * environments or if records were added. Little is gained from checking for a larger number of
   * records.
   */
  @Test
  @Category({
    Local.class,
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  @SneakyThrows
  public void bulkFhirCount() {
    ExpectedResponse response =
        TestClients.dataQuery().get(apiPath() + "internal/bulk/Patient/count");
    response.expect(200);
    BulkFhirCount bulkFhirCount = response.expectValid(BulkFhirCount.class);
    assertThat(bulkFhirCount.count()).isGreaterThan(3);
  }
}
