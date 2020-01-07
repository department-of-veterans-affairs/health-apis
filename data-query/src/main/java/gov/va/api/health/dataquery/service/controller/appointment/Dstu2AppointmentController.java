package gov.va.api.health.dataquery.service.controller.appointment;

import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Validator;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions.NotImplemented;
import gov.va.api.health.dstu2.api.resources.Appointment;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Appointment Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-Appointment.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
  value = {"/dstu2/Appointment"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class Dstu2AppointmentController {

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Appointment read(@PathVariable("publicId") String publicId) {
    throw new NotImplemented("not-implemented");
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Appointment.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    throw new NotImplemented("not-implemented");
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Appointment.Bundle searchByIdentifier(
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    throw new NotImplemented("not-implemented");
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public Appointment.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    throw new NotImplemented("not-implemented");
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Appointment.Bundle bundle) {
    return Dstu2Validator.create().validate(bundle);
  }
}
