package gov.va.api.health.dataquery.service.controller.organization;

import static java.util.Collections.emptyList;

import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.Dstu2Validator;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.dstu2.api.resources.Organization;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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
 * Request Mappings for DSTU Organization, see https://www.hl7.org/fhir/DSTU2/appointment.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
  value = "/dstu2/Organization",
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
public class Dstu2OrganizationController {

  private Dstu2Bundler bundler;

  private OrganizationRepository repository;

  private WitnessProtection witnessProtection;

  /** Spring constructor. */
  public Dstu2OrganizationController(
      @Autowired Dstu2Bundler bundler,
      @Autowired OrganizationRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  Organization.Bundle bundle(
      MultiValueMap<String, String> parameters, List<Organization> reports, int totalRecords) {
    LinkConfig linkConfig =
        LinkConfig.builder()
            .path("Organization")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        Dstu2Bundler.BundleContext.of(
            linkConfig,
            reports,
            Function.identity(),
            Organization.Entry::new,
            Organization.Bundle::new));
  }

  OrganizationEntity findById(String publicId) {
    Optional<OrganizationEntity> entity = repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  Organization read(@PathVariable("publicId") String publicId) {
    DatamartOrganization organization = findById(publicId).asDatamartOrganization();
    witnessProtection.registerAndUpdateReferences(
        List.of(organization), resource -> Stream.empty());
    return transform(organization);
  }

  /** Read by id. */
  @GetMapping(
    value = {"/{publicId}"},
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    IncludesIcnMajig.addHeaderForNoPatients(response);
    return findById(publicId).payload();
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  Organization.Bundle searchById(
      @RequestParam("_id") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    Organization resource = read(publicId);
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
  public Organization.Bundle searchByIdentifier(
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(id, page, count);
  }

  Organization transform(DatamartOrganization dm) {
    return Dstu2OrganizationTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Organization.Bundle bundle) {
    return Dstu2Validator.create().validate(bundle);
  }
}
