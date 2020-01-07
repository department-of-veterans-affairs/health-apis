package gov.va.api.health.dataquery.service.controller.encounter;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.Encounter;
import java.util.List;
import org.junit.Test;

public class Dstu2EncounterIncludesIcnMajigTest {
  @Test
  public void extractIcn() {
    ExtractIcnValidator.builder()
        .majig(new Dstu2EncounterIncludesIcnMajig())
        .body(
            Encounter.builder()
                .id("123")
                .patient(Reference.builder().reference("Patient/1010101010V666666").build())
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }
}
