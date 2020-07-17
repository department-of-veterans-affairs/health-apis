package gov.va.api.health.dataquery.service.controller.validation;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReport;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.v1.DatamartDiagnosticReports;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedication;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure;
import java.util.Map;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import lombok.SneakyThrows;
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
public class DatamartValidationController {

  private static Map<String, Class<?>> datamartResources =
      ImmutableMap.<String, Class<?>>builder()
          .put("AllergyIntolerance", DatamartAllergyIntolerance.class)
          .put("Condition", DatamartCondition.class)
          .put("DiagnosticReport1", DatamartDiagnosticReports.class)
          .put("DiagnosticReport2", DatamartDiagnosticReport.class)
          .put("Immunization", DatamartImmunization.class)
          .put("Medication", DatamartMedication.class)
          .put("MedicationOrder", DatamartMedicationOrder.class)
          .put("MedicationStatement", DatamartMedicationStatement.class)
          .put("Observation", DatamartObservation.class)
          .put("Patient", DatamartPatient.class)
          .put("Procedure", DatamartProcedure.class)
          .build();

  private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  /** Validation endpoint. Hey... it validates! */
  @Loggable(arguments = false)
  @PostMapping()
  @SneakyThrows
  public Object validation(@RequestBody String payload) {

    JsonNode jsonNode = createMapper().readTree(payload);
    String objectType = jsonNode.get("objectType").asText();
    if ("DiagnosticReport".equals(objectType)) {
      objectType = objectType.concat(jsonNode.get("objectVersion").asText());
    }
    Object datamartObject =
        createMapper().convertValue(jsonNode, datamartResources.get(objectType));
    var violations = validator.validate(datamartObject);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException("Payload is not valid,", violations);
    }
    return datamartObject;
  }
}
