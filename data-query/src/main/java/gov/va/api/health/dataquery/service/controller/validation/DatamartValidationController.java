package gov.va.api.health.dataquery.service.controller.validation;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.appointment.DatamartAppointment;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.dataquery.service.controller.device.DatamartDevice;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReport;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.dataquery.service.controller.location.DatamartLocation;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedication;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation;
import gov.va.api.health.dataquery.service.controller.organization.DatamartOrganization;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Validation;
import javax.validation.Validator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST validation endpoint for datamart records. This will verify that we can serialize and
 * deserialize a given record. Currently supports the base 10 resources in datamart.
 */
@Validated
@RestController
@RequestMapping(
    value = {"/datamart/validation"},
    produces = {"application/json"})
@Slf4j
public class DatamartValidationController {

  /** Datamart resources that are supported for validation. */
  // find src/main/java -name "Datamart*java" \
  //   | sed -s 's|.*/Datamart\(.*\).java|\1|' \
  //   | grep -v 'ValidationController' \
  //   | sort | sed 's/\(.*\)/.put("\1",Datamart\1.class)/'
  private static Map<String, Class<?>> datamartResources =
      ImmutableMap.<String, Class<?>>builder()
          .put("AllergyIntolerance", DatamartAllergyIntolerance.class)
          .put("Appointment", DatamartAppointment.class)
          .put("Condition", DatamartCondition.class)
          .put("Device", DatamartDevice.class)
          .put("DiagnosticReport2", DatamartDiagnosticReport.class)
          .put("Immunization", DatamartImmunization.class)
          .put("Location", DatamartLocation.class)
          .put("Medication", DatamartMedication.class)
          .put("MedicationOrder", DatamartMedicationOrder.class)
          .put("MedicationStatement", DatamartMedicationStatement.class)
          .put("Observation", DatamartObservation.class)
          .put("Organization", DatamartOrganization.class)
          .put("Patient", DatamartPatient.class)
          .put("Practitioner", DatamartPractitioner.class)
          .put("Procedure", DatamartProcedure.class)
          .build();

  private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  /** Validation endpoint. Hey... it validates! */
  @Loggable(arguments = false)
  @PostMapping()
  @SneakyThrows
  public ResponseEntity<Object> validation(@RequestBody String payload) {
    log.info("Validating Datamart payload");

    JsonNode jsonNode;
    try {
      jsonNode = createMapper().readTree(payload);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Cannot parse JSON payload: " + e.getMessage());
    }

    JsonNode objectTypeNode = jsonNode.get("objectType");
    if (objectTypeNode == null) {
      return ResponseEntity.badRequest().body("Missing \"objectType\" node.");
    }

    String objectType = objectTypeNode.asText();
    if ("DiagnosticReport".equals(objectType)) {
      objectType = objectType.concat(jsonNode.get("objectVersion").asText());
    }

    Class<?> classType = datamartResources.get(objectType);
    if (classType == null) {
      return ResponseEntity.badRequest()
          .body(String.format("Unknown object type \"%s\".", objectType));
    }

    Object datamartObject;
    try {
      datamartObject = createMapper().convertValue(jsonNode, classType);
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(
              String.format(
                  "Cannot parse objectType \"%s\" as %s: %s",
                  objectType, classType, e.getMessage()));
    }
    try {
      var violations = validator.validate(datamartObject);
      if (!violations.isEmpty()) {
        var errors =
            violations.stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining("\n"));

        return ResponseEntity.badRequest().body("Validation errors:\n" + errors);
      }
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Failed to validate: " + e.getMessage());
    }
    return ResponseEntity.ok(datamartObject);
  }
}
