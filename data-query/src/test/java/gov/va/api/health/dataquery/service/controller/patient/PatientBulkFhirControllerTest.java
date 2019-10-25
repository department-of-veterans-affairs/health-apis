package gov.va.api.health.dataquery.service.controller.patient;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.BulkFhirCount;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions.BadSearchParameter;
import java.util.ArrayList;
import lombok.SneakyThrows;
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
    assertThat(controller().patientCount())
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

  @Test
  public void patientExport() {
    ArrayList<Patient> patients = populateData();
    assertThat(json(controller().patientExport(1, 10))).isEqualTo(json(patients));
  }

  @Test(expected = BadSearchParameter.class)
  public void patientExportBadCountThrowsUnsatisfiedServletRequestParameterException() {
    controller().patientExport(1, -1);
  }

  @Test(expected = BadSearchParameter.class)
  public void patientExportBadPageThrowsUnsatisfiedServletRequestParameterException() {
    controller().patientExport(0, 15);
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
}
