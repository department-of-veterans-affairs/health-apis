package gov.va.api.health.dataquery.tests;

import static org.assertj.core.api.Assumptions.assumeThat;

import gov.va.api.health.sentinel.Environment;

public class TestAssumptionUtility {
  public static void assumeAllButLocal() {
    assumeThat(Environment.get())
        .overridingErrorMessage("Skipping in " + Environment.get())
        .isNotEqualTo(Environment.LOCAL);
  }

  public static void assumeLab() {
    assumeThat(Environment.get())
        .overridingErrorMessage("Skipping in " + Environment.get())
        .isEqualTo(Environment.LAB);
  }

  public static void assumeLocal() {
    assumeThat(Environment.get())
        .overridingErrorMessage("Skipping in " + Environment.get())
        .isEqualTo(Environment.LOCAL);
  }

  public static void assumeProduction() {
    assumeThat(Environment.get())
        .overridingErrorMessage("Skipping in " + Environment.get())
        .isEqualTo(Environment.PROD);
  }

  public static void assumeQa() {
    assumeThat(Environment.get())
        .overridingErrorMessage("Skipping in " + Environment.get())
        .isEqualTo(Environment.QA);
  }

  public static void assumeStaging() {
    assumeThat(Environment.get())
        .overridingErrorMessage("Skipping in " + Environment.get())
        .isEqualTo(Environment.STAGING);
  }

  public static void assumeStagingLab() {
    assumeThat(Environment.get())
        .overridingErrorMessage("Skipping in " + Environment.get())
        .isEqualTo(Environment.STAGING_LAB);
  }
}
