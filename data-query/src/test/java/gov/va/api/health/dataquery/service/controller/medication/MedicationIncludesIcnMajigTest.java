package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MedicationIncludesIcnMajigTest {

  @Test
  public void dstu2ExtractNoIcns() {
    ExtractIcnValidator.builder()
        .majig(new Dstu2MedicationIncludesIcnMajig())
        .body(gov.va.api.health.argonaut.api.resources.Medication.builder().id("123").build())
        .expectedIcns(List.of("NONE"))
        .build()
        .assertIcn();
  }

  @Test
  public void r42ExtractNoIcns() {
    ExtractIcnValidator.builder()
        .majig(new R4MedicationIncludesIcnMajig())
        .body(gov.va.api.health.uscorer4.api.resources.Medication.builder().id("123").build())
        .expectedIcns(List.of("NONE"))
        .build()
        .assertIcn();
  }
}
