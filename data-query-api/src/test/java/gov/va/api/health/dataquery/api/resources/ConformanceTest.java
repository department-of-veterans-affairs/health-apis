package gov.va.api.health.dataquery.api.resources;

import static gov.va.api.health.dataquery.api.RoundTrip.assertRoundTrip;

import gov.va.api.health.dataquery.api.samples.SampleConformance;
import org.junit.Test;

public class ConformanceTest {

  @Test
  public void conformance() {
    assertRoundTrip(SampleConformance.get().conformance());
  }
}
