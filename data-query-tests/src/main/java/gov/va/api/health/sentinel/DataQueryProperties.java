package gov.va.api.health.sentinel;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class DataQueryProperties {
  public static String cdwTestPatient() {
    return System.getProperty("patient-id", "1011537977V693883");
  }
}
