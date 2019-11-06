package gov.va.api.health.dataquery.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.dataquery.service.controller.BulkFhirCount;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.categories.Local;
import java.util.LinkedList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Slf4j
public class PatientBulkFhirIT {

  private final String apiPath() {
    return TestClients.internalDataQuery().service().apiPath();
  }

  @Test
  @Category(
    value = {
      Local.class,
      LabDataQueryPatient.class,
      ProdDataQueryPatient.class,
    }
  )
  public void bulkFhirPatientSearch() {
    log.info(
        "Using: " + TestClients.internalDataQuery().service().urlWithApiPath() + "internal/bulk");
    ExpectedResponse responseAll =
        TestClients.internalDataQuery()
            .get(
                ImmutableMap.of("bulk", System.getProperty("bulk-token", "some-token")),
                apiPath() + "internal/bulk/Patient?page=1&_count=6");
    responseAll.expect(200);
    List<Patient> allPatients = responseAll.expectListOf(Patient.class);
    ExpectedResponse responseFirstHalf =
        TestClients.internalDataQuery()
            .get(
                ImmutableMap.of("bulk", System.getProperty("bulk-token", "some-token")),
                apiPath() + "internal/bulk/Patient?page=1&_count=3");
    responseFirstHalf.expect(200);
    List<Patient> firstHalfPatients = responseFirstHalf.expectListOf(Patient.class);
    ExpectedResponse responseSecondHalf =
        TestClients.internalDataQuery()
            .get(
                ImmutableMap.of("bulk", System.getProperty("bulk-token", "some-token")),
                apiPath() + "internal/bulk/Patient?page=2&_count=3");
    responseSecondHalf.expect(200);
    List<Patient> secondHalfPatients = responseSecondHalf.expectListOf(Patient.class);
    List<Patient> combined = new LinkedList<>();
    combined.addAll(firstHalfPatients);
    combined.addAll(secondHalfPatients);
    assertThat(allPatients)
        .withFailMessage("We can't look at production data on failures.")
        .isEqualTo(combined);
  }

  @Test
  @Category({Local.class, LabDataQueryPatient.class, ProdDataQueryPatient.class})
  @SneakyThrows
  public void bulkPatientCount() {
    log.info(
        "Using: " + TestClients.internalDataQuery().service().urlWithApiPath() + "internal/bulk");
    ExpectedResponse response =
        TestClients.internalDataQuery()
            .get(
                ImmutableMap.of("bulk", System.getProperty("bulk-token", "some-token")),
                apiPath() + "internal/bulk/Patient/count");
    response.expect(200);
    var bulkFhirCount = response.expectValid(BulkFhirCount.class);
    assertThat(bulkFhirCount.count()).isGreaterThan(3);
  }
}
