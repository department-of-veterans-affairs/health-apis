package gov.va.api.health.dataquery.service.controller.condition;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import com.google.common.base.Splitter;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.uscorer4.api.resources.Condition;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Condition Profile, see
 * https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-condition.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
@Validated
@RestController
@RequestMapping(
    value = {"/r4/Condition"},
    produces = {"application/json", "application/fhir+json", "application/json+fhir"})
public class R4ConditionController {
  R4Bundler bundler;

  private ConditionRepository repository;

  private WitnessProtection witnessProtection;

  /** R4 Condition Constructor. */
  public R4ConditionController(
      @Autowired R4Bundler bundler,
      @Autowired ConditionRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  /**
   * Splits the R4 category received into its code and transforms it from an R4 code to a datamart
   * code that can be searched in the database.
   *
   * <p>Datamart: problem | diagnosis
   *
   * <p>R4: problem-list-item | encounter-diagnosis | health-concern
   *
   * @param category an r4 category search parameter of the form: {[system]}|[code]
   * @return a string representation of the code that is understood by CDW
   * @throws gov.va.api.health.dataquery.service.controller.ResourceExceptions.BadSearchParameter
   *     when r4 category can not be translated to a datamart category
   */
  private String asDatamartCategory(String category) {
    List<String> categoryCode = Splitter.onPattern("\\s*\\|\\s*").splitToList(category);
    switch (categoryCode.get(1)) {
      case "problem-list-item":
        return DatamartCondition.Category.problem.toString();
      case "encounter-diagnosis":
        return DatamartCondition.Category.diagnosis.toString();
      default:
        throw new ResourceExceptions.BadSearchParameter("Invalid Category: " + category);
    }
  }

  private Condition.Bundle bundle(
      MultiValueMap<String, String> parameters, List<Condition> reports, int totalRecords) {
    log.info("Search {} found {} results", parameters, totalRecords);
    return bundler.bundle(
        PageLinks.LinkConfig.builder()
            .path("Condition")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build(),
        reports,
        Condition.Entry::new,
        Condition.Bundle::new);
  }

  private Condition.Bundle bundle(
      MultiValueMap<String, String> parameters, int count, Page<ConditionEntity> entities) {
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

  private ConditionEntity findEntityById(String publicId) {
    Optional<ConditionEntity> entity = repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  private PageRequest page(int page, int count) {
    return PageRequest.of(page - 1, count == 0 ? 1 : count, ConditionEntity.naturalOrder());
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Condition read(@PathVariable("publicId") String publicId) {
    DatamartCondition dm = findEntityById(publicId).asDatamartCondition();
    replaceReferences(List.of(dm));
    return R4ConditionTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Get raw DatamartCondition by id. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    ConditionEntity entity = findEntityById(publicId);
    IncludesIcnMajig.addHeader(response, entity.icn());
    return entity.payload();
  }

  Collection<DatamartCondition> replaceReferences(Collection<DatamartCondition> resources) {
    witnessProtection.registerAndUpdateReferences(
        resources, resource -> Stream.of(resource.patient(), resource.asserter().orElse(null)));
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

  /** Search Condition by Patient. */
  @GetMapping(params = {"patient"})
  public Condition.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build();
    String patientIcn = parameters.getFirst("patient");
    int page1 = Parameters.pageOf(parameters);
    int count1 = Parameters.countOf(parameters);
    Page<ConditionEntity> entitiesPage = repository.findByIcn(patientIcn, page(page1, count1));
    if (Parameters.countOf(parameters) <= 0) {
      return bundle(parameters, emptyList(), (int) entitiesPage.getTotalElements());
    }
    List<DatamartCondition> datamarts =
        entitiesPage.stream().map(e -> e.asDatamartCondition()).collect(Collectors.toList());
    replaceReferences(datamarts);
    List<Condition> fhir =
        datamarts.stream()
            .map(dm -> R4ConditionTransformer.builder().datamart(dm).build().toFhir())
            .collect(Collectors.toList());
    return bundle(parameters, fhir, (int) entitiesPage.getTotalElements());
  }

  /**
   * Search Condition by patient and category.
   *
   * <p>GET [base]/Condition?patient=[reference]&category={[system]}|[code]
   */
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
        repository.findByIcnAndCategory(icn, asDatamartCategory(category), page(page, count)));
  }

  /** Search Condition by patient and clinical status. */
  @GetMapping(params = {"patient", "clinical-status"})
  public Condition.Bundle searchByPatientAndClinicalStatus(
      @RequestParam("patient") String patient,
      @RequestParam("clinical-status") String clinicalStatus,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    String icn = witnessProtection.toCdwId(patient);
    return bundle(
        Parameters.builder()
            .add("patient", patient)
            .add("clinical-status", clinicalStatus)
            .add("page", page)
            .add("_count", count)
            .build(),
        count,
        repository.findByIcnAndClinicalStatusIn(
            icn, Set.of(clinicalStatus.split("\\s*,\\s*")), page(page, count)));
  }

  Condition transform(DatamartCondition dm) {
    return R4ConditionTransformer.builder().datamart(dm).build().toFhir();
  }
}
