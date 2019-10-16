package gov.va.api.health.dataquery.service.controller.practitioner;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import java.util.List;
import org.junit.Test;

public class PractitionerIncludesIcnMajigTest {

  @Test
  public void extractNoIcns() {
    ExtractIcnValidator.<PractitionerIncludesIcnMajig, Practitioner>builder()
        .majig(new PractitionerIncludesIcnMajig())
        .body(Practitioner.builder().id("123").build())
        .expectedIcns(List.of("NONE"))
        .build()
        .assertIcn();
  }
}
