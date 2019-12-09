package gov.va.api.health.dataquery.service.controller.medicationorder;

import static java.util.Collections.emptyList;

import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.argonaut.api.resources.MedicationOrder.Bundle;
import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
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
 * Request Mappings for Medication Order Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-medicationorder.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@Slf4j
@RestController
@RequestMapping(
  value = {"/dstu2/MedicationOrder"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
public class Dstu2MedicationOrderController {

  private Bundler bundler;

  private MedicationOrderRepository repository;

  private WitnessProtection witnessProtection;

  /** All args constructor. */
  public Dstu2MedicationOrderController(
      @Autowired Bundler bundler,
      @Autowired MedicationOrderRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

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

  /** Read by identifier. */
  @GetMapping(value = {"/{publicId}"})
  public MedicationOrder read(@PathVariable("publicId") String publicId) {
    DatamartMedicationOrder medicationOrder = findById(publicId).asDatamartMedicationOrder();
    replaceReferences(List.of(medicationOrder));
    return transform(medicationOrder);
  }

  /** Read by id, raw data. */
  @GetMapping(
    value = {"/{publicId}"},
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    MedicationOrderEntity entity = findById(publicId);
    AbstractIncludesIcnMajig.addHeader(response, entity.icn());
    return entity.payload();
  }

  Collection<DatamartMedicationOrder> replaceReferences(
      Collection<DatamartMedicationOrder> resources) {
    witnessProtection.registerAndUpdateReferences(
        resources,
        resource -> Stream.of(resource.medication(), resource.patient(), resource.prescriber()));
    return resources;
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public MedicationOrder.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MedicationOrder medicationOrder = read(id);
    return bundle(
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build(),
        medicationOrder == null || count == 0 ? emptyList() : List.of(medicationOrder),
        medicationOrder == null ? 0 : 1);
  }

  /** Search by identifier. */
  @GetMapping(params = {"identifier"})
  public MedicationOrder.Bundle searchByIdentifier(
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(identifier, page, count);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public MedicationOrder.Bundle searchByPatient(
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
            PageRequest.of(
                page - 1, count == 0 ? 1 : count, MedicationOrderEntity.naturalOrder())));
  }

  MedicationOrder transform(DatamartMedicationOrder dm) {
    return Dstu2MedicationOrderTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Validate Endpoint. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody MedicationOrder.Bundle bundle) {
    return Validator.create().validate(bundle);
  }
}
