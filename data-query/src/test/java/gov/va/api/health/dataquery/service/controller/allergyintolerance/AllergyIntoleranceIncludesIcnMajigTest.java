package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AllergyIntoleranceIncludesIcnMajigTest {
  @Test
  public void dstu2() {
    ExtractIcnValidator.builder()
        .majig(new Dstu2AllergyIntoleranceIncludesIcnMajig())
        .body(
            gov.va.api.health.argonaut.api.resources.AllergyIntolerance.builder()
                .id("123")
                .patient(
                    gov.va.api.health.dstu2.api.elements.Reference.builder()
                        .reference("Patient/666V666")
                        .build())
                .build())
        .expectedIcns(List.of("666V666"))
        .build()
        .assertIcn();
  }

  @Test
  public void r4() {
    ExtractIcnValidator.builder()
        .majig(new R4AllergyIntoleranceIncludesIcnMajig())
        .body(
            gov.va.api.health.uscorer4.api.resources.AllergyIntolerance.builder()
                .id("123")
                .patient(
                    gov.va.api.health.r4.api.elements.Reference.builder()
                        .reference("Patient/666V666")
                        .build())
                .build())
        .expectedIcns(List.of("666V666"))
        .build()
        .assertIcn();
  }
}
