package gov.va.api.health.dataquery.service.controller.organization;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import java.util.List;
import org.junit.Test;

public class OrganizationIncludesIcnMajigTest {
  @Test
  public void dstu2() {
    ExtractIcnValidator.builder()
        .majig(new Dstu2OrganizationIncludesIcnMajig())
        .body(gov.va.api.health.dstu2.api.resources.Organization.builder().id("123").build())
        .expectedIcns(List.of("NONE"))
        .build()
        .assertIcn();
  }

  @Test
  public void stu3() {
    ExtractIcnValidator.builder()
        .majig(new Stu3OrganizationIncludesIcnMajig())
        .body(gov.va.api.health.stu3.api.resources.Organization.builder().id("123").build())
        .expectedIcns(List.of("NONE"))
        .build()
        .assertIcn();
  }
}
