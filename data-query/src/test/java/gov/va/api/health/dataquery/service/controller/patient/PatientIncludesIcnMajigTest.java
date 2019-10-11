package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import java.util.List;
import org.junit.Test;

public class PatientIncludesIcnMajigTest {

  @Test
  public void extractIcns() {
    ExtractIcnValidator.<PatientIncludesIcnMajig, Patient>builder()
        .majig(new PatientIncludesIcnMajig())
        .body(Patient.builder().id("666V666").build())
        .expectedIcns(List.of("666V666"))
        .build()
        .assertIcn();
  }
}
