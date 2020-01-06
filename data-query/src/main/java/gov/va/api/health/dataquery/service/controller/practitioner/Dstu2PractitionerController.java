package gov.va.api.health.dataquery.service.controller.practitioner;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.hasPayload;
import static java.util.Collections.emptyList;

import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.Dstu2Validator;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dataquery.service.mranderson.client.Query.Profile;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root;
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
 * Request Mappings for Practitioner Profile, see http://hl7.org/fhir/DSTU2/practitioner.html for
 * implementation details.
 */
@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
  value = {"/dstu2/Practitioner"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
public class Dstu2PractitionerController {

  private final Datamart datamart = new Datamart();

  private Transformer transformer;

  private MrAndersonClient mrAndersonClient;

  private Dstu2Bundler bundler;

  private PractitionerRepository repository;

  private WitnessProtection witnessProtection;

  private boolean defaultToDatamart;

  /** Autowired constructor. */
  public Dstu2PractitionerController(
      @Value("${datamart.practitioner}") boolean defaultToDatamart,
      @Autowired Transformer transformer,
      @Autowired MrAndersonClient mrAndersonClient,
      @Autowired Dstu2Bundler bundler,
      @Autowired PractitionerRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.defaultToDatamart = defaultToDatamart;
    this.transformer = transformer;
    this.mrAndersonClient = mrAndersonClient;
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private Practitioner.Bundle bundle(
      MultiValueMap<String, String> parameters, int page, int count) {
    CdwPractitioner100Root root = search(parameters);
    LinkConfig linkConfig =
        LinkConfig.builder()
            .path("Practitioner")
            .queryParams(parameters)
            .page(page)
            .recordsPerPage(count)
            .totalRecords(root.getRecordCount().intValue())
            .build();
    return bundler.bundle(
        BundleContext.of(
            linkConfig,
            root.getPractitioners() == null
                ? Collections.emptyList()
                : root.getPractitioners().getPractitioner(),
            transformer,
            Practitioner.Entry::new,
            Practitioner.Bundle::new));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Practitioner read(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @PathVariable("publicId") String publicId) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.read(publicId);
    }
    return transformer.apply(
        firstPayloadItem(
            hasPayload(search(Parameters.forIdentity(publicId)).getPractitioners())
                .getPractitioner()));
  }

  /** Read by id. */
  @GetMapping(
    value = {"/{publicId}"},
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    PractitionerEntity entity = datamart.readRaw(publicId);
    IncludesIcnMajig.addHeaderForNoPatients(response);
    return entity.payload();
  }

  /**
   * The XML should remain the same, but the version of the resource needs to be incremented for
   * SQL52.
   */
  private CdwPractitioner100Root search(MultiValueMap<String, String> params) {
    Query<CdwPractitioner100Root> query =
        Query.forType(CdwPractitioner100Root.class)
            .profile(Profile.DSTU2)
            .resource("Practitioner")
            .version("1.02")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Practitioner.Bundle searchById(
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
  public Practitioner.Bundle searchByIdentifier(
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
  public OperationOutcome validate(@RequestBody Practitioner.Bundle bundle) {
    return Dstu2Validator.create().validate(bundle);
  }

  public interface Transformer
      extends Function<CdwPractitioner100Root.CdwPractitioners.CdwPractitioner, Practitioner> {}

  /**
   * This class is being used to help organize the code such that all the datamart logic is
   * contained together. In the future when Mr. Anderson support is dropped, this class can be
   * eliminated.
   */
  private class Datamart {

    Practitioner.Bundle bundle(
        MultiValueMap<String, String> parameters, List<Practitioner> reports, int totalRecords) {
      PageLinks.LinkConfig linkConfig =
          PageLinks.LinkConfig.builder()
              .path("Practitioner")
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
              Practitioner.Entry::new,
              Practitioner.Bundle::new));
    }

    PractitionerEntity findById(String publicId) {
      Optional<PractitionerEntity> entity =
          repository.findById(witnessProtection.toCdwId(publicId));
      return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
    }

    boolean isDatamartRequest(String datamartHeader) {
      if (StringUtils.isBlank(datamartHeader)) {
        return defaultToDatamart;
      }
      return BooleanUtils.isTrue(BooleanUtils.toBooleanObject(datamartHeader));
    }

    Practitioner read(String publicId) {
      DatamartPractitioner practitioner = findById(publicId).asDatamartPractitioner();
      replaceReferences(List.of(practitioner));
      return transform(practitioner);
    }

    PractitionerEntity readRaw(String publicId) {
      return findById(publicId);
    }

    private Collection<DatamartPractitioner> replaceReferences(
        Collection<DatamartPractitioner> resources) {
      witnessProtection.registerAndUpdateReferences(
          resources,
          resource ->
              Stream.concat(
                  Stream.of(resource.practitionerRole().get().managingOrganization().orElse(null)),
                  resource.practitionerRole().get().location().stream()));
      return resources;
    }

    Practitioner.Bundle searchById(String publicId, int page, int count) {
      Practitioner resource = read(publicId);
      return bundle(
          Parameters.builder()
              .add("identifier", publicId)
              .add("page", page)
              .add("_count", count)
              .build(),
          resource == null || count == 0 ? emptyList() : List.of(resource),
          resource == null ? 0 : 1);
    }

    Practitioner transform(DatamartPractitioner dm) {
      return Dstu2PractitionerTransformer.builder().datamart(dm).build().toFhir();
    }
  }
}
