package gov.va.api.health.dataquery.service.controller.validation;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceSamples;
import gov.va.api.health.dataquery.service.controller.condition.ConditionSamples;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportSamples;
import gov.va.api.health.dataquery.service.controller.immunization.ImmunizationSamples;
import gov.va.api.health.dataquery.service.controller.medication.MedicationSamples;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderSamples;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementSamples;
import gov.va.api.health.dataquery.service.controller.observation.ObservationSamples;
import gov.va.api.health.dataquery.service.controller.patient.PatientSamples;
import gov.va.api.health.dataquery.service.controller.procedure.ProcedureSamples;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class DatamartValidationControllerTest {

  private DatamartValidationController controller = new DatamartValidationController();

  @SneakyThrows
  private String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @Test
  public void validationDeserializesOnValidPayloads() {
    assertThat(
            controller.validation(
                json(AllergyIntoleranceSamples.Datamart.create().allergyIntolerance())))
        .isEqualTo(AllergyIntoleranceSamples.Datamart.create().allergyIntolerance());
    assertThat(controller.validation(json(ConditionSamples.Datamart.create().condition())))
        .isEqualTo(ConditionSamples.Datamart.create().condition());
    assertThat(controller.validation(json(DiagnosticReportSamples.DatamartV1.create().reports())))
        .isEqualTo(DiagnosticReportSamples.DatamartV1.create().reports());
    assertThat(controller.validation(json(ImmunizationSamples.Datamart.create().immunization())))
        .isEqualTo(ImmunizationSamples.Datamart.create().immunization());
    assertThat(controller.validation(json(MedicationSamples.Datamart.create().medication())))
        .isEqualTo(MedicationSamples.Datamart.create().medication());
    assertThat(
            controller.validation(json(MedicationOrderSamples.Datamart.create().medicationOrder())))
        .isEqualTo(MedicationOrderSamples.Datamart.create().medicationOrder());
    assertThat(
            controller.validation(
                json(MedicationStatementSamples.Datamart.create().medicationStatement())))
        .isEqualTo(MedicationStatementSamples.Datamart.create().medicationStatement());
    assertThat(controller.validation(json(ObservationSamples.Datamart.create().observation())))
        .isEqualTo(ObservationSamples.Datamart.create().observation());
    assertThat(controller.validation(json(PatientSamples.Datamart.create().patient())))
        .isEqualTo(PatientSamples.Datamart.create().patient());
    assertThat(controller.validation(json(ProcedureSamples.Datamart.create().procedure())))
        .isEqualTo(ProcedureSamples.Datamart.create().procedure());
  }
}
