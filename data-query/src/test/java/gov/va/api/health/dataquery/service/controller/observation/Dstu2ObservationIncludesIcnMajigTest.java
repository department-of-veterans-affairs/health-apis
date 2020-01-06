package gov.va.api.health.dataquery.service.controller.observation;

import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.List;
import org.junit.Test;

public class Dstu2ObservationIncludesIcnMajigTest {
  @Test
  public void extractIcns() {
    ExtractIcnValidator.<Observation>builder()
        .majig(new Dstu2ObservationIncludesIcnMajig())
        .body(
            Observation.builder()
                .id("123")
                .subject(Reference.builder().reference("Patient/1010101010V666666").build())
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }
}
