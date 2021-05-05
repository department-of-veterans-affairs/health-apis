package gov.va.api.health.dataquery.service.controller.medicationrequest;

import static gov.va.api.health.autoconfig.logging.LogSanitizer.sanitize;
import static java.util.Collections.emptyList;

import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.R4Controllers;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder.Category;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderEntity;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderRepository;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementEntity;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementRepository;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.resources.MedicationRequest;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@SuppressWarnings({"WeakerAccess", "EnhancedSwitchMigration"})
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

  private final Pattern medOrderOutpatientCategoryPattern;

  private final Pattern medOrderInpatientCategoryPattern;

  /** R4 MedicationRequest Constructor. */
  public R4MedicationRequestController(
      @Autowired R4Bundler bundler,
      @Autowired MedicationOrderRepository medicationOrderRepository,
      @Autowired MedicationStatementRepository medicationStatementRepository,
      @Autowired WitnessProtection witnessProtection,
      @Value("${pattern.outpatient}") String patternOutpatient,
      @Value("${pattern.inpatient}") String patternInpatient) {
    this.bundler = bundler;
    this.medicationOrderRepository = medicationOrderRepository;
    this.medicationStatementRepository = medicationStatementRepository;
    this.witnessProtection = witnessProtection;
    medOrderOutpatientCategoryPattern = Pattern.compile(patternOutpatient);
    medOrderInpatientCategoryPattern = Pattern.compile(patternInpatient);
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

  private MedicationRequest.Bundle bundleMixedResources(
      MultiValueMap<String, String> parameters, List<MedicationRequest> reports, int totalRecords) {
    return bundle(parameters, reports, totalRecords, -1);
  }

  MedicationRequest.Bundle medicationOrdersForPatient(
      MultiValueMap<String, String> parameters, String icn, int page, int count) {
    Page<MedicationOrderEntity> medicationOrderEntities =
        medicationOrderRepository.findByIcn(
            icn,
            PageRequest.of(page - 1, count == 0 ? 1 : count, MedicationOrderEntity.naturalOrder()));
    int totalRecords = (int) medicationOrderEntities.getTotalElements();
    if (count == 0) {
      return bundleMixedResources(parameters, emptyList(), totalRecords);
    }
    var ctx = newSearchContext(icn, page, count);
    var vulcanizedTransformation = ctx.medicationOrderSupport().transformation();
    List<MedicationRequest> fhir =
        medicationOrderEntities
            .get()
            .map(vulcanizedTransformation.toDatamart())
            .peek(vulcanizedTransformation::applyWitnessProtection)
            .map(vulcanizedTransformation.toResource())
            .collect(Collectors.toList());
    return bundleMixedResources(parameters, fhir, totalRecords);
  }

  MedicationRequest.Bundle medicationStatementsForPatient(
      MultiValueMap<String, String> parameters, String icn, int page, int count) {
    Page<MedicationStatementEntity> medicationStatementEntities =
        medicationStatementRepository.findByIcn(
            icn,
            PageRequest.of(
                page - 1, count == 0 ? 1 : count, MedicationStatementEntity.naturalOrder()));
    int totalRecords = (int) medicationStatementEntities.getTotalElements();
    if (count == 0) {
      return bundleMixedResources(parameters, emptyList(), totalRecords);
    }
    var ctx = newSearchContext(icn, page, count);
    var vulcanizedTransformation = ctx.medicationStatementSupport().transformation();

    List<MedicationRequest> fhir =
        medicationStatementEntities
            .get()
            .map(vulcanizedTransformation.toDatamart())
            .peek(vulcanizedTransformation::applyWitnessProtection)
            .map(vulcanizedTransformation.toResource())
            .collect(Collectors.toList());
    return bundleMixedResources(parameters, fhir, totalRecords);
  }

  SearchContext newSearchContext(String patient, int page, int count) {
    return new SearchContext(patient, count, page);
  }

  /** Read Support. */
  @GetMapping(value = {"/{publicId}"})
  public MedicationRequest read(@PathVariable("publicId") String publicId) {
    return vulcanizedReaderFor(publicId).read(publicId);
  }

  /** Read Raw Datamart Payload Support. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return vulcanizedReaderFor(publicId).readRaw(publicId, response);
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
    return R4Controllers.searchById(parameters, this::read, this::bundleMixedResources);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public MedicationRequest.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    SearchContext ctx = newSearchContext(patient, page, count);
    if (count == 0) {
      return bundleMixedResources(ctx.parameters(), emptyList(), ctx.totalPages());
    }
    log.info("Looking for {} ({})", patient, ctx.toCdwId());
    if (ctx.lastPageWithMedicationStatement() >= page) {
      return bundle(
          ctx.parameters(),
          ctx.medicationStatementSupport().search(),
          ctx.totalRecords(),
          ctx.totalPages());
    } else if (ctx.medicationOrderSupport().numMedicationOrdersForPatient() > 0) {
      return bundle(
          ctx.parameters(),
          ctx.medicationOrderSupport().search(),
          ctx.totalRecords(),
          ctx.totalPages());
    }
    return bundleMixedResources(ctx.parameters(), emptyList(), ctx.totalPages());
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
      return medicationOrdersForPatient(parameters, icn, page, count);
    } else if ("plan".equals(intent)) {
      return medicationStatementsForPatient(parameters, icn, page, count);
    } else {
      return bundleMixedResources(parameters, emptyList(), 0);
    }
  }

  private VulcanizedReader<?, ?, MedicationRequest, String> vulcanizedReaderFor(String publicId) {
    ResourceIdentity resourceIdentity = witnessProtection.toResourceIdentity(publicId);
    SearchContext ctx = newSearchContext(publicId, 1, 1);
    switch (resourceIdentity.resource()) {
      case "MEDICATION_ORDER":
        return ctx.medicationOrderSupport().vulcanizedReader();
      case "MEDICATION_STATEMENT":
        return ctx.medicationStatementSupport().vulcanizedReader();
      default:
        throw new ResourceExceptions.NotFound(publicId);
    }
  }

  @lombok.Value
  class SearchContext {
    private String patient;

    private int count;

    private int page;

    private MedicationOrderSupport medicationOrderSupport;

    private MedicationStatementSupport medicationStatementSupport;

    SearchContext(String patient, int count, int page) {
      this.patient = patient;
      this.count = count;
      this.page = page;
      medicationOrderSupport = new MedicationOrderSupport(this);
      medicationStatementSupport = new MedicationStatementSupport(this);
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

    int totalRecords() {
      int statements = (int) medicationStatementSupport.numMedicationStatementsForPatient();
      int orders = (int) medicationOrderSupport.numMedicationOrdersForPatient();
      return statements + orders;
    }
  }

  @lombok.Value
  @AllArgsConstructor
  class MedicationOrderSupport {
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
      return medicationOrderEntities()
          .get()
          .map(transformation().toDatamart())
          .peek(this::updateCategory)
          .peek(dm -> transformation().applyWitnessProtection(dm))
          .map(transformation().toResource())
          .collect(Collectors.toList());
    }

    VulcanizedTransformation<MedicationOrderEntity, DatamartMedicationOrder, MedicationRequest>
        transformation() {
      return VulcanizedTransformation.toDatamart(MedicationOrderEntity::asDatamartMedicationOrder)
          .toResource(
              dm ->
                  R4MedicationRequestFromMedicationOrderTransformer.builder()
                      .datamart(dm)
                      .build()
                      .toFhir())
          .witnessProtection(witnessProtection)
          .replaceReferences(
              resource ->
                  Stream.of(resource.medication(), resource.patient(), resource.prescriber()))
          .build();
    }

    void updateCategory(DatamartMedicationOrder datamartMedicationOrder) {
      if (medOrderOutpatientCategoryPattern.matcher(datamartMedicationOrder.cdwId()).matches()) {
        datamartMedicationOrder.category(Category.OUTPATIENT);
      } else if (medOrderInpatientCategoryPattern
          .matcher(datamartMedicationOrder.cdwId())
          .matches()) {
        datamartMedicationOrder.category(Category.INPATIENT);
      }
    }

    VulcanizedReader<MedicationOrderEntity, DatamartMedicationOrder, MedicationRequest, String>
        vulcanizedReader() {
      return VulcanizedReader
          .<MedicationOrderEntity, DatamartMedicationOrder, MedicationRequest, String>
              forTransformation(transformation())
          .repository(medicationOrderRepository)
          .toPatientId(e -> Optional.of(e.icn()))
          .toPrimaryKey(Function.identity())
          .toPayload(MedicationOrderEntity::payload)
          .build();
    }
  }

  @lombok.Value
  @AllArgsConstructor
  class MedicationStatementSupport {
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
      return medicationStatementEntities()
          .get()
          .map(transformation().toDatamart())
          .peek(dm -> transformation().applyWitnessProtection(dm))
          .map(transformation().toResource())
          .collect(Collectors.toList());
    }

    VulcanizedTransformation<
            MedicationStatementEntity, DatamartMedicationStatement, MedicationRequest>
        transformation() {
      return VulcanizedTransformation.toDatamart(
              MedicationStatementEntity::asDatamartMedicationStatement)
          .toResource(
              dm ->
                  R4MedicationRequestFromMedicationStatementTransformer.builder()
                      .datamart(dm)
                      .build()
                      .toFhir())
          .witnessProtection(witnessProtection)
          .replaceReferences(resource -> Stream.of(resource.medication(), resource.patient()))
          .build();
    }

    VulcanizedReader<
            MedicationStatementEntity, DatamartMedicationStatement, MedicationRequest, String>
        vulcanizedReader() {
      return VulcanizedReader
          .<MedicationStatementEntity, DatamartMedicationStatement, MedicationRequest, String>
              forTransformation(transformation())
          .repository(medicationStatementRepository)
          .toPatientId(e -> Optional.of(e.icn()))
          .toPrimaryKey(Function.identity())
          .toPayload(MedicationStatementEntity::payload)
          .build();
    }
  }
}
