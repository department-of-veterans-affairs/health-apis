package gov.va.api.health.dataquery.service.controller.practitioner;

import static java.util.Collections.emptyList;

import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.Dstu2Validator;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import java.util.Collection;
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

  private Dstu2Bundler bundler;

  private PractitionerRepository repository;

  private WitnessProtection witnessProtection;

  /** Autowired constructor. */
  public Dstu2PractitionerController(
      @Autowired Dstu2Bundler bundler,
      @Autowired PractitionerRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

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
    Optional<PractitionerEntity> entity = repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Practitioner read(@PathVariable("publicId") String publicId) {
    DatamartPractitioner practitioner = findById(publicId).asDatamartPractitioner();
    replaceReferences(List.of(practitioner));
    return transform(practitioner);
  }

  /** Read by id. */
  @GetMapping(
    value = {"/{publicId}"},
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    PractitionerEntity entity = findById(publicId);
    IncludesIcnMajig.addHeaderForNoPatients(response);
    return entity.payload();
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

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Practitioner.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    Practitioner resource = read(id);
    return bundle(
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build(),
        resource == null || count == 0 ? emptyList() : List.of(resource),
        resource == null ? 0 : 1);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Practitioner.Bundle searchByIdentifier(
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(id, page, count);
  }

  Practitioner transform(DatamartPractitioner dm) {
    return Dstu2PractitionerTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Practitioner.Bundle bundle) {
    return Dstu2Validator.create().validate(bundle);
  }
}
