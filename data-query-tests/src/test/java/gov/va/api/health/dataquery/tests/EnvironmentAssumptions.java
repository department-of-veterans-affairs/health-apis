package gov.va.api.health.dataquery.tests;

import static org.assertj.core.api.Assumptions.assumeThat;

import gov.va.api.health.sentinel.Environment;

public class EnvironmentAssumptions {

  public static void assumeLocal() {
    assumeThat(Environment.get())
        .overridingErrorMessage("Skipping in " + Environment.get())
        .isEqualTo(Environment.LOCAL);
  }

  public static void assumeNotLocal() {
    assumeThat(Environment.get())
        .overridingErrorMessage("Skipping in " + Environment.get())
        .isNotEqualTo(Environment.LOCAL);
  }
}
