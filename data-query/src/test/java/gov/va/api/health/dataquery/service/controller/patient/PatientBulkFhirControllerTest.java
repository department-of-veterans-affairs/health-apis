package gov.va.api.health.dataquery.service.controller.patient;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class PatientBulkFhirControllerTest {

  @Autowired EntityManager entityManager;

  PatientBulkFhirController controller;

  @Before
  public void _init() {
    entityManager.persist(PatientEntity.builder().icn("1").build());
    entityManager.persist(PatientEntity.builder().icn("2").build());
    entityManager.persist(PatientEntity.builder().icn("3").build());
    entityManager.persist(PatientEntity.builder().icn("4").build());
    controller = new PatientBulkFhirController(2, entityManager);
  }

  @Test
  public void count() {
    assertThat(controller.patientCount())
        .isEqualTo(BulkFhirCount.builder().resourceType("Patient").count(4).maxPageSize(2).build());
  }
}
