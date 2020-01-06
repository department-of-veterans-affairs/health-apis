package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import java.util.List;
import org.junit.Test;

public class Dstu2MedicationIncludesIcnMajigTest {
  @Test
  public void extractNoIcns() {
    ExtractIcnValidator.<Medication>builder()
        .majig(new Dstu2MedicationIncludesIcnMajig())
        .body(Medication.builder().id("123").build())
        .expectedIcns(List.of("NONE"))
        .build()
        .assertIcn();
  }
}
