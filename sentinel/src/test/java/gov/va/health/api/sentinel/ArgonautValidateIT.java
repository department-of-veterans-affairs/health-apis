package gov.va.health.api.sentinel;

import gov.va.api.health.argonaut.api.bundle.AbstractBundle;
import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.argonaut.api.resources.Appointment;
import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.argonaut.api.resources.Encounter;
import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.argonaut.api.resources.Location;
import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.argonaut.api.resources.MedicationDispense;
import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.api.health.argonaut.api.resources.Organization;
import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.argonaut.api.resources.Practitioner;
import gov.va.api.health.argonaut.api.resources.Procedure;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.BeanUtils;

@RunWith(Parameterized.class)
public class ArgonautValidateIT {

  @Parameter(0)
  public String resource;

  @Parameter(1)
  public String id;

  @Parameter(2)
  public Class<? extends AbstractBundle<?>> bundleType;

  @Parameters(name = "{index}: {0}")
  public static List<Object[]> parameters() {
    TestIds ids = IdRegistrar.of(Sentinel.get().system()).registeredIds();

    return Arrays.asList(
        validate("AllergyIntolerance", ids.allergyIntolerance(), AllergyIntolerance.Bundle.class),
        validate("Appointment", ids.appointment(), Appointment.Bundle.class),
        validate("Condition", ids.condition(), Condition.Bundle.class),
        validate("DiagnosticReport", ids.diagnosticReport(), DiagnosticReport.Bundle.class),
        validate("Encounter", ids.encounter(), Encounter.Bundle.class),
        validate("Immunization", ids.immunization(), Immunization.Bundle.class),
        validate("Location", ids.location(), Location.Bundle.class),
        validate("Medication", ids.medication(), Medication.Bundle.class),
        validate("MedicationDispense", ids.medicationDispense(), MedicationDispense.Bundle.class),
        validate("MedicationOrder", ids.medicationOrder(), MedicationOrder.Bundle.class),
        validate(
            "MedicationStatement", ids.medicationStatement(), MedicationStatement.Bundle.class),
        validate("Observation", ids.observation(), Observation.Bundle.class),
        validate("Organization", ids.organization(), Organization.Bundle.class),
        validate("Patient", ids.patient(), Patient.Bundle.class),
        validate("Practitioner", ids.practitioner(), Practitioner.Bundle.class),
        validate("Procedure", ids.procedure(), Procedure.Bundle.class));
  }

  @SneakyThrows
  private static void murderResourceType(AbstractBundle<?> bundle) {
    Object something = bundle.entry().get(0).resource();
    BeanUtils.findMethod(something.getClass(), "resourceType", String.class)
        .invoke(something, new Object[] {null});
  }

  private static Object[] validate(
      String resource, String id, Class<? extends AbstractBundle<?>> bundleType) {
    return new Object[] {resource, id, bundleType};
  }

  private TestClient argonaut() {
    return Sentinel.get().clients().argonaut();
  }

  private final String apiPath() {
    return Sentinel.get().system().clients().argonaut().service().apiPath();
  }

  @Test
  public void validate() {
    String path = apiPath() + resource;

    AbstractBundle<?> bundle = argonaut().get(path + "?_id={id}", id).expectValid(bundleType);
    argonaut().post(path + "/$validate", bundle).expect(200).expectValid(OperationOutcome.class);
    /*
     * Murder the resource so it's not valid.
     */
    murderResourceType(bundle);
    argonaut().post(path + "/$validate", bundle).expect(400).expectValid(OperationOutcome.class);
  }
}
