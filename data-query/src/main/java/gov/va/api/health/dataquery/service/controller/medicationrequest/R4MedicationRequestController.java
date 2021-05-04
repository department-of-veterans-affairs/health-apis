package gov.va.api.health.dataquery.service.controller.medicationrequest;

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
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    var parameters =
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build();
    SearchContext ctx = newSearchContext(patient, page, count);
    if (count == 0) {
      return bundleMixedResources(parameters, emptyList(), ctx.totalPages());
    }
    if (ctx.lastPageWithMedicationStatement() >= page) {
      return bundle(
          parameters,
          ctx.medicationStatementSupport().searchByPatient(),
          ctx.totalRecords(),
          ctx.totalPages());
    } else if (ctx.medicationOrderSupport().numMedicationOrdersForPatient() > 0) {
      return bundle(
          parameters,
          ctx.medicationOrderSupport().searchByPatient(),
          ctx.totalRecords(),
          ctx.totalPages());
    }
    return bundleMixedResources(parameters, emptyList(), ctx.totalPages());
  }

  /** Search by patient and intent. */
  @GetMapping(params = {"patient", "intent"})
  public MedicationRequest.Bundle searchByPatientAndIntent(
      @RequestParam("patient") String patient,
      @RequestParam("intent") String intent,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("patient", patient)
            .add("intent", intent)
            .add("page", page)
            .add("_count", count)
            .build();
    SearchContext ctx = new SearchContext(patient, count, page);
    // Only return if the intent is type order or plan. Otherwise return an empty bundle.
    // If the intent == plan then it is a MedicationStatement
    // If the intent == order then it is a MedicationOrder
    if ("order".equals(intent)) {
      return bundle(
          parameters,
          count == 0 ? emptyList() : ctx.medicationOrderSupport().searchByPatient(page - 1),
          (int) ctx.medicationOrderSupport().countForPatient(),
          -1);
    } else if ("plan".equals(intent)) {
      return bundle(
          parameters,
          count == 0 ? emptyList() : ctx.medicationStatementSupport().searchByPatient(),
          (int) ctx.medicationStatementSupport().countForPatient(),
          -1);
    } else {
      return bundleMixedResources(parameters, emptyList(), 0);
    }
  }

  private VulcanizedReader<?, ?, MedicationRequest, String> vulcanizedReaderFor(String publicId) {
    ResourceIdentity resourceIdentity = witnessProtection.toResourceIdentity(publicId);
    switch (resourceIdentity.resource()) {
      case "MEDICATION_ORDER":
        return new MedicationOrderSupport().vulcanizedReader();
      case "MEDICATION_STATEMENT":
        return new MedicationStatementSupport().vulcanizedReader();
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
      this.count = count;
      this.page = page;
      this.patient = witnessProtection.toCdwId(patient);
      medicationOrderSupport = new MedicationOrderSupport(this);
      medicationStatementSupport = new MedicationStatementSupport(this);
    }

    int lastPageWithMedicationStatement() {
      return (int) Math.ceil((double) medicationStatementSupport().countForPatient() / count());
    }

    int totalPages() {
      return (int)
          (lastPageWithMedicationStatement()
              + Math.ceil(medicationOrderSupport().countForPatient() / (double) count()));
    }

    int totalRecords() {
      int statements = (int) medicationStatementSupport().countForPatient();
      int orders = (int) medicationOrderSupport().countForPatient();
      return statements + orders;
    }
  }

  @lombok.Data
  @AllArgsConstructor
  @NoArgsConstructor
  class MedicationOrderSupport {
    private SearchContext ctx;

    private long countForPatient;

    MedicationOrderSupport(SearchContext ctx) {
      this.ctx = ctx;
      this.countForPatient = numMedicationOrdersForPatient();
    }

    int mixedResourcesMedicationOrderPage() {
      return ctx().page() - ctx().lastPageWithMedicationStatement() - 1;
    }

    private long numMedicationOrdersForPatient() {
      var patientSpecification = MedicationOrderRepository.PatientSpecification.of(ctx().patient());
      return medicationOrderRepository.count(patientSpecification);
    }

    List<MedicationRequest> searchByPatient() {
      return searchByPatient(mixedResourcesMedicationOrderPage());
    }

    List<MedicationRequest> searchByPatient(int databasePage) {
      var medicationOrders =
          medicationOrderRepository.findByIcn(
              ctx().patient(),
              PageRequest.of(databasePage, ctx().count(), MedicationOrderEntity.naturalOrder()));
      return medicationOrders.stream()
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

  @lombok.Data
  @AllArgsConstructor
  @NoArgsConstructor
  class MedicationStatementSupport {
    private SearchContext ctx;

    private long countForPatient;

    MedicationStatementSupport(SearchContext ctx) {
      this.ctx = ctx;
      this.countForPatient = numMedicationStatementsForPatient();
    }

    private long numMedicationStatementsForPatient() {
      var patientSpecification =
          MedicationStatementRepository.PatientSpecification.of(ctx().patient());
      return medicationStatementRepository.count(patientSpecification);
    }

    List<MedicationRequest> searchByPatient() {
      var medicationStatements =
          medicationStatementRepository.findByIcn(
              ctx().patient(),
              PageRequest.of(
                  ctx().page() - 1, ctx().count(), MedicationStatementEntity.naturalOrder()));
      return medicationStatements.stream()
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
