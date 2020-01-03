package gov.va.api.health.dataquery.service.controller.condition;

import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.List;
import org.junit.Test;

public class Dstu2ConditionIncludesIcnMajigTest {

  @Test
  public void extractIcn() {
    ExtractIcnValidator.<Dstu2ConditionIncludesIcnMajig, Condition>builder()
        .majig(new Dstu2ConditionIncludesIcnMajig())
        .body(
            Condition.builder()
                .id("123")
                .patient(Reference.builder().reference("Patient/1010101010V666666").build())
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }
}
