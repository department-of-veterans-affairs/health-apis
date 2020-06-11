package gov.va.api.health.dataquery.service.controller.medicationrequest;

import static gov.va.api.health.autoconfig.logging.LogSanitizer.sanitize;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderEntity;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderRepository;
import gov.va.api.health.uscorer4.api.resources.MedicationRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
 * Request Mappings for Medication Request Profile, see
 * https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-medicationrequest.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@Slf4j
@RestController
@RequestMapping(
    value = {"/r4/MedicationRequest"},
    produces = {"application/json", "application/fhir+json"})
public class R4MedicationRequestController {
  private R4Bundler bundler;

  private MedicationOrderRepository repository;

  private WitnessProtection witnessProtection;

  /** R4 MedicationRequest Constructor. */
  public R4MedicationRequestController(
      @Autowired R4Bundler bundler,
      @Autowired MedicationOrderRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private MedicationRequest.Bundle bundle(
      MultiValueMap<String, String> parameters, List<MedicationRequest> reports, int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("MedicationRequest")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        linkConfig, reports, MedicationRequest.Entry::new, MedicationRequest.Bundle::new);
  }

  private MedicationRequest.Bundle bundle(
      MultiValueMap<String, String> parameters, int count, Page<MedicationOrderEntity> entities) {
    log.info("Search {} found {} results", parameters, entities.getTotalElements());
    if (count == 0) {
      return bundle(parameters, emptyList(), (int) entities.getTotalElements());
    }
    List<DatamartMedicationOrder> datamartMedicationOrders =
        entities
            .get()
            .map(MedicationOrderEntity::asDatamartMedicationOrder)
            .collect(Collectors.toList());
    replaceReferences(datamartMedicationOrders);
    List<MedicationRequest> fhir =
        datamartMedicationOrders.stream()
            .map(
                dm ->
                    R4MedicationRequestFromMedicationOrderTransformer.builder()
                        .datamart(dm)
                        .build()
                        .toFhir())
            .collect(Collectors.toList());
    return bundle(parameters, fhir, (int) entities.getTotalElements());
  }

  MedicationOrderEntity findById(String publicId) {
    Optional<MedicationOrderEntity> entity =
        repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  /** Read by identifier. */
  @GetMapping(value = {"/{publicId}"})
  public MedicationRequest read(@PathVariable("publicId") String publicId) {
    DatamartMedicationOrder medicationOrder = findById(publicId).asDatamartMedicationOrder();
    replaceReferences(List.of(medicationOrder));
    return transform(medicationOrder);
  }

  /** Read by id, raw data. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    MedicationOrderEntity entity = findById(publicId);
    IncludesIcnMajig.addHeader(response, entity.icn());
    return entity.payload();
  }

  Collection<DatamartMedicationOrder> replaceReferences(
      Collection<DatamartMedicationOrder> resources) {
    witnessProtection.registerAndUpdateReferences(
        resources,
        resource -> Stream.of(resource.medication(), resource.patient(), resource.prescriber()));
    return resources;
  }

  /** Search R4 MedicationRequest by _id. */
  @GetMapping(params = {"_id"})
  public MedicationRequest.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchByIdentifier(id, page, count);
  }

  /** Search R4 MedicationRequest by identifier. */
  @GetMapping(params = {"identifier"})
  public MedicationRequest.Bundle searchByIdentifier(
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build();
    MedicationRequest resource = read(id);
    int totalRecords = resource == null ? 0 : 1;
    if (resource == null || page != 1 || count <= 0) {
      return bundle(parameters, emptyList(), totalRecords);
    }
    return bundle(parameters, asList(resource), totalRecords);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public MedicationRequest.Bundle searchByPatient(
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

  /** Search by patient and intent. */
  @GetMapping(params = {"patient", "intent"})
  public MedicationRequest.Bundle searchByPatientAndIntent(
      @RequestParam("patient") String patient,
      @RequestParam("intent") String intent,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    String icn = witnessProtection.toCdwId(patient);

    log.info(
        "Looking for patient: {} ({}), intent: {} . Only returning intent:order searches.",
        sanitize(patient),
        sanitize(icn),
        sanitize(intent));

    // Only return if the intent is type order. Otherwise return an empty bundle
    if ("order".equals(intent)) {
      return bundle(
          Parameters.builder()
              .add("patient", patient)
              .add("intent", intent)
              .add("page", page)
              .add("_count", count)
              .build(),
          count,
          repository.findByIcn(
              icn,
              PageRequest.of(
                  page - 1, count == 0 ? 1 : count, MedicationOrderEntity.naturalOrder())));
    } else {
      return bundle(Parameters.builder()
              .add("patient", patient)
              .add("intent", intent)
              .add("page", page)
              .add("_count", count)
              .build(), emptyList(),0);
    }
  }

  MedicationRequest transform(DatamartMedicationOrder dm) {
    return R4MedicationRequestFromMedicationOrderTransformer.builder()
        .datamart(dm)
        .build()
        .toFhir();
  }
}
