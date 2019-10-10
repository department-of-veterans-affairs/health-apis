package gov.va.api.health.dataquery.service.controller;

import static junit.framework.TestCase.fail;

import gov.va.api.health.dstu2.api.resources.Resource;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ExtractIcnValidator<M extends AbstractIncludesIcnMajig, R extends Resource> {

  M majig;
  R body;
  List<String> expectedIcns;

  /**
   * Assert that the ICNs from the Majig's extract function match the payload ICNs
   */
  public void assertIcn() {
    fail();;
  }

}
