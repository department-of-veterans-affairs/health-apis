package gov.va.api.health.dataquery.service.controller.condition;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.argonaut.api.resources.Condition.Bundle;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.Dstu2Validator;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
 * Request Mappings for Condition Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-condition.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
@Validated
@RestController
@RequestMapping(
  value = {"/dstu2/Condition"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
public class Dstu2ConditionController {

  private Dstu2Bundler bundler;

  private ConditionRepository repository;

  private WitnessProtection witnessProtection;

  /** Spring constructor. */
  @SuppressWarnings("ParameterHidesMemberVariable")
  public Dstu2ConditionController(
      @Autowired Dstu2Bundler bundler,
      @Autowired ConditionRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private Condition.Bundle bundle(
      MultiValueMap<String, String> parameters, List<Condition> reports, int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("Condition")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        Dstu2Bundler.BundleContext.of(
            linkConfig, reports, Condition.Entry::new, Condition.Bundle::new));
  }

  private Bundle bundle(
      MultiValueMap<String, String> parameters, int count, Page<ConditionEntity> entities) {
    log.info("Search {} found {} results", parameters, entities.getTotalElements());
    if (count == 0) {
      return bundle(parameters, emptyList(), (int) entities.getTotalElements());
    }
    return bundle(
        parameters,
        replaceReferences(
                entities
                    .get()
                    .map(ConditionEntity::asDatamartCondition)
                    .collect(Collectors.toList()))
            .stream()
            .map(this::transform)
            .collect(Collectors.toList()),
        (int) entities.getTotalElements());
  }

  ConditionEntity findById(String publicId) {
    Optional<ConditionEntity> entity = repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new NotFound(publicId));
  }

  private PageRequest page(int page, int count) {
    return PageRequest.of(page - 1, count == 0 ? 1 : count, ConditionEntity.naturalOrder());
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Condition read(@PathVariable("publicId") String publicId) {
    DatamartCondition dm = findById(publicId).asDatamartCondition();
    replaceReferences(List.of(dm));
    return Dstu2ConditionTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Read by id. */
  @GetMapping(
    value = {"/{publicId}"},
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    ConditionEntity entity = findById(publicId);
    IncludesIcnMajig.addHeader(response, entity.icn());
    return entity.payload();
  }

  Collection<DatamartCondition> replaceReferences(Collection<DatamartCondition> resources) {
    witnessProtection.registerAndUpdateReferences(
        resources,
        resource ->
            Stream.of(
                resource.patient(),
                resource.asserter().orElse(null),
                resource.encounter().orElse(null)));
    return resources;
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Condition.Bundle searchById(
      @RequestParam("_id") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchByIdentifier(publicId, page, count);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Condition.Bundle searchByIdentifier(
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("identifier", identifier)
            .add("page", page)
            .add("_count", count)
            .build();
    Condition resource = read(identifier);
    int totalRecords = resource == null ? 0 : 1;
    if (resource == null || page != 1 || count <= 0) {
      return bundle(parameters, emptyList(), totalRecords);
    }
    return bundle(parameters, asList(resource), totalRecords);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public Condition.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build();
    String publicIcn = parameters.getFirst("patient");
    String cdwIcn = witnessProtection.toCdwId(publicIcn);
    int page1 = Parameters.pageOf(parameters);
    int count1 = Parameters.countOf(parameters);
    Page<ConditionEntity> entitiesPage =
        repository.findByIcn(
            cdwIcn,
            PageRequest.of(page1 - 1, count1 == 0 ? 1 : count1, ConditionEntity.naturalOrder()));
    if (Parameters.countOf(parameters) <= 0) {
      return bundle(parameters, emptyList(), (int) entitiesPage.getTotalElements());
    }
    List<DatamartCondition> datamarts =
        entitiesPage.stream().map(e -> e.asDatamartCondition()).collect(Collectors.toList());
    replaceReferences(datamarts);
    List<Condition> fhir =
        datamarts
            .stream()
            .map(dm -> Dstu2ConditionTransformer.builder().datamart(dm).build().toFhir())
            .collect(Collectors.toList());
    return bundle(parameters, fhir, (int) entitiesPage.getTotalElements());
  }

  /** Search by patient and category if available. */
  @GetMapping(params = {"patient", "category"})
  public Condition.Bundle searchByPatientAndCategory(
      @RequestParam("patient") String patient,
      @RequestParam("category") String category,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    String icn = witnessProtection.toCdwId(patient);
    return bundle(
        Parameters.builder()
            .add("patient", patient)
            .add("category", category)
            .add("page", page)
            .add("_count", count)
            .build(),
        count,
        repository.findByIcnAndCategory(icn, category, page(page, count)));
  }

  /** Search by patient and clinical status if available. */
  @GetMapping(params = {"patient", "clinicalstatus"})
  public Condition.Bundle searchByPatientAndClinicalStatus(
      @RequestParam("patient") String patient,
      @RequestParam("clinicalstatus") String clinicalStatus,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    String icn = witnessProtection.toCdwId(patient);
    return bundle(
        Parameters.builder()
            .add("patient", patient)
            .add("clinicalstatus", clinicalStatus)
            .add("page", page)
            .add("_count", count)
            .build(),
        count,
        repository.findByIcnAndClinicalStatusIn(
            icn, Set.of(clinicalStatus.split("\\s*,\\s*")), page(page, count)));
  }

  Condition transform(DatamartCondition dm) {
    return Dstu2ConditionTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Condition.Bundle bundle) {
    return Dstu2Validator.create().validate(bundle);
  }
}
