package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import java.util.List;
import org.junit.Test;

public class Dstu2PatientIncludesIcnMajigTest {
  @Test
  public void extractIcns() {
    ExtractIcnValidator.builder()
        .majig(new Dstu2PatientIncludesIcnMajig())
        .body(Patient.builder().id("666V666").build())
        .expectedIcns(List.of("666V666"))
        .build()
        .assertIcn();
  }
}
