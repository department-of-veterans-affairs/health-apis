package gov.va.api.health.dataquery.service.controller.observation;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import java.util.List;
import org.junit.Test;

public class ObservationIncludesIcnMajigTest {
  @Test
  public void dstu2() {
    ExtractIcnValidator.builder()
        .majig(new Dstu2ObservationIncludesIcnMajig())
        .body(
            gov.va.api.health.argonaut.api.resources.Observation.builder()
                .id("123")
                .subject(
                    gov.va.api.health.dstu2.api.elements.Reference.builder()
                        .reference("Patient/1010101010V666666")
                        .build())
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }

  @Test
  public void r4() {
    ExtractIcnValidator.builder()
        .majig(new R4ObservationIncludesIcnMajig())
        .body(
            gov.va.api.health.uscorer4.api.resources.Observation.builder()
                .id("123")
                .subject(
                    gov.va.api.health.r4.api.elements.Reference.builder()
                        .reference("Patient/1010101010V666666")
                        .build())
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }
}
