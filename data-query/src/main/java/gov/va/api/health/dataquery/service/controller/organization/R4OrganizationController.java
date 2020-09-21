package gov.va.api.health.dataquery.service.controller.organization;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static java.util.Collections.emptyList;

import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.r4.api.resources.Organization;
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
 * Request Mappings for Organization Profile.
 *
 * @implSpec https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-organization.html
 */
@Validated
@RestController
@RequestMapping(
    value = {"/r4/Organization"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class R4OrganizationController {
  private R4Bundler bundler;
  private OrganizationRepository repository;
  private WitnessProtection witnessProtection;

  private static PageRequest page(int page, int count) {
    return PageRequest.of(page - 1, count == 0 ? 1 : count, OrganizationEntity.naturalOrder());
  }

  private Organization.Bundle bundle(
      MultiValueMap<String, String> parameters, List<Organization> records, int totalRecords) {
    return bundler.bundle(
        PageLinks.LinkConfig.builder()
            .path("Organization")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build(),
        records,
        Organization.Entry::new,
        Organization.Bundle::new);
  }

  private Organization.Bundle bundle(
      MultiValueMap<String, String> parameters, int count, Page<OrganizationEntity> entitiesPage) {
    if (count == 0) {
      return bundle(parameters, emptyList(), (int) entitiesPage.getTotalElements());
    }
    List<DatamartOrganization> datamarts =
        entitiesPage.stream()
            .map(OrganizationEntity::asDatamartOrganization)
            .collect(Collectors.toList());
    witnessProtection.registerAndUpdateReferences(datamarts, resource -> Stream.empty());
    List<Organization> records =
        datamarts.stream()
            .map(dm -> R4OrganizationTransformer.builder().datamart(dm).build().toFhir())
            .collect(Collectors.toList());
    return bundle(parameters, records, (int) entitiesPage.getTotalElements());
  }

  private OrganizationEntity findById(String publicId) {
    return repository
        .findById(witnessProtection.toCdwId(publicId))
        .orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  /** Get resource by id. */
  @GetMapping(value = "/{publicId}")
  public Organization read(@PathVariable("publicId") String publicId) {
    DatamartOrganization dm = findById(publicId).asDatamartOrganization();
    witnessProtection.registerAndUpdateReferences(List.of(dm), resource -> Stream.empty());
    return R4OrganizationTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Get raw datamart resource by id. */
  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    IncludesIcnMajig.addHeaderForNoPatients(response);
    return findById(publicId).payload();
  }

  /** Search for resource by address. */
  @GetMapping
  public Organization.Bundle searchByAddress(
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
    OrganizationRepository.AddressSpecification spec =
        OrganizationRepository.AddressSpecification.builder()
            .address(address)
            .city(city)
            .state(state)
            .postalCode(postalCode)
            .build();
    Page<OrganizationEntity> entitiesPage = repository.findAll(spec, page(page, count));
    return bundle(parameters, count, entitiesPage);
  }

  /** Search for resource by _id. */
  @GetMapping(params = {"_id"})
  public Organization.Bundle searchById(
      @RequestParam("_id") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchByIdentifier(publicId, page, count);
  }

  /** Search for resource by identifier. */
  @GetMapping(params = {"identifier"})
  public Organization.Bundle searchByIdentifier(
      @RequestParam("identifier") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("identifier", publicId)
            .add("page", page)
            .add("_count", count)
            .build();
    Organization resource = read(publicId);
    List<Organization> records = resource == null ? emptyList() : List.of(resource);
    if (count == 0) {
      return bundle(parameters, emptyList(), records.size());
    }
    return bundle(parameters, records, records.size());
  }

  /** Search for resource by name. */
  @GetMapping(params = {"name"})
  public Organization.Bundle searchByName(
      @RequestParam("name") String name,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder().add("name", name).add("page", page).add("_count", count).build();
    Page<OrganizationEntity> entitiesPage = repository.findByName(name, page(page, count));
    return bundle(parameters, count, entitiesPage);
  }
}
