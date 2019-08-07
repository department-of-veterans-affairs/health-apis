package gov.va.api.health.dataquery.service.controller.medication;

import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;
import static java.util.Collections.emptyList;

import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.dataquery.service.controller.*;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.dvp.cdw.xsd.model.CdwMedication101Root;


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import gov.va.api.health.argonaut.api.resources.Medication.Bundle;
import gov.va.api.health.argonaut.api.resources.Medication.Entry;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Autowired}))
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
  @GetMapping(value = {"/{publicId}/raw"})
  public String readRaw(@PathVariable("publicId") String publicId) {
    MultiValueMap<String, String> publicParameters = Parameters.forIdentity(publicId);
    MultiValueMap<String, String> cdwParameters =
            witnessProtection.replacePublicIdsWithCdwIds(publicParameters);
    Optional<MedicationEntity> entity =
            repository.findById(Parameters.identiferOf(cdwParameters));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicParameters)).payload();
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
    return bundle(
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build(),
        page,
        count);
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
                      linkConfig,
                      reports,
                      Function.identity(),
                      Entry::new,
                      Bundle::new));
    }

    private Bundle bundle(
            MultiValueMap<String, String> parameters, int count, Page<MedicationEntity> entities) {

      log.info("Search {} found {} results", parameters, entities.getTotalElements());
      if (count == 0) {
        return bundle(parameters, emptyList(), (int) entities.getTotalElements());
      }

      return bundle(
              parameters,
                      entities
                              .get()
                              .map(MedicationEntity::asDatamartMedication)
                              .collect(Collectors.toList())
                      .stream()
                      .map(this::transform)
                      .collect(Collectors.toList()),
              (int) entities.getTotalElements());
    }

    MedicationEntity findById(String publicId) {
      Optional<MedicationEntity> entity =
              repository.findById(witnessProtection.toCdwId(publicId));
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
