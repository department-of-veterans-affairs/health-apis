package gov.va.api.health.dataquery.service.api.conformance;

import static gov.va.api.health.dataquery.service.api.RoundTrip.assertRoundTrip;

import gov.va.api.health.dataquery.service.api.samples.SampleConformance;
import org.junit.Test;

public class ConformanceTest {
  @Test
  public void conformance() {
    assertRoundTrip(SampleConformance.get().conformance());
  }
}
