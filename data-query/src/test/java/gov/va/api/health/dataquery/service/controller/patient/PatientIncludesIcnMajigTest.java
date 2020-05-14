package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import java.util.List;
import org.junit.Test;

public class PatientIncludesIcnMajigTest {
  @Test
  public void dstu2() {
    ExtractIcnValidator.builder()
        .majig(new Dstu2PatientIncludesIcnMajig())
        .body(gov.va.api.health.argonaut.api.resources.Patient.builder().id("666V666").build())
        .expectedIcns(List.of("666V666"))
        .build()
        .assertIcn();
  }
  @Test
  public void r4() {
    ExtractIcnValidator.builder()
            .majig(new R4PatientIncludesIcnMajig())
            .body(gov.va.api.health.uscorer4.api.resources.Patient.builder().id("666V666").build())
            .expectedIcns(List.of("666V666"))
            .build()
            .assertIcn();
  }
}
