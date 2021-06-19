package gov.va.api.health.dataquery.service.controller.validation;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceSamples;
import gov.va.api.health.dataquery.service.controller.appointment.AppointmentSamples;
import gov.va.api.health.dataquery.service.controller.condition.ConditionSamples;
import gov.va.api.health.dataquery.service.controller.device.DeviceSamples;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportSamples;
import gov.va.api.health.dataquery.service.controller.immunization.ImmunizationSamples;
import gov.va.api.health.dataquery.service.controller.location.LocationSamples;
import gov.va.api.health.dataquery.service.controller.medication.MedicationSamples;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderSamples;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementSamples;
import gov.va.api.health.dataquery.service.controller.observation.ObservationSamples;
import gov.va.api.health.dataquery.service.controller.organization.OrganizationSamples;
import gov.va.api.health.dataquery.service.controller.patient.PatientSamples;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerSamples;
import gov.va.api.health.dataquery.service.controller.practitionerrole.PractitionerRoleSamples;
import gov.va.api.health.dataquery.service.controller.procedure.ProcedureSamples;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.ResponseEntity;

public class DatamartValidationControllerTest {
  private DatamartValidationController controller = new DatamartValidationController();

  @SneakyThrows
  static String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  static Stream<Arguments> supports() {
    return List.of(
            AllergyIntoleranceSamples.Datamart.create().allergyIntolerance(),
            AppointmentSamples.Datamart.create().appointment(),
            ConditionSamples.Datamart.create().condition(),
            DeviceSamples.Datamart.create().device(),
            DiagnosticReportSamples.DatamartV2.create().diagnosticReport(),
            ImmunizationSamples.Datamart.create().immunization(),
            LocationSamples.Datamart.create().location(),
            MedicationSamples.Datamart.create().medication(),
            MedicationOrderSamples.Datamart.create().medicationOrder(),
            MedicationStatementSamples.Datamart.create().medicationStatement(),
            ObservationSamples.Datamart.create().observation(),
            OrganizationSamples.Datamart.create().organization(),
            PatientSamples.Datamart.create().patient(),
            PractitionerSamples.Datamart.create().practitioner(),
            PractitionerRoleSamples.Datamart.create().practitionerRole(),
            ProcedureSamples.Datamart.create().procedure())
        .stream()
        .map(Arguments::of);
  }

  @Test
  void cannotParseJson() {
    assertThat(controller.validation("notjson"))
        .isEqualTo(
            ResponseEntity.badRequest()
                .body(
                    "Cannot parse JSON payload: Unrecognized token 'notjson': was expecting"
                        + " (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n"
                        + " at [Source: (String)\"notjson\"; line: 1, column: 8]"));
  }

  @Test
  void missingObjectType() {
    assertThat(controller.validation("{}"))
        .isEqualTo(ResponseEntity.badRequest().body("Missing \"objectType\" node."));
  }

  @MethodSource
  @ParameterizedTest
  void supports(Object datamartObject) {
    assertThat(controller.validation(json(datamartObject)).getBody()).isEqualTo(datamartObject);
  }

  @Test
  void unknownObjectType() {
    assertThat(controller.validation("{\"objectType\":\"foobar\"}"))
        .isEqualTo(ResponseEntity.badRequest().body("Unknown object type \"foobar\"."));
  }
}
