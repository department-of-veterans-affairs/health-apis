package gov.va.api.health.dataquery.service.controller.location;

import static java.util.Collections.emptyList;

import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.Dstu2Validator;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.resources.Location;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Location Profile, see https://www.hl7.org/fhir/DSTU2/location.html for
 * implementation details.
 */
@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
    value = "/dstu2/Location",
    produces = {"application/json", "application/json+fhir", "application/fhir+json"})
public class Dstu2LocationController {
  private Dstu2Bundler bundler;

  private LocationRepository repository;

  private WitnessProtection witnessProtection;

  /** Spring constructor. */
  public Dstu2LocationController(
      @Autowired Dstu2Bundler bundler,
      @Autowired LocationRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private Location.Bundle bundle(
      MultiValueMap<String, String> parameters, List<Location> reports, int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("Location")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        Dstu2Bundler.BundleContext.of(
            linkConfig, reports, Location.Entry::new, Location.Bundle::new));
  }

  private LocationEntity entityById(String publicId) {
    Optional<LocationEntity> entity = repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Location read(@PathVariable("publicId") String publicId) {
    DatamartLocation location = entityById(publicId).asDatamartLocation();
    witnessProtection.registerAndUpdateReferences(
        List.of(location), resource -> Stream.of(resource.managingOrganization()));
    return Dstu2LocationTransformer.builder().datamart(location).build().toFhir();
  }

  /** Read raw. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    IncludesIcnMajig.addHeaderForNoPatients(response);
    return entityById(publicId).payload();
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Location.Bundle searchById(
      @RequestParam("_id") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    Location resource = read(publicId);
    return bundle(
        Parameters.builder()
            .add("identifier", publicId)
            .add("page", page)
            .add("_count", count)
            .build(),
        resource == null || count == 0 ? emptyList() : List.of(resource),
        resource == null ? 0 : 1);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Location.Bundle searchByIdentifier(
      @RequestParam("identifier") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(publicId, page, count);
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
      value = "/$validate",
      consumes = {"application/json", "application/json+fhir", "application/fhir+json"})
  public OperationOutcome validate(@RequestBody Location.Bundle bundle) {
    return Dstu2Validator.create().validate(bundle);
  }
}
