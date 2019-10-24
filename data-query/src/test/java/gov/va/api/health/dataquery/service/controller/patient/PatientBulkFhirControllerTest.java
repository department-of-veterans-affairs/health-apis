package gov.va.api.health.dataquery.service.controller.patient;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.BulkFhirCount;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class PatientBulkFhirControllerTest {

  @Autowired private PatientRepository repository;

  PatientBulkFhirController controller() {
    return new PatientBulkFhirController(2, repository);
  }

  @Test
  public void count() {
    repository.save(PatientEntity.builder().icn("1").build());
    repository.save(PatientEntity.builder().icn("2").build());
    repository.save(PatientEntity.builder().icn("3").build());
    repository.save(PatientEntity.builder().icn("4").build());
    assertThat(controller().patientCount())
        .isEqualTo(
            BulkFhirCount.builder().resourceType("Patient").count(4).maxRecordsPerPage(2).build());
  }
}
