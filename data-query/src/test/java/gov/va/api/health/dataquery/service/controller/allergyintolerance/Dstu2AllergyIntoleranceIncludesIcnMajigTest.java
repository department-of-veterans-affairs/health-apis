package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.List;
import org.junit.Test;

public class Dstu2AllergyIntoleranceIncludesIcnMajigTest {

  @Test
  public void extractIcns() {
    ExtractIcnValidator.<AllergyIntolerance>builder()
        .majig(new Dstu2AllergyIntoleranceIncludesIcnMajig())
        .body(
            AllergyIntolerance.builder()
                .id("123")
                .patient(Reference.builder().reference("Patient/666V666").build())
                .build())
        .expectedIcns(List.of("666V666"))
        .build()
        .assertIcn();
  }
}
