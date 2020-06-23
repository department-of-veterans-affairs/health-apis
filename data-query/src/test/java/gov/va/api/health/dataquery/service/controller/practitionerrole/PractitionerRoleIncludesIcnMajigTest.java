package gov.va.api.health.dataquery.service.controller.practitionerrole;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PractitionerRoleIncludesIcnMajigTest {
  @Test
  public void stu3() {
    ExtractIcnValidator.builder()
        .majig(new Stu3PractitionerRoleIncludesIcnMajig())
        .body(gov.va.api.health.stu3.api.resources.PractitionerRole.builder().id("123").build())
        .expectedIcns(List.of("NONE"))
        .build()
        .assertIcn();
  }
}
