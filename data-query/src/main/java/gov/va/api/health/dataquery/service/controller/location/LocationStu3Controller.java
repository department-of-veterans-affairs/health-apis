package gov.va.api.health.dataquery.service.controller.location;

import static java.util.Collections.emptyList;

import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.BundlerStu3;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.PageLinksStu3;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.ValidatorStu3;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.stu3.api.resources.Location;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Location Profile, see
 * https://www.fhir.org/guides/argonaut/pd/StructureDefinition-argo-location.html for implementation
 * details.
 */
@Validated
@RestController
@RequestMapping(
  value = {"/stu3/Location"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
@SuppressWarnings("WeakerAccess")
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class LocationStu3Controller {
  private BundlerStu3 bundler;

  private LocationRepository repository;

  private WitnessProtection witnessProtection;

  private Location.Bundle bundle(
      MultiValueMap<String, String> parameters, List<Location> reports, int totalRecords) {
    PageLinksStu3.LinkConfig linkConfig =
        PageLinksStu3.LinkConfig.builder()
            .path("Location")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        BundlerStu3.BundleContext.of(
            linkConfig, reports, Function.identity(), Location.Entry::new, Location.Bundle::new));
  }

  private LocationEntity findById(String publicId) {
    Optional<LocationEntity> entity = repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Location read(@PathVariable("publicId") String publicId) {
    DatamartLocation location = findById(publicId).asDatamartLocation();
    replaceReferences(List.of(location));
    return LocationStu3Transformer.builder().datamart(location).build().toFhir();
  }

  /** Read raw. */
  @GetMapping(
    value = {"/{publicId}"},
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    AbstractIncludesIcnMajig.addHeaderForNoPatients(response);
    return findById(publicId).payload();
  }

  private Collection<DatamartLocation> replaceReferences(Collection<DatamartLocation> resources) {
    witnessProtection.registerAndUpdateReferences(
        resources, resource -> Stream.of(resource.managingOrganization()));
    return resources;
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Location.Bundle searchById(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
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
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("identifier") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(datamartHeader, publicId, page, count);
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Location.Bundle bundle) {
    return ValidatorStu3.create().validate(bundle);
  }

  // PETERTODO
  //  GET [base]/Location?identifier=[system]|[code]
  //    Example: GET [base]/Location?identifier=http://hospital.smarthealthit.org/Location|123571
  //
  //    GET [base]/Location?name=[string]
  //    Example: GET [base]/Location?name=Health
  //
  //    GET [base]/Location?address=[string]
  //    Example: GET [base]/Location?address=Arbor
  //    Example: GET [base]/Location?address-city=Melbourne
  //    Example: GET [base]/Location?address-state=FL
  //
  //    Example: GET [base]/Location?address-postalcode=12345
}
