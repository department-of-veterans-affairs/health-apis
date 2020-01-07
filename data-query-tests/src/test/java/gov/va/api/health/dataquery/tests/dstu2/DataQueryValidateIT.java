package gov.va.api.health.dataquery.tests.dstu2;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.dataquery.tests.IdRegistrar;
import gov.va.api.health.dataquery.tests.SystemDefinitions;
import gov.va.api.health.dataquery.tests.TestClients;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle;
import gov.va.api.health.dstu2.api.resources.Location;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.dstu2.api.resources.Organization;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import gov.va.api.health.sentinel.TestClient;
import gov.va.api.health.sentinel.categories.Local;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.BeanUtils;

public class DataQueryValidateIT {
  private TestClient dataQuery;

  private TestIds ids;

  @SneakyThrows
  private static void murderResourceType(AbstractBundle<?> bundle) {
    Object something = bundle.entry().get(0).resource();
    BeanUtils.findMethod(something.getClass(), "resourceType", String.class)
        .invoke(something, new Object[] {null});
  }

  @Before
  public void _init() {
    ids = IdRegistrar.of(SystemDefinitions.systemDefinition()).registeredIds();
    dataQuery = TestClients.dstu2DataQuery();
  }

  private void validate(String resource, String id, Class<? extends AbstractBundle<?>> bundleType) {
    String path = dataQuery.service().apiPath() + resource;

    AbstractBundle<?> bundle = dataQuery.get(path + "?_id={id}", id).expectValid(bundleType);
    dataQuery.post(path + "/$validate", bundle).expect(200).expectValid(OperationOutcome.class);
    /*
     * Murder the resource so it's not valid.
     */
    murderResourceType(bundle);
    dataQuery.post(path + "/$validate", bundle).expect(400).expectValid(OperationOutcome.class);
  }

  @Test
  @Category(Local.class)
  public void validateAllergyIntolerance() {
    validate("AllergyIntolerance", ids.allergyIntolerance(), AllergyIntolerance.Bundle.class);
  }

  /*
  ----------------------------------------------------------------------------------
  Disabling Validate tests until the resource becomes available in datamart
  and the controller/transformer are implemented.
  ----------------------------------------------------------------------------------
  @Test
  @Category(Local.class)
  public void validateAppointment() {
    validate("Appointment", ids.appointment(), Appointment.Bundle.class);
  }

  @Test
  @Category(Local.class)
  public void validateMedicationDispense() {
    validate("MedicationDispense", ids.medicationDispense(), MedicationDispense.Bundle.class);
  }
  */

  @Test
  @Category(Local.class)
  public void validateCondition() {
    validate("Condition", ids.condition(), Condition.Bundle.class);
  }

  @Test
  @Category(Local.class)
  public void validateDiagnosticReport() {
    validate("DiagnosticReport", ids.diagnosticReport(), DiagnosticReport.Bundle.class);
  }

  /*
    ----------------------------------------------------------------------------------
    Disabling Validate tests until the resource becomes available in datamart
    and the controller/transformer are implemented.
    ----------------------------------------------------------------------------------
    @Test
    @Category(Local.class)
    public void validateEncounter() {
      validate("Encounter", ids.encounter(), Encounter.Bundle.class);
    }
  */
  @Test
  @Category(Local.class)
  public void validateImmunization() {
    validate("Immunization", ids.immunization(), Immunization.Bundle.class);
  }

  @Test
  @Category(Local.class)
  public void validateLocation() {
    validate("Location", ids.location(), Location.Bundle.class);
  }

  @Test
  @Category(Local.class)
  public void validateMedication() {
    validate("Medication", ids.medication(), Medication.Bundle.class);
  }

  @Test
  @Category(Local.class)
  public void validateMedicationOrder() {
    validate("MedicationOrder", ids.medicationOrder(), MedicationOrder.Bundle.class);
  }

  @Test
  @Category(Local.class)
  public void validateMedicationStatement() {
    validate("MedicationStatement", ids.medicationStatement(), MedicationStatement.Bundle.class);
  }

  @Test
  @Category(Local.class)
  public void validateObservation() {
    validate("Observation", ids.observation(), Observation.Bundle.class);
  }

  @Test
  @Category(Local.class)
  public void validateOrganization() {
    validate("Organization", ids.organization(), Organization.Bundle.class);
  }

  @Test
  @Category(Local.class)
  public void validatePatient() {
    validate("Patient", ids.patient(), Patient.Bundle.class);
  }

  @Test
  @Category(Local.class)
  public void validatePractitioner() {
    validate("Practitioner", ids.practitioner(), Practitioner.Bundle.class);
  }

  @Test
  @Category(Local.class)
  public void validateProcedure() {
    validate("Procedure", ids.procedure(), Procedure.Bundle.class);
  }
}
