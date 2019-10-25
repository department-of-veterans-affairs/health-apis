package gov.va.api.health.dataquery.service.controller.patient;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.BulkFhirCount;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions.BadSearchParameter;
import java.util.ArrayList;
import lombok.SneakyThrows;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class PatientBulkFhirControllerTest {

  @Autowired private PatientRepository repository;

  @SneakyThrows
  private PatientEntity asEntity(DatamartPatient patient) {
    return PatientEntity.builder()
        .icn(patient.fullIcn())
        .payload(JacksonConfig.createMapper().writeValueAsString(patient))
        .build();
  }

  PatientBulkFhirController controller() {
    return new PatientBulkFhirController(10, repository);
  }

  @Test
  public void count() {
    populateData();
    assertThat(controller().count())
        .isEqualTo(
            BulkFhirCount.builder()
                .resourceType("Patient")
                .count(10)
                .maxRecordsPerPage(10)
                .build());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  private ArrayList<Patient> populateData() {
    var fhir = DatamartPatientSamples.Fhir.create();
    var datamart = DatamartPatientSamples.Datamart.create();
    var patients = new ArrayList<Patient>();
    for (int i = 0; i < 10; i++) {
      var id = String.valueOf(i);
      var dm = datamart.patient(id);
      repository.save(asEntity(dm));
      var patient = fhir.patient(id);
      patients.add(patient);
    }
    return patients;
  }

  @Test
  public void search() {
    ArrayList<Patient> patients = populateData();
    assertThat(json(controller().search(1, 10))).isEqualTo(json(patients));
  }

  @Test(expected = BadSearchParameter.class)
  public void searchBadCountThrowsUnsatisfiedServletRequestParameterException() {
    controller().search(1, -1);
  }

  @Test(expected = BadSearchParameter.class)
  public void searchBadPageThrowsUnsatisfiedServletRequestParameterException() {
    controller().search(0, 15);
  }

  @Test
  public void searchForEmptyPage() {
    populateData();
    assertThat(json(controller().search(2, 10))).isEqualTo(json(Lists.emptyList()));
  }

  @Test
  public void searchForSmallPagesSumsToLargerPage() {
    ArrayList<Patient> patients = populateData();
    ArrayList<Patient> sumPatients = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      sumPatients.add(controller().search(i, 1).get(0));
    }
    assertThat(json(patients)).isEqualTo(json(sumPatients));
  }
}
