package gov.va.api.health.dataquery.service.controller.condition;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ConditionIncludesIcnMajigTest {
  @Test
  public void dstu2() {
    ExtractIcnValidator.builder()
        .majig(new Dstu2ConditionIncludesIcnMajig())
        .body(
            gov.va.api.health.dstu2.api.resources.Condition.builder()
                .id("123")
                .patient(
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
        .majig(new R4ConditionIncludesIcnMajig())
        .body(
            gov.va.api.health.r4.api.resources.Condition.builder()
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
