package gov.va.api.health.dataquery.tests;

import lombok.experimental.UtilityClass;

/** Utility class for loading properties that can be used for testing. */
@UtilityClass
public final class DataQueryProperties {
  public static String cdwTestPatient() {
    return System.getProperty("patient-id", "1011537977V693883");
  }
}
