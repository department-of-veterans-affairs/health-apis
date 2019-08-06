package gov.va.api.health.dataquery.tests;

import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DataQueryScopes {

  public static List<String> labResources() {
    return Arrays.asList(
        "patient/AllergyIntolerance.read",
        "patient/Condition.read",
        "patient/DiagnosticReport.read",
        "patient/Immunization.read",
        "patient/Medication.read",
        "patient/MedicationOrder.read",
        "patient/MedicationStatement.read",
        "patient/Observation.read",
        "patient/Patient.read",
        "patient/Procedure.read",
        "openid",
        "profile",
        "offline_access",
        "launch/patient");
  }
}
