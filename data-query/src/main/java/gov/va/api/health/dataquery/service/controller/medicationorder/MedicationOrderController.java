package gov.va.api.health.dataquery.service.controller.medicationorder;

import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;
import static java.util.Collections.emptyList;

import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.argonaut.api.resources.MedicationOrder.Bundle;
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
import gov.va.dvp.cdw.xsd.model.CdwMedicationOrder103Root;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
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
 * Request Mappings for Medication Order Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-medicationorder.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@Slf4j
@RestController
@RequestMapping(
  value = {"MedicationOrder", "/api/MedicationOrder"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
public class MedicationOrderController {

  Datamart datamart = new Datamart();

  private Transformer transformer;

  private MrAndersonClient mrAndersonClient;

  private Bundler bundler;

  private boolean defaultToDatamart;

  private MedicationOrderRepository repository;

  private WitnessProtection witnessProtection;

  /** All args constructor. */
  public MedicationOrderController(
      @Value(value = "${datamart.medication-order}") boolean defaultToDatamart,
      @Autowired Transformer transformer,
      @Autowired MrAndersonClient mrAndersonClient,
      @Autowired Bundler bundler,
      @Autowired MedicationOrderRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.defaultToDatamart = defaultToDatamart;
    this.transformer = transformer;
    this.mrAndersonClient = mrAndersonClient;
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private Bundle bundle(MultiValueMap<String, String> parameters, int page, int count) {
    CdwMedicationOrder103Root root = search(parameters);
    LinkConfig linkConfig =
        LinkConfig.builder()
            .path("MedicationOrder")
            .queryParams(parameters)
            .page(page)
            .recordsPerPage(count)
            .totalRecords(root.getRecordCount())
            .build();
    return bundler.bundle(
        BundleContext.of(
            linkConfig,
            root.getMedicationOrders() == null
                ? Collections.emptyList()
                : root.getMedicationOrders().getMedicationOrder(),
            transformer,
            MedicationOrder.Entry::new,
            MedicationOrder.Bundle::new));
  }

  /** Read by identifier. */
  @GetMapping(value = {"/{publicId}"})
  public MedicationOrder read(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @PathVariable("publicId") String publicId) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.read(publicId);
    }
    return transformer.apply(
        firstPayloadItem(
            hasPayload(search(Parameters.forIdentity(publicId)).getMedicationOrders())
                .getMedicationOrder()));
  }

  /** Read by id, raw data. */
  @GetMapping(
    value = {"/{publicId}"},
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId) {
    return datamart.readRaw(publicId);
  }

  private CdwMedicationOrder103Root search(MultiValueMap<String, String> params) {
    Query<CdwMedicationOrder103Root> query =
        Query.forType(CdwMedicationOrder103Root.class)
            .profile(Query.Profile.ARGONAUT)
            .resource("MedicationOrder")
            .version("1.04")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public MedicationOrder.Bundle searchById(
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

  /** Search by identifier. */
  @GetMapping(params = {"identifier"})
  public MedicationOrder.Bundle searchByIdentifier(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(datamartHeader, identifier, page, count);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public MedicationOrder.Bundle searchByPatient(
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

  /** Validate Endpoint. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody MedicationOrder.Bundle bundle) {
    return Validator.create().validate(bundle);
  }

  public interface Transformer
      extends Function<
          CdwMedicationOrder103Root.CdwMedicationOrders.CdwMedicationOrder, MedicationOrder> {}

  /**
   * This class is being used to help organize the code such that all the datamart logic is
   * contained together. In the future when Mr. Anderson support is dropped, this class can be
   * eliminated.
   */
  private class Datamart {

    Bundle bundle(
        MultiValueMap<String, String> parameters, List<MedicationOrder> results, int totalRecords) {
      PageLinks.LinkConfig linkConfig =
          PageLinks.LinkConfig.builder()
              .path("MedicationOrder")
              .queryParams(parameters)
              .page(Parameters.pageOf(parameters))
              .recordsPerPage(Parameters.countOf(parameters))
              .totalRecords(totalRecords)
              .build();
      return bundler.bundle(
          Bundler.BundleContext.of(
              linkConfig,
              results,
              Function.identity(),
              MedicationOrder.Entry::new,
              MedicationOrder.Bundle::new));
    }

    private Bundle bundle(
        MultiValueMap<String, String> parameters, int count, Page<MedicationOrderEntity> entities) {
      log.info("Search {} found {} results", parameters, entities.getTotalElements());
      if (count == 0) {
        return bundle(parameters, emptyList(), (int) entities.getTotalElements());
      }
      return bundle(
          parameters,
          replaceReferences(
                  entities
                      .get()
                      .map(MedicationOrderEntity::asDatamartMedicationOrder)
                      .collect(Collectors.toList()))
              .stream()
              .map(this::transform)
              .collect(Collectors.toList()),
          (int) entities.getTotalElements());
    }

    MedicationOrderEntity findById(String publicId) {
      Optional<MedicationOrderEntity> entity =
          repository.findById(witnessProtection.toCdwId(publicId));
      return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
    }

    boolean isDatamartRequest(String datamartHeader) {
      if (datamartHeader.isBlank()) {
        return defaultToDatamart;
      }
      return BooleanUtils.isTrue(BooleanUtils.toBooleanObject(datamartHeader));
    }

    private PageRequest page(int page, int count) {
      return PageRequest.of(page - 1, count == 0 ? 1 : count, MedicationOrderEntity.naturalOrder());
    }

    MedicationOrder read(String publicId) {
      DatamartMedicationOrder medicationOrder = findById(publicId).asDatamartMedicationOrder();
      replaceReferences(List.of(medicationOrder));
      return transform(medicationOrder);
    }

    String readRaw(String publicId) {
      return findById(publicId).payload();
    }

    Collection<DatamartMedicationOrder> replaceReferences(
        Collection<DatamartMedicationOrder> resources) {
      witnessProtection.registerAndUpdateReferences(
          resources,
          resource -> Stream.of(resource.medication(), resource.patient(), resource.prescriber()));
      return resources;
    }

    Bundle searchById(String publicId, int page, int count) {
      MedicationOrder medicationOrder = read(publicId);
      return bundle(
          Parameters.builder()
              .add("identifier", publicId)
              .add("page", page)
              .add("_count", count)
              .build(),
          medicationOrder == null || count == 0 ? emptyList() : List.of(medicationOrder),
          medicationOrder == null ? 0 : 1);
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

    MedicationOrder transform(DatamartMedicationOrder dm) {
      return DatamartMedicationOrderTransformer.builder().datamart(dm).build().toFhir();
    }
  }
}
