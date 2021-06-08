package gov.va.api.health.dataquery.service.controller.medicationrequest;

import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;
import static gov.va.api.lighthouse.vulcan.VulcanConfiguration.PagingConfiguration.noSortableParameters;
import static java.util.Collections.emptyList;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.R4PatientReferenceMapping;
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
import gov.va.api.health.ids.client.IdEncoder;
import gov.va.api.health.r4.api.resources.MedicationRequest;
import gov.va.api.lighthouse.datamart.DatamartEntity;
import gov.va.api.lighthouse.datamart.ResourceNameTranslation;
import gov.va.api.lighthouse.vulcan.CircuitBreaker;
import gov.va.api.lighthouse.vulcan.RequestContext;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import gov.va.api.lighthouse.vulcan.mappings.TokenParameter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
  public static final String MEDICATION_REQUEST_INTENT_SYSTEM =
      "http://hl7.org/fhir/CodeSystem/medicationrequest-intent";

  private final R4Bundler bundler;

  private final LinkProperties linkProperties;

  private final MedicationOrderRepository medicationOrderRepository;

  private final MedicationStatementRepository medicationStatementRepository;

  private final WitnessProtection witnessProtection;

  private final Pattern medOrderOutpatientCategoryPattern;

  private final Pattern medOrderInpatientCategoryPattern;

  /** R4 MedicationRequest Constructor. */
  public R4MedicationRequestController(
      @Autowired R4Bundler bundler,
      @Autowired LinkProperties linkProperties,
      @Autowired MedicationOrderRepository medicationOrderRepository,
      @Autowired MedicationStatementRepository medicationStatementRepository,
      @Autowired WitnessProtection witnessProtection,
      @Value("${pattern.outpatient}") String patternOutpatient,
      @Value("${pattern.inpatient}") String patternInpatient) {
    this.bundler = bundler;
    this.linkProperties = linkProperties;
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

  MedicationRequestContext newContext(String patient, int page, int count) {
    return new MedicationRequestContext(patient, count, page);
  }

  /** Read Support. */
  @GetMapping(value = {"/{publicId}"})
  public MedicationRequest read(@PathVariable("publicId") String publicId) {
    return newContext(publicId, 1, 1).determineVulcanizedReader().read(publicId);
  }

  /** Read Raw Datamart Payload Support. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return newContext(publicId, 1, 1).determineVulcanizedReader().readRaw(publicId, response);
  }

  private String resourceTypeForId(String publicId) {
    try {
      ResourceIdentity resourceIdentity = witnessProtection.toResourceIdentity(publicId);
      return ResourceNameTranslation.get().identityServiceToFhir(resourceIdentity.resource());
    } catch (IdEncoder.BadId e) {
      // Don't throw. Searches will return an empty bundle and reads will 404.
      return "undetermined";
    }
  }

  /** Search Support. */
  @GetMapping
  public MedicationRequest.Bundle search(HttpServletRequest request) {
    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    request.getParameterMap().forEach((key, value) -> parameters.addAll(key, Arrays.asList(value)));
    parameters.addIfAbsent("page", "1");
    parameters.addIfAbsent("_count", "" + linkProperties.getDefaultPageSize());
    var publicId =
        Stream.of(
                parameters.getFirst("patient"),
                parameters.getFirst("_id"),
                Parameters.identifierOf(parameters))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    var page = Parameters.pageOf(parameters);
    var count = Parameters.countOf(parameters);
    MedicationRequestContext ctx = newContext(publicId, page, count == 0 ? 1 : count);
    var results = ctx.combinedVulcanResultsFor(request);
    if (count == 0) {
      /* -1 tells the bundler to automatically determine paging instead of manually (see below) */
      return bundle(parameters, emptyList(), results.totalRecords(), -1);
    }
    return bundle(
        parameters, results.medicationRequests(), results.totalRecords(), results.totalPages());
  }

  private String toCdwId(String expectedResourceType, String publicId) {
    if (expectedResourceType.equals(resourceTypeForId(publicId))) {
      return witnessProtection.toCdwId(publicId);
    }
    throw CircuitBreaker.noResultsWillBeFound(
        "id", publicId, "publicId provided is not a " + expectedResourceType);
  }

  /**
   * Supported Intents:
   *
   * <p>- code
   *
   * <p>- http://hl7.org/fhir/CodeSystem/medicationrequest-intent|
   *
   * <p>- http://hl7.org/fhir/CodeSystem/medicationrequest-intent|code
   */
  private boolean tokenIntentIsSupported(TokenParameter token, String supportedIntent) {
    var hasSupportedCode = token.hasSupportedCode(supportedIntent);
    return (token.hasSupportedSystem(MEDICATION_REQUEST_INTENT_SYSTEM)
            && (token.hasAnyCode() || hasSupportedCode))
        || (token.hasAnySystem() && hasSupportedCode);
  }

  private <EntityT extends DatamartEntity> VulcanConfiguration<EntityT> vulcanConfigurationFor(
      String resourceType, Class<EntityT> entityClass, Sort sortBy, String intent) {
    return VulcanConfiguration.forEntity(entityClass)
        .paging(linkProperties.pagingConfiguration(resourceType, sortBy, noSortableParameters()))
        .mappings(
            Mappings.forEntity(entityClass)
                .value("_id", "cdwId", s -> toCdwId(resourceType, s))
                .value("identifier", "cdwId", s -> toCdwId(resourceType, s))
                .tokens("intent", t -> tokenIntentIsSupported(t, intent), t -> null)
                .add(R4PatientReferenceMapping.<EntityT>forLinks(linkProperties).get())
                .get())
        .defaultQuery(returnNothing())
        .rules(
            List.of(
                parametersNeverSpecifiedTogether("_id", "identifier", "patient"),
                ifParameter("patient").thenAllowOnlyKnownModifiers("identifier")))
        .build();
  }

  @Builder
  @lombok.Value
  static class MedicationRequestResults {
    int totalRecords;

    int totalPages;

    List<MedicationRequest> medicationRequests;
  }

  @lombok.Value
  class MedicationRequestContext {
    String publicId;

    int count;

    int page;

    MedicationOrderSupport medicationOrderSupport;

    MedicationStatementSupport medicationStatementSupport;

    MedicationRequestContext(String publicId, int count, int page) {
      this.publicId = publicId;
      this.count = count;
      this.page = page;
      medicationOrderSupport = new MedicationOrderSupport(this);
      medicationStatementSupport = new MedicationStatementSupport(this);
    }

    public MedicationRequestResults combinedVulcanResultsFor(HttpServletRequest request) {
      RequestContext<MedicationOrderEntity> medOrdRequestContext =
          RequestContext.forConfig(medicationOrderSupport().configuration())
              .request(request)
              .build();
      RequestContext<MedicationStatementEntity> medStaRequestContext =
          RequestContext.forConfig(medicationStatementSupport().configuration())
              .request(request)
              .build();
      int medOrdCount = medicationOrderSupport().databaseRecordCount(medOrdRequestContext);
      int medStaCount = medicationStatementSupport().databaseRecordCount(medStaRequestContext);
      int medStaLastPage = lastPageWithMedicationStatement(medStaCount);
      int totalPages = totalPages(medStaLastPage, medOrdCount);
      boolean mixedResults = medOrdCount != 0 && medStaCount != 0;
      if (medStaLastPage >= page()) {
        var results =
            medicationStatementRepository.findAll(
                medStaRequestContext.specification(),
                PageRequest.of(page() - 1, count(), MedicationStatementEntity.naturalOrder()));
        return MedicationRequestResults.builder()
            .totalRecords(medOrdCount + medStaCount)
            .totalPages(totalPages)
            .medicationRequests(medicationStatementSupport().transform(results))
            .build();
      } else if (medOrdCount > 0) {
        var results =
            medicationOrderRepository.findAll(
                medOrdRequestContext.specification(),
                PageRequest.of(
                    (mixedResults ? page() - medStaLastPage : page()) - 1,
                    count(),
                    MedicationOrderEntity.naturalOrder()));
        return MedicationRequestResults.builder()
            .totalRecords(medOrdCount + medStaCount)
            .totalPages(totalPages)
            .medicationRequests(medicationOrderSupport().transform(results))
            .build();
      }
      return MedicationRequestResults.builder()
          .totalRecords(0)
          .totalPages(totalPages)
          .medicationRequests(emptyList())
          .build();
    }

    public VulcanizedReader<?, ?, MedicationRequest, String> determineVulcanizedReader() {
      switch (resourceTypeForId(publicId())) {
        case "MedicationOrder":
          return medicationOrderSupport().vulcanizedReader();
        case "MedicationStatement":
          return medicationStatementSupport().vulcanizedReader();
        default:
          throw new ResourceExceptions.NotFound(publicId());
      }
    }

    private int lastPageWithMedicationStatement(int medicationStatementResultCount) {
      return (int) Math.ceil((double) medicationStatementResultCount / count());
    }

    private int totalPages(int medicationStatementLastPage, int medicationOrderCount) {
      return (int)
          (medicationStatementLastPage + Math.ceil(medicationOrderCount / (double) count()));
    }
  }

  @lombok.Value
  @AllArgsConstructor
  class MedicationOrderSupport {
    MedicationRequestContext ctx;

    VulcanConfiguration<MedicationOrderEntity> configuration() {
      return vulcanConfigurationFor(
          "MedicationOrder",
          MedicationOrderEntity.class,
          MedicationOrderEntity.naturalOrder(),
          "order");
    }

    public int databaseRecordCount(RequestContext<MedicationOrderEntity> requestContext) {
      return requestContext.abortSearch()
          ? 0
          : (int) medicationOrderRepository.count(requestContext.specification());
    }

    List<MedicationRequest> transform(Page<MedicationOrderEntity> page) {
      return page.stream()
          .map(transformation().toDatamart())
          .peek(dm -> transformation().applyWitnessProtection(dm))
          .map(transformation().toResource())
          .collect(Collectors.toList());
    }

    VulcanizedTransformation<MedicationOrderEntity, DatamartMedicationOrder, MedicationRequest>
        transformation() {
      return VulcanizedTransformation.<MedicationOrderEntity, DatamartMedicationOrder>toDatamart(
              entity -> {
                var dm = entity.asDatamartMedicationOrder();
                updateCategory(dm);
                return dm;
              })
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
    MedicationRequestContext ctx;

    VulcanConfiguration<MedicationStatementEntity> configuration() {
      return vulcanConfigurationFor(
          "MedicationStatement",
          MedicationStatementEntity.class,
          MedicationStatementEntity.naturalOrder(),
          "plan");
    }

    public int databaseRecordCount(RequestContext<MedicationStatementEntity> requestContext) {
      return requestContext.abortSearch()
          ? 0
          : (int) medicationStatementRepository.count(requestContext.specification());
    }

    List<MedicationRequest> transform(Page<MedicationStatementEntity> results) {
      return results.stream()
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
