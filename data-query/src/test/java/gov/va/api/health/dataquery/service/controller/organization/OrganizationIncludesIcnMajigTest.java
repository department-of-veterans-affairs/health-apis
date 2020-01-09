package gov.va.api.health.dataquery.service.controller.organization;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.dstu2.api.resources.Organization;
import java.util.List;
import org.junit.Test;

public class OrganizationIncludesIcnMajigTest {
  @Test
  public void extractNoIcns() {
    ExtractIcnValidator.builder()
        .majig(new Dstu2OrganizationIncludesIcnMajig())
        .body(Organization.builder().id("123").build())
        .expectedIcns(List.of("NONE"))
        .build()
        .assertIcn();
  }
}
