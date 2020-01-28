package gov.va.api.health.dataquery.service.controller.encounter;

import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Validator;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dstu2.api.resources.Encounter;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Encounter, see http://www.hl7.org/fhir/DSTU2/encounter.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
    value = {"/dstu2/Encounter"},
    produces = {"application/json", "application/json+fhir", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class Dstu2EncounterController {

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
  public Encounter read(@PathVariable("publicId") String publicId) {
    throw new ResourceExceptions.NotImplemented("not-implemented");
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
  public Encounter.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    throw new ResourceExceptions.NotImplemented("not-implemented");
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
  public Encounter.Bundle searchByIdentifier(
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    throw new ResourceExceptions.NotImplemented("not-implemented");
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
      value = "/$validate",
      consumes = {"application/json", "application/json+fhir", "application/fhir+json"})
  public OperationOutcome validate(@RequestBody Encounter.Bundle bundle) {
    return Dstu2Validator.create().validate(bundle);
  }
}
