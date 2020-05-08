package gov.va.api.health.dataquery.service.controller.patient;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.uscorer4.api.resources.Patient;
import org.junit.Test;

public class R4PatientTransformerTest {
  @Test
  public void empty() {
    assertThat(
            R4PatientTransformer.builder()
                .datamart(DatamartPatient.builder().build())
                .build()
                .toFhir())
        .isEqualTo(Patient.builder().resourceType("Patient").build());
  }
}
