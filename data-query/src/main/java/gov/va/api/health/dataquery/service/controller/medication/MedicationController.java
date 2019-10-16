package gov.va.api.health.dataquery.service.controller.medication;

import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;
import static java.util.Collections.emptyList;

import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.argonaut.api.resources.Medication.Bundle;
import gov.va.api.health.argonaut.api.resources.Medication.Entry;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.dvp.cdw.xsd.model.CdwMedication101Root;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
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
 * Request Mappings for Medication Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-medication.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
  value = {"Medication", "/api/Medication"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
public class MedicationController {
  private final MedicationController.Datamart datamart = new MedicationController.Datamart();
  private Transformer transformer;
  private MrAndersonClient mrAndersonClient;
  private Bundler bundler;
  private MedicationRepository repository;
  private WitnessProtection witnessProtection;
  private boolean defaultToDatamart;

  /** Spring constructor. */
  @SuppressWarnings("ParameterHidesMemberVariable")
  public MedicationController(
      @Value("${datamart.medication}") boolean defaultToDatamart,
      @Autowired MedicationController.Transformer transformer,
      @Autowired MrAndersonClient mrAndersonClient,
      @Autowired Bundler bundler,
      @Autowired MedicationRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.defaultToDatamart = defaultToDatamart;
    this.transformer = transformer;
    this.mrAndersonClient = mrAndersonClient;
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private Bundle bundle(MultiValueMap<String, String> parameters, int page, int count) {
    CdwMedication101Root root = search(parameters);
    LinkConfig linkConfig =
        LinkConfig.builder()
            .path("Medication")
            .queryParams(parameters)
            .page(page)
            .recordsPerPage(count)
            .totalRecords(root.getRecordCount().intValue())
            .build();
    return bundler.bundle(
        BundleContext.of(
            linkConfig,
            root.getMedications() == null
                ? Collections.emptyList()
                : root.getMedications().getMedication(),
            transformer,
            Entry::new,
            Bundle::new));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Medication read(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @PathVariable("publicId") String publicId) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.read(publicId);
    }
    return transformer.apply(
        firstPayloadItem(
            hasPayload(search(Parameters.forIdentity(publicId)).getMedications()).getMedication()));
  }

  /** Read by id, raw data. */
  @GetMapping(
    value = {"/{publicId}"},
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId) {
    return datamart.readRaw(publicId);
  }

  private CdwMedication101Root search(MultiValueMap<String, String> params) {
    Query<CdwMedication101Root> query =
        Query.forType(CdwMedication101Root.class)
            .profile(Query.Profile.ARGONAUT)
            .resource("Medication")
            .version("1.01")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Bundle searchById(
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
  public Bundle searchByIdentifier(
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
  public OperationOutcome validate(@RequestBody Bundle bundle) {
    return Validator.create().validate(bundle);
  }

  public interface Transformer
      extends Function<CdwMedication101Root.CdwMedications.CdwMedication, Medication> {}

  /**
   * This class is being used to help organize the code such that all the datamart logic is
   * contained together. In the future when Mr. Anderson support is dropped, this class can be
   * eliminated.
   */
  private class Datamart {

    private Bundle bundle(
        MultiValueMap<String, String> parameters, List<Medication> reports, int totalRecords) {
      PageLinks.LinkConfig linkConfig =
          PageLinks.LinkConfig.builder()
              .path("Medication")
              .queryParams(parameters)
              .page(Parameters.pageOf(parameters))
              .recordsPerPage(Parameters.countOf(parameters))
              .totalRecords(totalRecords)
              .build();
      return bundler.bundle(
          Bundler.BundleContext.of(
              linkConfig, reports, Function.identity(), Entry::new, Bundle::new));
    }

    MedicationEntity findById(String publicId) {
      Optional<MedicationEntity> entity = repository.findById(witnessProtection.toCdwId(publicId));
      return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
    }

    boolean isDatamartRequest(String datamartHeader) {
      if (StringUtils.isBlank(datamartHeader)) {
        return defaultToDatamart;
      }
      return BooleanUtils.isTrue(BooleanUtils.toBooleanObject(datamartHeader));
    }

    Medication read(String publicId) {
      DatamartMedication medication = findById(publicId).asDatamartMedication();
      witnessProtection.registerAndUpdateReferences(
          List.of(medication), resource -> Stream.empty());
      return transform(medication);
    }

    String readRaw(String publicId) {
      return findById(publicId).payload();
    }

    Bundle searchById(String publicId, int page, int count) {
      Medication resource = read(publicId);
      return bundle(
          Parameters.builder()
              .add("identifier", publicId)
              .add("page", page)
              .add("_count", count)
              .build(),
          resource == null || count == 0 ? emptyList() : List.of(resource),
          resource == null ? 0 : 1);
    }

    Medication transform(DatamartMedication dm) {
      return DatamartMedicationTransformer.builder().datamart(dm).build().toFhir();
    }
  }
}
