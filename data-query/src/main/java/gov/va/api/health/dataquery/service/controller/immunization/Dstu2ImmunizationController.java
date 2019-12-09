package gov.va.api.health.dataquery.service.controller.immunization;

import static java.util.Collections.emptyList;

import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.argonaut.api.resources.Immunization.Bundle;
import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
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
 * Request Mappings for Immunization Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-Immunization.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
  value = {"/dstu2/Immunization"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
@Slf4j
public class Dstu2ImmunizationController {

  private Dstu2Bundler bundler;

  private ImmunizationRepository repository;

  private WitnessProtection witnessProtection;

  /** Spring constructor. */
  @SuppressWarnings("ParameterHidesMemberVariable")
  public Dstu2ImmunizationController(
      @Autowired Dstu2Bundler bundler,
      @Autowired ImmunizationRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private Bundle bundle(
      MultiValueMap<String, String> parameters, List<Immunization> reports, int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("Immunization")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        Dstu2Bundler.BundleContext.of(
            linkConfig,
            reports,
            Immunization.Entry::new,
            Immunization.Bundle::new));
  }

  private Bundle bundle(
      MultiValueMap<String, String> parameters, int count, Page<ImmunizationEntity> entities) {
    log.info("Search {} found {} results", parameters, entities.getTotalElements());
    if (count == 0) {
      return bundle(parameters, emptyList(), (int) entities.getTotalElements());
    }
    return bundle(
        parameters,
        replaceReferences(
                entities
                    .get()
                    .map(ImmunizationEntity::asDatamartImmunization)
                    .collect(Collectors.toList()))
            .stream()
            .map(dm -> Dstu2ImmunizationTransformer.builder().datamart(dm).build().toFhir())
            .collect(Collectors.toList()),
        (int) entities.getTotalElements());
  }

  ImmunizationEntity findById(String publicId) {
    String cdwId = witnessProtection.toCdwId(publicId);
    Optional<ImmunizationEntity> entity = repository.findById(cdwId);
    return entity.orElseThrow(() -> new NotFound(publicId));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Immunization read(@PathVariable("publicId") String publicId) {
    DatamartImmunization immunization = findById(publicId).asDatamartImmunization();
    replaceReferences(List.of(immunization));
    return Dstu2ImmunizationTransformer.builder().datamart(immunization).build().toFhir();
  }

  /** Read by id. */
  @GetMapping(
    value = {"/{publicId}"},
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    ImmunizationEntity entity = findById(publicId);
    AbstractIncludesIcnMajig.addHeader(response, entity.icn());
    return entity.payload();
  }

  Collection<DatamartImmunization> replaceReferences(Collection<DatamartImmunization> resources) {
    /*
     * Reaction is not a reference to another resource and is intentionally excluded from
     * registration.
     */
    witnessProtection.registerAndUpdateReferences(
        resources,
        resource ->
            Stream.of(
                resource.patient(),
                resource.performer().orElse(null),
                resource.requester().orElse(null),
                resource.encounter().orElse(null),
                resource.location().orElse(null)));
    return resources;
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Immunization.Bundle searchById(
      @RequestParam("_id") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    Immunization resource = read(publicId);
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
  public Immunization.Bundle searchByIdentifier(
      @RequestParam("identifier") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(publicId, page, count);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public Immunization.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    String icn = witnessProtection.toCdwId(patient);
    log.info("Looking for {} ({})", patient, icn);
    return bundle(
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build(),
        count,
        repository.findByIcn(
            icn,
            PageRequest.of(page - 1, count == 0 ? 1 : count, ImmunizationEntity.naturalOrder())));
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Immunization.Bundle bundle) {
    return Validator.create().validate(bundle);
  }
}
