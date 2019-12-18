package gov.va.api.health.dataquery.service.controller.validate;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReports;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedication;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure;
import java.util.List;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Does stuff. */
@Slf4j
@Validated
@RestController
@RequestMapping(
  value = {"/datamart/validation"},
  produces = {"application/json"}
)
public class DatamartValidationController {

  private static List<Class<?>> datamartResources =
      List.of(
          DatamartAllergyIntolerance.class,
          DatamartCondition.class,
          DatamartDiagnosticReports.class,
          DatamartImmunization.class,
          DatamartMedication.class,
          DatamartMedicationOrder.class,
          DatamartMedicationStatement.class,
          DatamartObservation.class,
          DatamartPatient.class,
          DatamartProcedure.class);

  /** Validation endpoint. Hey... it validates! */
  @PostMapping("/")
  @SneakyThrows
  public Object validation(@RequestBody String payload) {

    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    Object datamartObject = null;

    for (Class<?> resourceType : datamartResources) {
      try {
        datamartObject = createMapper().readValue(payload, resourceType);
        var violations = validator.validate(datamartObject);
        if (!violations.isEmpty()) {
          throw new ConstraintViolationException("Payload is not valid,", violations);
        }
        log.info("Unmarshalling payload as {}", resourceType.toString());
      } catch (JsonProcessingException e) {
        log.info("Cannot unmarshall payload as {}", resourceType.toString());
      }
    }

    return datamartObject;
  }
}
