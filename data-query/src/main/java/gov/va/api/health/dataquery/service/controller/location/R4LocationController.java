package gov.va.api.health.dataquery.service.controller.location;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static java.util.Collections.emptyList;

import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.r4.api.resources.Location;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Location Profile.
 *
 * @implSpec https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-location.html
 */
@Validated
@RestController
@RequestMapping(
    value = {"/r4/Location"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class R4LocationController {
  private R4Bundler bundler;

  private LocationRepository repository;

  private WitnessProtection witnessProtection;

  private static PageRequest page(int page, int count) {
    return PageRequest.of(page - 1, count == 0 ? 1 : count, LocationEntity.naturalOrder());
  }

  private Location.Bundle bundle(
      MultiValueMap<String, String> parameters, List<Location> records, int totalRecords) {
    return bundler.bundle(
        PageLinks.LinkConfig.builder()
            .path("Location")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build(),
        records,
        Location.Entry::new,
        Location.Bundle::new);
  }

  private Location.Bundle bundle(
      MultiValueMap<String, String> parameters, int count, Page<LocationEntity> entitiesPage) {
    if (count == 0) {
      return bundle(parameters, emptyList(), (int) entitiesPage.getTotalElements());
    }
    List<DatamartLocation> datamarts =
        entitiesPage.stream().map(LocationEntity::asDatamartLocation).collect(Collectors.toList());
    replaceReferences(datamarts);
    List<Location> records =
        datamarts.stream()
            .map(dm -> R4LocationTransformer.builder().datamart(dm).build().toFhir())
            .collect(Collectors.toList());
    return bundle(parameters, records, (int) entitiesPage.getTotalElements());
  }

  private LocationEntity entityById(String publicId) {
    String cdwId = witnessProtection.toCdwId(publicId);
    return repository.findById(cdwId).orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  /** Get resource by id. */
  @GetMapping(value = "/{publicId}")
  public Location read(@PathVariable("publicId") String publicId) {
    DatamartLocation dm = entityById(publicId).asDatamartLocation();
    replaceReferences(List.of(dm));
    return R4LocationTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Get raw datamart resource by id. */
  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    IncludesIcnMajig.addHeaderForNoPatients(response);
    return entityById(publicId).payload();
  }

  private void replaceReferences(Collection<DatamartLocation> resources) {
    witnessProtection.registerAndUpdateReferences(
        resources, resource -> Stream.of(resource.managingOrganization()));
  }

  /** Search for resource by address. */
  @GetMapping
  public Location.Bundle searchByAddress(
      @RequestParam(value = "address", required = false) String address,
      @RequestParam(value = "address-city", required = false) String city,
      @RequestParam(value = "address-state", required = false) String state,
      @RequestParam(value = "address-postalcode", required = false) String postalCode,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (allBlank(address, city, state, postalCode)) {
      throw new ResourceExceptions.MissingSearchParameters(
          String.format(
              "At least one of %s must be specified",
              List.of("address", "address-city", "address-state", "address-postalcode")));
    }
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .addIgnoreNull("address", address)
            .addIgnoreNull("address-city", city)
            .addIgnoreNull("address-state", state)
            .addIgnoreNull("address-postalcode", postalCode)
            .add("page", page)
            .add("_count", count)
            .build();
    LocationRepository.AddressSpecification spec =
        LocationRepository.AddressSpecification.builder()
            .address(address)
            .city(city)
            .state(state)
            .postalCode(postalCode)
            .build();
    Page<LocationEntity> entitiesPage = repository.findAll(spec, page(page, count));
    return bundle(parameters, count, entitiesPage);
  }

  /** Search for resource by _id. */
  @GetMapping(params = {"_id"})
  public Location.Bundle searchById(
      @RequestParam("_id") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchByIdentifier(publicId, page, count);
  }

  /** Search for resource by identifier. */
  @GetMapping(params = {"identifier"})
  public Location.Bundle searchByIdentifier(
      @RequestParam("identifier") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("identifier", publicId)
            .add("page", page)
            .add("_count", count)
            .build();
    Location resource = read(publicId);
    List<Location> records = resource == null ? emptyList() : List.of(resource);
    if (count == 0) {
      return bundle(parameters, emptyList(), records.size());
    }
    return bundle(parameters, records, records.size());
  }

  /** Search for resource by name. */
  @GetMapping(params = {"name"})
  public Location.Bundle searchByName(
      @RequestParam("name") String name,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder().add("name", name).add("page", page).add("_count", count).build();
    Page<LocationEntity> entitiesPage = repository.findByName(name, page(page, count));
    return bundle(parameters, count, entitiesPage);
  }
}
