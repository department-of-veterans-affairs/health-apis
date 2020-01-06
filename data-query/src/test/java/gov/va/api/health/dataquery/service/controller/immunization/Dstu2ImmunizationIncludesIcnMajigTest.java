package gov.va.api.health.dataquery.service.controller.immunization;

import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.List;
import org.junit.Test;

public class Dstu2ImmunizationIncludesIcnMajigTest {
  @Test
  public void extractIcn() {
    ExtractIcnValidator.<Immunization>builder()
        .majig(new Dstu2ImmunizationIncludesIcnMajig())
        .body(
            Immunization.builder()
                .id("123")
                .patient(Reference.builder().reference("Patient/1010101010V666666").build())
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }
}
