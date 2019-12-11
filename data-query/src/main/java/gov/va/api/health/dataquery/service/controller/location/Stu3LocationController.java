package gov.va.api.health.dataquery.service.controller.location;

import static java.util.Collections.emptyList;

import com.google.common.collect.Iterables;
import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.Stu3Bundler;
import gov.va.api.health.dataquery.service.controller.Stu3Validator;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.stu3.api.resources.Location;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class Stu3LocationController {
  private Stu3Bundler bundler;

  private LocationRepository repository;

  private WitnessProtection witnessProtection;

  private static PageRequest page(int page, int count) {
    return PageRequest.of(page - 1, count == 0 ? 1 : count, LocationEntity.naturalOrder());
  }

  private Location.Bundle bundle(
      MultiValueMap<String, String> parameters, List<Location> reports, int totalRecords) {
    return bundler.bundle(
        PageLinks.LinkConfig.builder()
            .path("Location")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build(),
        reports,
        Location.Entry::new,
        Location.Bundle::new);
  }

  private LocationEntity entityById(String publicId) {
    Optional<LocationEntity> entity = repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Location read(@PathVariable("publicId") String publicId) {
    LocationEntity entity = entityById(publicId);
    return Iterables.getOnlyElement(transform(Stream.of(entity)));
  }

  /** Read raw. */
  @GetMapping(
    value = {"/{publicId}"},
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    AbstractIncludesIcnMajig.addHeaderForNoPatients(response);
    return entityById(publicId).payload();
  }

  /** Search by address. */
  @GetMapping
  @SneakyThrows
  public Location.Bundle searchByAddress(
      @RequestParam(value = "address", required = false) String street,
      @RequestParam(value = "address-city", required = false) String city,
      @RequestParam(value = "address-state", required = false) String state,
      @RequestParam(value = "address-postalcode", required = false) String postalCode,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (street == null && city == null && state == null && postalCode == null) {
      throw new ResourceExceptions.MissingSearchParameters(
          String.format(
              "At least one of %s must be specified",
              List.of("address", "address-city", "address-state", "address-postalcode")));
    }
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .addIgnoreNull("address", street)
            .addIgnoreNull("address-city", city)
            .addIgnoreNull("address-state", state)
            .addIgnoreNull("address-postalcode", postalCode)
            .add("page", page)
            .add("_count", count)
            .build();
    LocationRepository.AddressSpecification spec =
        LocationRepository.AddressSpecification.builder()
            .street(street)
            .city(city)
            .state(state)
            .postalCode(postalCode)
            .build();
    Page<LocationEntity> entitiesPage = repository.findAll(spec, page(page, count));

    if (count == 0) {
      return bundle(parameters, emptyList(), (int) entitiesPage.getTotalElements());
    }
    return bundle(parameters, transform(entitiesPage.get()), (int) entitiesPage.getTotalElements());
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

  /** Search by name. */
  @GetMapping(params = {"name"})
  public Location.Bundle searchByName(
      @RequestParam("name") String name,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder().add("name", name).add("page", page).add("_count", count).build();
    Page<LocationEntity> entitiesPage = repository.findByName(name, page(page, count));
    if (count == 0) {
      return bundle(parameters, emptyList(), (int) entitiesPage.getTotalElements());
    }
    return bundle(parameters, transform(entitiesPage.get()), (int) entitiesPage.getTotalElements());
  }

  private List<Location> transform(Stream<LocationEntity> entities) {
    List<DatamartLocation> datamarts =
        entities.map(LocationEntity::asDatamartLocation).collect(Collectors.toList());
    witnessProtection.registerAndUpdateReferences(
        datamarts, resource -> Stream.of(resource.managingOrganization()));
    return datamarts
        .stream()
        .map(dm -> Stu3LocationTransformer.builder().datamart(dm).build().toFhir())
        .collect(Collectors.toList());
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Location.Bundle bundle) {
    return Stu3Validator.create().validate(bundle);
  }
}
