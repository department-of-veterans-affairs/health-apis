package gov.va.api.health.dataquery.tests.dstu2;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.dataquery.service.controller.BulkFhirCount;
import gov.va.api.health.dataquery.tests.TestClients;
import gov.va.api.health.sentinel.ExpectedResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class PatientBulkFhirIT {
  private String apiPath() {
    return TestClients.internalDataQuery().service().urlWithApiPath();
  }

  @Test
  public void bulkFhirPatientSearch() {
    //todo what do here for env?
    log.info("Verify Patient Bulk Search internal/bulk/Patient?page=x&_count=y");
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
  public void bulkFhirPatientSearchPerformance() {
    // todo what do here for env?
    /*
     * We will ask for 5000 patients 100 times and log out the time it took to complete
     */
    log.info("Get a large chunk of patients 100x: internal/bulk/Patient?page=x&_count=y");
    Instant start = Instant.now();
    for (int i = 0; i < 100; i++) {
      ExpectedResponse responseAll =
          TestClients.internalDataQuery()
              .get(
                  ImmutableMap.of("bulk", System.getProperty("bulk-token", "some-token")),
                  apiPath() + "internal/bulk/Patient?page=1&_count=5000");
      responseAll.expect(200);
      responseAll.expectListOf(Patient.class);
    }
    Instant complete = Instant.now();
    log.info(
        "Call took {} milliseconds to complete retrieving patients 100 times",
        Duration.between(start, complete).toMillis());
  }

  @Test
  @SneakyThrows
  public void bulkPatientCount() {
    //todo what do here for env?
    String path = apiPath() + "internal/bulk/Patient/count";
    log.info("Verify bulk-fhir count [{}]", path);
    ExpectedResponse response =
        TestClients.internalDataQuery()
            .get(ImmutableMap.of("bulk", System.getProperty("bulk-token", "some-token")), path);
    response.expect(200);
    var bulkFhirCount = response.expectValid(BulkFhirCount.class);
    assertThat(bulkFhirCount.count()).isGreaterThan(3);
  }
}
