package gov.va.api.health.dataquery.service.controller.condition;

import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;
import static java.util.Collections.emptyList;

import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.argonaut.api.resources.Condition.Bundle;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.dvp.cdw.xsd.model.CdwCondition103Root;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
 * Request Mappings for Condition Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-condition.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
@Validated
@RestController
@RequestMapping(
  value = {"Condition", "/api/Condition"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
public class ConditionController {
  private final Datamart datamart = new Datamart();

  private Transformer transformer;

  private MrAndersonClient mrAndersonClient;

  private Bundler bundler;

  private ConditionRepository repository;

  private WitnessProtection witnessProtection;

  private boolean defaultToDatamart;

  /** Spring constructor. */
  @SuppressWarnings("ParameterHidesMemberVariable")
  public ConditionController(
      @Value("${datamart.condition}") boolean defaultToDatamart,
      @Autowired Transformer transformer,
      @Autowired MrAndersonClient mrAndersonClient,
      @Autowired Bundler bundler,
      @Autowired ConditionRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.defaultToDatamart = defaultToDatamart;
    this.transformer = transformer;
    this.mrAndersonClient = mrAndersonClient;
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private Condition.Bundle bundle(MultiValueMap<String, String> parameters, int page, int count) {
    CdwCondition103Root root = search(parameters);
    LinkConfig linkConfig =
        LinkConfig.builder()
            .path("Condition")
            .queryParams(parameters)
            .page(page)
            .recordsPerPage(count)
            .totalRecords(root.getRecordCount().intValue())
            .build();
    return bundler.bundle(
        BundleContext.of(
            linkConfig,
            root.getConditions() == null
                ? Collections.emptyList()
                : root.getConditions().getCondition(),
            transformer,
            Condition.Entry::new,
            Condition.Bundle::new));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Condition read(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @PathVariable("publicId") String publicId) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.read(publicId);
    }
    return transformer.apply(
        firstPayloadItem(
            hasPayload(search(Parameters.forIdentity(publicId)).getConditions()).getCondition()));
  }

  /** Read by id. */
  @GetMapping(
    value = {"/{publicId}"},
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId) {
    return datamart.readRaw(publicId);
  }

  private CdwCondition103Root search(MultiValueMap<String, String> params) {
    Query<CdwCondition103Root> query =
        Query.forType(CdwCondition103Root.class)
            .profile(Query.Profile.ARGONAUT)
            .resource("Condition")
            .version("1.03")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Condition.Bundle searchById(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("_id") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.searchById(publicId, page, count);
    }
    return bundle(
        Parameters.builder()
            .add("identifier", publicId)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Condition.Bundle searchByIdentifier(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("identifier") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(datamartHeader, publicId, page, count);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public Condition.Bundle searchByPatient(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.searchByPatient(patient, page, count);
    }
    return bundle(
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Search by patient and category if available. */
  @GetMapping(params = {"patient", "category"})
  public Condition.Bundle searchByPatientAndCategory(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("patient") String patient,
      @RequestParam("category") String category,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.searchByPatientAndCategory(patient, category, page, count);
    }
    return bundle(
        Parameters.builder()
            .add("patient", patient)
            .add("category", category)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Search by patient and clinical status if available. */
  @GetMapping(params = {"patient", "clinicalstatus"})
  public Condition.Bundle searchByPatientAndClinicalStatus(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("patient") String patient,
      @RequestParam("clinicalstatus") String clinicalStatus,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.searchByPatientAndClinicalStatus(patient, clinicalStatus, page, count);
    }
    return bundle(
        Parameters.builder()
            .add("patient", patient)
            .add("clinicalstatus", clinicalStatus)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Condition.Bundle bundle) {
    return Validator.create().validate(bundle);
  }

  public interface Transformer
      extends Function<CdwCondition103Root.CdwConditions.CdwCondition, Condition> {}

  /**
   * This class is being used to help organize the code such that all the datamart logic is
   * contained together. In the future when Mr. Anderson support is dropped, this class can be
   * eliminated.
   */
  private class Datamart {
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
          Bundler.BundleContext.of(
              linkConfig,
              reports,
              Function.identity(),
              Condition.Entry::new,
              Condition.Bundle::new));
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

    boolean isDatamartRequest(String datamartHeader) {
      if (StringUtils.isBlank(datamartHeader)) {
        return defaultToDatamart;
      }
      return BooleanUtils.isTrue(BooleanUtils.toBooleanObject(datamartHeader));
    }

    private PageRequest page(int page, int count) {
      return PageRequest.of(page - 1, count == 0 ? 1 : count, ConditionEntity.naturalOrder());
    }

    Condition read(String publicId) {
      DatamartCondition condition = findById(publicId).asDatamartCondition();
      replaceReferences(List.of(condition));
      return transform(condition);
    }

    String readRaw(String publicId) {
      return findById(publicId).payload();
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

    Bundle searchById(String publicId, int page, int count) {
      Condition resource = read(publicId);
      return bundle(
          Parameters.builder()
              .add("identifier", publicId)
              .add("page", page)
              .add("_count", count)
              .build(),
          resource == null || count == 0 ? emptyList() : List.of(resource),
          resource == null ? 0 : 1);
    }

    Bundle searchByPatient(String patient, int page, int count) {
      String icn = witnessProtection.toCdwId(patient);
      log.info("Looking for {} ({})", patient, icn);
      return bundle(
          Parameters.builder()
              .add("patient", patient)
              .add("page", page)
              .add("_count", count)
              .build(),
          count,
          repository.findByIcn(icn, page(page, count)));
    }

    Bundle searchByPatientAndCategory(String patient, String category, int page, int count) {
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

    Bundle searchByPatientAndClinicalStatus(
        String patient, String clinicalStatusCsv, int page, int count) {
      String icn = witnessProtection.toCdwId(patient);
      return bundle(
          Parameters.builder()
              .add("patient", patient)
              .add("clinicalstatus", clinicalStatusCsv)
              .add("page", page)
              .add("_count", count)
              .build(),
          count,
          repository.findByIcnAndClinicalStatusIn(
              icn, Set.of(clinicalStatusCsv.split("\\s*,\\s*")), page(page, count)));
    }

    Condition transform(DatamartCondition dm) {
      return DatamartConditionTransformer.builder().datamart(dm).build().toFhir();
    }
  }
}
