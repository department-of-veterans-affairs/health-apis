package gov.va.api.health.dataquery.service.controller.organization;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.hasPayload;
import static java.util.Collections.emptyList;

import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.Dstu2Validator;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dataquery.service.mranderson.client.Query.Profile;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.dstu2.api.resources.Organization;
import gov.va.dvp.cdw.xsd.model.CdwOrganization100Root;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
 * Request Mappings for DSTU Organization, see https://www.hl7.org/fhir/DSTU2/appointment.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
  value = {"/dstu2/Organization"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
public class Dstu2OrganizationController {
  private final Datamart datamart = new Datamart();
  private Transformer transformer;

  private MrAndersonClient mrAndersonClient;
  private Dstu2Bundler bundler;
  private OrganizationRepository repository;
  private WitnessProtection witnessProtection;
  private boolean defaultToDatamart;

  /** Spring constructor. */
  public Dstu2OrganizationController(
      @Value("${datamart.organization}") boolean defaultToDatamart,
      @Autowired Transformer transformer,
      @Autowired MrAndersonClient mrAndersonClient,
      @Autowired Dstu2Bundler bundler,
      @Autowired OrganizationRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.defaultToDatamart = defaultToDatamart;
    this.transformer = transformer;
    this.mrAndersonClient = mrAndersonClient;
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private Organization.Bundle bundle(
      MultiValueMap<String, String> parameters, int page, int count) {
    CdwOrganization100Root root = search(parameters);
    LinkConfig linkConfig =
        LinkConfig.builder()
            .path("Organization")
            .queryParams(parameters)
            .page(page)
            .recordsPerPage(count)
            .totalRecords(root.getRecordCount().intValue())
            .build();
    return bundler.bundle(
        BundleContext.of(
            linkConfig,
            root.getOrganizations() == null
                ? Collections.emptyList()
                : root.getOrganizations().getOrganization(),
            transformer,
            Organization.Entry::new,
            Organization.Bundle::new));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Organization read(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @PathVariable("publicId") String publicId) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.read(publicId);
    }
    return transformer.apply(
        firstPayloadItem(
            hasPayload(search(Parameters.forIdentity(publicId)).getOrganizations())
                .getOrganization()));
  }

  /** Read by id. */
  @GetMapping(
    value = {"/{publicId}"},
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    AbstractIncludesIcnMajig.addHeaderForNoPatients(response);
    return datamart.readRaw(publicId).payload();
  }

  /**
   * The XML should remain the same, but the version of the resource needs to be incremented for
   * SQL52.
   */
  private CdwOrganization100Root search(MultiValueMap<String, String> params) {
    Query<CdwOrganization100Root> query =
        Query.forType(CdwOrganization100Root.class)
            .profile(Profile.DSTU2)
            .resource("Organization")
            .version("1.02")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Organization.Bundle searchById(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.searchById(id, page, count);
    }
    return bundle(
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Organization.Bundle searchByIdentifier(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(datamartHeader, id, page, count);
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Organization.Bundle bundle) {
    return Dstu2Validator.create().validate(bundle);
  }

  public interface Transformer
      extends Function<CdwOrganization100Root.CdwOrganizations.CdwOrganization, Organization> {}

  /**
   * This class is being used to help organize the code such that all the datamart logic is
   * contained together. In the future when Mr. Anderson support is dropped, this class can be
   * eliminated.
   */
  private class Datamart {
    Organization.Bundle bundle(
        MultiValueMap<String, String> parameters, List<Organization> reports, int totalRecords) {
      PageLinks.LinkConfig linkConfig =
          PageLinks.LinkConfig.builder()
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
      Optional<OrganizationEntity> entity =
          repository.findById(witnessProtection.toCdwId(publicId));
      return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
    }

    boolean isDatamartRequest(String datamartHeader) {
      if (StringUtils.isBlank(datamartHeader)) {
        return defaultToDatamart;
      }
      return BooleanUtils.isTrue(BooleanUtils.toBooleanObject(datamartHeader));
    }

    Organization read(String publicId) {
      DatamartOrganization organization = findById(publicId).asDatamartOrganization();
      replaceReferences(List.of(organization));
      return transform(organization);
    }

    OrganizationEntity readRaw(String publicId) {
      return findById(publicId);
    }

    Collection<DatamartOrganization> replaceReferences(Collection<DatamartOrganization> resources) {
      witnessProtection.registerAndUpdateReferences(
          resources, resource -> Stream.of(resource.partOf().get()));
      return resources;
    }

    Organization.Bundle searchById(String publicId, int page, int count) {
      Organization resource = read(publicId);
      return bundle(
          Parameters.builder()
              .add("_id", publicId)
              .add("page", page)
              .add("_count", count)
              .build(),
          resource == null || count == 0 ? emptyList() : List.of(resource),
          resource == null ? 0 : 1);
    }

    Organization transform(DatamartOrganization dm) {
      return Dstu2OrganizationTransformer.builder().datamart(dm).build().toFhir();
    }
  }
}
