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
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementEntity;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementRepository;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.uscorer4.api.resources.MedicationRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Value;
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
  private final R4Bundler bundler;

  private final MedicationOrderRepository medicationOrderRepository;

  private final MedicationStatementRepository medicationStatementRepository;

  private final WitnessProtection witnessProtection;

  /** R4 MedicationRequest Constructor. */
  public R4MedicationRequestController(
      @Autowired R4Bundler bundler,
      @Autowired MedicationOrderRepository medicationOrderRepository,
      @Autowired MedicationStatementRepository medicationStatementRepository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.medicationOrderRepository = medicationOrderRepository;
    this.medicationStatementRepository = medicationStatementRepository;
    this.witnessProtection = witnessProtection;
  }

  private MedicationRequest.Bundle bundle(
      MultiValueMap<String, String> parameters,
      List<MedicationRequest> reports,
      int totalRecords,
      int totalPages) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("MedicationRequest")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .totalPages(totalPages)
            .build();
    return bundler.bundle(
        linkConfig, reports, MedicationRequest.Entry::new, MedicationRequest.Bundle::new);
  }

  private MedicationRequest.Bundle bundle(
      MultiValueMap<String, String> parameters, List<MedicationRequest> reports, int totalRecords) {
    return bundle(parameters, reports, totalRecords, -1);
  }

  MedicationOrderEntity findByIdMedicationOrderEntity(String publicId) {
    Optional<MedicationOrderEntity> entity =
        medicationOrderRepository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  MedicationStatementEntity findByIdMedicationStatementEntity(String publicId) {
    Optional<MedicationStatementEntity> entity =
        medicationStatementRepository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  List<MedicationRequest> medRequestsFromMedOrders(
      List<DatamartMedicationOrder> datamartMedicationOrders) {
    return datamartMedicationOrders.stream()
        .map(
            dmo ->
                R4MedicationRequestFromMedicationOrderTransformer.builder()
                    .datamart(dmo)
                    .build()
                    .toFhir())
        .collect(Collectors.toList());
  }

  List<MedicationRequest> medRequestsFromMedStatements(
      List<DatamartMedicationStatement> datamartMedicationStatements) {
    return datamartMedicationStatements.stream()
        .map(
            dms ->
                R4MedicationRequestFromMedicationStatementTransformer.builder()
                    .datamart(dms)
                    .build()
                    .toFhir())
        .collect(Collectors.toList());
  }

  /** Read by identifier. */
  @GetMapping(value = {"/{publicId}"})
  public MedicationRequest read(@PathVariable("publicId") String publicId) {
    ResourceIdentity resourceIdentity = witnessProtection.toResourceIdentity(publicId);
    switch (resourceIdentity.resource()) {
      case "MEDICATION_ORDER":
        return readMedicationOrder(publicId);
      case "MEDICATION_STATEMENT":
        return readMedicationStatement(publicId);
      default:
        throw new ResourceExceptions.NotFound(publicId);
    }
  }

  MedicationRequest readMedicationOrder(String publicId) {
    DatamartMedicationOrder medicationOrder =
        findByIdMedicationOrderEntity(publicId).asDatamartMedicationOrder();
    replaceReferencesMedicationOrder(List.of(medicationOrder));
    return transformMedicationOrderToMedicationRequest(medicationOrder);
  }

  MedicationRequest readMedicationStatement(String publicId) {
    DatamartMedicationStatement medicationStatement =
        findByIdMedicationStatementEntity(publicId).asDatamartMedicationStatement();
    replaceReferencesMedicationStatement(List.of(medicationStatement));
    return transformMedicationStatementToMedicationRequest(medicationStatement);
  }

  /** Read by id, raw data. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    ResourceIdentity resourceIdentity = witnessProtection.toResourceIdentity(publicId);
    switch (resourceIdentity.resource()) {
      case "MEDICATION_ORDER":
        MedicationOrderEntity orderEntity = findByIdMedicationOrderEntity(publicId);
        IncludesIcnMajig.addHeader(response, orderEntity.icn());
        return orderEntity.payload();
      case "MEDICATION_STATEMENT":
        MedicationStatementEntity statementEntity = findByIdMedicationStatementEntity(publicId);
        IncludesIcnMajig.addHeader(response, statementEntity.icn());
        return statementEntity.payload();
      default:
        throw new ResourceExceptions.NotFound(publicId);
    }
  }

  Collection<DatamartMedicationOrder> replaceReferencesMedicationOrder(
      Collection<DatamartMedicationOrder> resources) {
    witnessProtection.registerAndUpdateReferences(
        resources,
        resource -> Stream.of(resource.medication(), resource.patient(), resource.prescriber()));
    return resources;
  }

  Collection<DatamartMedicationStatement> replaceReferencesMedicationStatement(
      Collection<DatamartMedicationStatement> resources) {
    witnessProtection.registerAndUpdateReferences(
        resources, resource -> Stream.of(resource.medication(), resource.patient()));
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
    SearchContext ctx = new SearchContext(patient, count, page);
    if (count == 0) {
      return bundle(ctx.parameters(), emptyList(), ctx.totalPages());
    }
    log.info("Looking for {} ({})", patient, ctx.toCdwId());
    if (ctx.lastPageWithMedicationStatement() >= page) {
      return bundle(
          ctx.parameters(),
          ctx.medicationStatementSupport().search(),
          (int) ctx.medicationStatementSupport().medicationStatementEntities().getTotalElements(),
          ctx.totalPages());
    } else if (ctx.medicationOrderSupport().numMedicationOrdersForPatient() > 0) {
      return bundle(
          ctx.parameters(),
          ctx.medicationOrderSupport().search(),
          (int) ctx.medicationOrderSupport().medicationOrderEntities().getTotalElements(),
          ctx.totalPages());
    }
    return bundle(ctx.parameters(), emptyList(), ctx.totalPages());
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
        "Looking for patient: {} ({}), intent: {} .",
        sanitize(patient),
        sanitize(icn),
        sanitize(intent));
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("patient", patient)
            .add("intent", intent)
            .add("page", page)
            .add("_count", count)
            .build();
    // Only return if the intent is type order or plan. Otherwise return an empty bundle.
    // If the intent == plan then it is a MedicationStatement
    // If the intent == order then it is a MedicationOrder
    if ("order".equals(intent)) {
      Page<MedicationOrderEntity> medicationOrderEntities =
          medicationOrderRepository.findByIcn(
              icn,
              PageRequest.of(
                  page - 1, count == 0 ? 1 : count, MedicationOrderEntity.naturalOrder()));
      int totalRecords = (int) medicationOrderEntities.getTotalElements();
      if (count == 0) {
        return bundle(parameters, emptyList(), totalRecords);
      }
      List<DatamartMedicationOrder> datamartMedicationOrders =
          medicationOrderEntities
              .get()
              .map(MedicationOrderEntity::asDatamartMedicationOrder)
              .collect(Collectors.toList());
      replaceReferencesMedicationOrder(datamartMedicationOrders);
      List<MedicationRequest> fhir = medRequestsFromMedOrders(datamartMedicationOrders);
      return bundle(parameters, fhir, totalRecords);
    } else if ("plan".equals(intent)) {
      Page<MedicationStatementEntity> medicationStatementEntities =
          medicationStatementRepository.findByIcn(
              icn,
              PageRequest.of(
                  page - 1, count == 0 ? 1 : count, MedicationStatementEntity.naturalOrder()));
      int totalRecords = (int) medicationStatementEntities.getTotalElements();
      if (count == 0) {
        return bundle(parameters, emptyList(), totalRecords);
      }
      List<DatamartMedicationStatement> datamartMedicationStatements =
          medicationStatementEntities
              .get()
              .map(MedicationStatementEntity::asDatamartMedicationStatement)
              .collect(Collectors.toList());
      replaceReferencesMedicationStatement(datamartMedicationStatements);
      List<MedicationRequest> fhir = medRequestsFromMedStatements(datamartMedicationStatements);
      return bundle(parameters, fhir, totalRecords);
    } else {
      return bundle(parameters, emptyList(), 0);
    }
  }

  MedicationRequest transformMedicationOrderToMedicationRequest(DatamartMedicationOrder dm) {
    return R4MedicationRequestFromMedicationOrderTransformer.builder()
        .datamart(dm)
        .build()
        .toFhir();
  }

  MedicationRequest transformMedicationStatementToMedicationRequest(
      DatamartMedicationStatement dm) {
    return R4MedicationRequestFromMedicationStatementTransformer.builder()
        .datamart(dm)
        .build()
        .toFhir();
  }

  @Value
  private class SearchContext {
    String patient;

    int count;

    int page;

    MedicationOrderSupport medicationOrderSupport;

    MedicationStatementSupport medicationStatementSupport;

    SearchContext(String patient, int count, int page) {
      this.patient = patient;
      this.count = count;
      this.page = page;
      this.medicationOrderSupport = new MedicationOrderSupport(this);
      this.medicationStatementSupport = new MedicationStatementSupport(this);
    }

    int lastPageWithMedicationStatement() {
      return (int)
          Math.ceil(
              (double) medicationStatementSupport().numMedicationStatementsForPatient() / count());
    }

    MultiValueMap<String, String> parameters() {
      return Parameters.builder()
          .add("patient", patient())
          .add("page", page())
          .add("_count", count())
          .build();
    }

    String toCdwId() {
      return witnessProtection.toCdwId(patient);
    }

    int totalPages() {
      return (int)
          (lastPageWithMedicationStatement()
              + Math.ceil(
                  medicationOrderSupport().numMedicationOrdersForPatient() / (double) count()));
    }
  }

  @Value
  @AllArgsConstructor
  private class MedicationOrderSupport {
    SearchContext ctx;

    Page<MedicationOrderEntity> medicationOrderEntities() {
      return medicationOrderRepository.findByIcn(
          ctx().toCdwId(),
          PageRequest.of(
              medicationOrderPage(), ctx().count(), MedicationOrderEntity.naturalOrder()));
    }

    int medicationOrderPage() {
      return ctx().page() - ctx().lastPageWithMedicationStatement() - 1;
    }

    MedicationOrderRepository.PatientSpecification medicationOrderPatientSpec() {
      return MedicationOrderRepository.PatientSpecification.of(ctx().toCdwId());
    }

    long numMedicationOrdersForPatient() {
      return medicationOrderRepository.count(medicationOrderPatientSpec());
    }

    List<MedicationRequest> search() {
      List<DatamartMedicationOrder> datamartMedicationOrders =
          medicationOrderEntities()
              .get()
              .map(MedicationOrderEntity::asDatamartMedicationOrder)
              .collect(Collectors.toList());
      replaceReferencesMedicationOrder(datamartMedicationOrders);
      return medRequestsFromMedOrders(datamartMedicationOrders);
    }
  }

  @Value
  @AllArgsConstructor
  private class MedicationStatementSupport {
    SearchContext ctx;

    Page<MedicationStatementEntity> medicationStatementEntities() {
      return medicationStatementRepository.findByIcn(
          ctx().toCdwId(),
          PageRequest.of(
              ctx().page() - 1, ctx().count(), MedicationStatementEntity.naturalOrder()));
    }

    MedicationStatementRepository.PatientSpecification medicationStatementPatientSpec() {
      return MedicationStatementRepository.PatientSpecification.of(ctx().toCdwId());
    }

    long numMedicationStatementsForPatient() {
      return medicationStatementRepository.count(medicationStatementPatientSpec());
    }

    List<MedicationRequest> search() {
      List<DatamartMedicationStatement> datamartMedicationStatements =
          medicationStatementEntities()
              .get()
              .map(MedicationStatementEntity::asDatamartMedicationStatement)
              .collect(Collectors.toList());
      replaceReferencesMedicationStatement(datamartMedicationStatements);
      return medRequestsFromMedStatements(datamartMedicationStatements);
    }
  }
}
