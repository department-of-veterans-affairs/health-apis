package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.dvp.cdw.xsd.model.CdwAllergyIntolerance105Root;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Min;
import org.apache.commons.lang3.BooleanUtils;
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
 * Request Mappings for Allergy Intolerance Profile, see
 * http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-allergyintolerance.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
  value = {"AllergyIntolerance", "/api/AllergyIntolerance"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
public class AllergyIntoleranceController {
  private final Datamart datamart = new Datamart();

  private Transformer transformer;

  private MrAndersonClient mrAndersonClient;

  private Bundler bundler;

  private WitnessProtection witnessProtection;

  private EntityManager entityManager;

  private boolean defaultToDatamart;

  /** Autowired constructor. */
  public AllergyIntoleranceController(
      @Value("${datamart.allergy-intolerance}") boolean defaultToDatamart,
      @Autowired Transformer transformer,
      @Autowired MrAndersonClient mrAndersonClient,
      @Autowired Bundler bundler,
      @Autowired EntityManager entityManager,
      @Autowired WitnessProtection witnessProtection) {
    this.defaultToDatamart = defaultToDatamart;
    this.transformer = transformer;
    this.mrAndersonClient = mrAndersonClient;
    this.bundler = bundler;
    this.entityManager = entityManager;
    this.witnessProtection = witnessProtection;
  }

  private AllergyIntolerance.Bundle bundle(
      MultiValueMap<String, String> parameters,
      List<AllergyIntolerance> records,
      int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("AllergyIntolerance")
            .queryParams(parameters)
            .page(Integer.parseInt(parameters.getOrDefault("page", asList("1")).get(0)))
            .recordsPerPage(
                Integer.parseInt(parameters.getOrDefault("_count", asList("15")).get(0)))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        Bundler.BundleContext.of(
            linkConfig,
            records,
            Function.identity(),
            AllergyIntolerance.Entry::new,
            AllergyIntolerance.Bundle::new));
  }

  private AllergyIntolerance.Bundle mrAndersonBundle(MultiValueMap<String, String> parameters) {
    CdwAllergyIntolerance105Root root = mrAndersonSearch(parameters);
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("AllergyIntolerance")
            .queryParams(parameters)
            .page(Integer.parseInt(parameters.getOrDefault("page", asList("1")).get(0)))
            .recordsPerPage(
                Integer.parseInt(parameters.getOrDefault("_count", asList("15")).get(0)))
            .totalRecords(root.getRecordCount().intValue())
            .build();
    return bundler.bundle(
        Bundler.BundleContext.of(
            linkConfig,
            root.getAllergyIntolerances() == null
                ? Collections.emptyList()
                : root.getAllergyIntolerances().getAllergyIntolerance(),
            transformer,
            AllergyIntolerance.Entry::new,
            AllergyIntolerance.Bundle::new));
  }

  private CdwAllergyIntolerance105Root mrAndersonSearch(MultiValueMap<String, String> params) {
    Query<CdwAllergyIntolerance105Root> query =
        Query.forType(CdwAllergyIntolerance105Root.class)
            .profile(Query.Profile.ARGONAUT)
            .resource("AllergyIntolerance")
            .version("1.05")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public AllergyIntolerance read(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @PathVariable("publicId") String publicId) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.read(publicId);
    }
    return transformer.apply(
        firstPayloadItem(
            hasPayload(mrAndersonSearch(Parameters.forIdentity(publicId)).getAllergyIntolerances())
                .getAllergyIntolerance()));
  }

  /** Return the raw Datamart document for the given identifier. */
  @GetMapping(value = "/{publicId}/raw")
  public String readRaw(@PathVariable("publicId") String publicId) {
    return datamart.readRaw(publicId);
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public AllergyIntolerance.Bundle searchById(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchByIdentifier(datamartHeader, id, page, count);
  }

  /** Search by identifier. */
  @GetMapping(params = {"identifier"})
  public AllergyIntolerance.Bundle searchByIdentifier(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    AllergyIntolerance resource = read(datamartHeader, identifier);
    return bundle(
        Parameters.builder()
            .add("identifier", identifier)
            .add("page", page)
            .add("_count", count)
            .build(),
        resource == null ? emptyList() : asList(resource),
        resource == null ? 0 : 1);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public AllergyIntolerance.Bundle searchByPatient(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build();
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.bundle(parameters);
    }
    return mrAndersonBundle(parameters);
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody AllergyIntolerance.Bundle bundle) {
    return Validator.create().validate(bundle);
  }

  public interface Transformer
      extends Function<
          CdwAllergyIntolerance105Root.CdwAllergyIntolerances.CdwAllergyIntolerance,
          AllergyIntolerance> {}

  /**
   * This class is being used to help organize the code such that all the datamart logic is
   * contained together. In the future when Mr. Anderson support is dropped, this class can be
   * eliminated.
   */
  private class Datamart {
    AllergyIntolerance.Bundle bundle(MultiValueMap<String, String> publicParameters) {
      MultiValueMap<String, String> cdwParameters =
          witnessProtection.replacePublicIdsWithCdwIds(publicParameters);

      // Only patient search is supported
      String icn = cdwParameters.getFirst("patient");
      TypedQuery<AllergyIntoleranceEntity> query =
          entityManager.createQuery(
              "Select ai from AllergyIntoleranceEntity ai where ai.icn = :patient",
              AllergyIntoleranceEntity.class);
      query.setParameter("patient", icn);

      int page = Integer.parseInt(cdwParameters.getOrDefault("page", asList("1")).get(0));
      int count = Integer.parseInt(cdwParameters.getOrDefault("_count", asList("15")).get(0));
      query.setFirstResult((page - 1) * count);
      query.setMaxResults(count);

      List<DatamartAllergyIntolerance> payloads =
          query
              .getResultStream()
              .map(entity -> entity.asDatamartAllergyIntolerance())
              .collect(Collectors.toList());

      replaceReferences(payloads);

      List<AllergyIntolerance> fhir =
          payloads
              .stream()
              .map(
                  dm ->
                      DatamartAllergyIntoleranceTransformer.builder().datamart(dm).build().toFhir())
              .collect(Collectors.toList());

      return AllergyIntoleranceController.this.bundle(publicParameters, fhir, totalRecords(icn));
    }

    AllergyIntoleranceEntity findById(@PathVariable("publicId") String publicId) {
      MultiValueMap<String, String> publicParameters = Parameters.forIdentity(publicId);
      MultiValueMap<String, String> cdwParameters =
          witnessProtection.replacePublicIdsWithCdwIds(publicParameters);
      AllergyIntoleranceEntity entity =
          entityManager.find(AllergyIntoleranceEntity.class, Parameters.identiferOf(cdwParameters));
      if (entity == null) {
        throw new ResourceExceptions.NotFound(publicParameters);
      }
      return entity;
    }

    boolean isDatamartRequest(String datamartHeader) {
      if (isBlank(datamartHeader)) {
        return defaultToDatamart;
      }
      return BooleanUtils.isTrue(BooleanUtils.toBooleanObject(datamartHeader));
    }

    AllergyIntolerance read(String publicId) {
      DatamartAllergyIntolerance ai = findById(publicId).asDatamartAllergyIntolerance();
      replaceReferences(List.of(ai));
      return transform(ai);
    }

    String readRaw(@PathVariable("publicId") String publicId) {
      return findById(publicId).payload();
    }

    void replaceReferences(Collection<DatamartAllergyIntolerance> resources) {
      witnessProtection.registerAndUpdateReferences(
          resources,
          resource ->
              Stream.concat(
                  Stream.of(resource.recorder().orElse(null), resource.patient().orElse(null)),
                  resource.notes().stream().map(n -> n.practitioner().orElse(null))));
    }

    int totalRecords(String icn) {
      TypedQuery<Long> totalQuery =
          entityManager.createQuery(
              "Select count(ai.id) from AllergyIntoleranceEntity ai where ai.icn = :patient",
              Long.class);
      totalQuery.setParameter("patient", icn);
      return totalQuery.getSingleResult().intValue();
    }

    AllergyIntolerance transform(DatamartAllergyIntolerance dm) {
      return DatamartAllergyIntoleranceTransformer.builder().datamart(dm).build().toFhir();
    }
  }
}
