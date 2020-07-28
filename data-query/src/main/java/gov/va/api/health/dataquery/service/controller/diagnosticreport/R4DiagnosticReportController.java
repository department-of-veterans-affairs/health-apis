package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import com.google.common.base.Splitter;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.DateTimeParameter;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.TokenParameter;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.uscorer4.api.resources.DiagnosticReport;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Diagnostic Report Profile.
 *
 * @implSpec
 *     https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-diagnosticreport-lab.html
 */
@Slf4j
@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
    value = {"/r4/DiagnosticReport"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class R4DiagnosticReportController {
  private static final String DR_CATEGORY_SYSTEM = "http://terminology.hl7.org/CodeSystem/v2-0074";

  private static final String DR_STATUS_SYSTEM = "http://hl7.org/fhir/diagnostic-report-status";

  private R4Bundler bundler;

  private WitnessProtection witnessProtection;

  private DiagnosticReportRepository repository;

  private DiagnosticReport.Bundle bundle(
      MultiValueMap<String, String> parameters,
      List<DiagnosticReport> diagnosticReports,
      int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("DiagnosticReport")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        linkConfig, diagnosticReports, DiagnosticReport.Entry::new, DiagnosticReport.Bundle::new);
  }

  DiagnosticReport.Bundle bundle(
      MultiValueMap<String, String> parameters, Page<DiagnosticReportEntity> entitiesPage) {
    if (Parameters.countOf(parameters) <= 0) {
      return bundle(parameters, emptyList(), (int) entitiesPage.getTotalElements());
    }
    List<DatamartDiagnosticReport> datamart =
        entitiesPage.stream()
            .map(DiagnosticReportEntity::asDatamartDiagnosticReport)
            .collect(Collectors.toList());
    replaceReferences(datamart);
    List<DiagnosticReport> fhir =
        datamart.stream()
            .map(dm -> R4DiagnosticReportTransformer.builder().datamart(dm).build().toFhir())
            .collect(Collectors.toList());
    return bundle(parameters, fhir, (int) entitiesPage.getTotalElements());
  }

  private Set<DiagnosticReportEntity.CategoryCodes> categoriesFor(TokenParameter categoryToken) {
    return categoryToken
        .behavior()
        .onExplicitSystemAndExplicitCode((s, c) -> datamartCategoryCodesFor(c))
        .onAnySystemAndExplicitCode(this::datamartCategoryCodesFor)
        .onNoSystemAndExplicitCode(this::datamartCategoryCodesFor)
        .onExplicitSystemAndAnyCode(
            s -> {
              if (DR_CATEGORY_SYSTEM.equals(s)) {
                return Set.of(
                    DiagnosticReportEntity.CategoryCodes.CH,
                    DiagnosticReportEntity.CategoryCodes.MB);
              }
              throw new IllegalStateException(
                  "Unsupported Status System: " + s + " Cannot build ExplicitSystemSpecification.");
            })
        .build()
        .execute();
  }

  private Set<String> codesFor(List<TokenParameter> codeTokens) {
    if (codeTokens.isEmpty()) {
      return emptySet();
    }
    return codeTokens.stream()
        .flatMap(
            t ->
                t.behavior().onAnySystemAndExplicitCode(List::of)
                    .onNoSystemAndExplicitCode(List::of)
                    .onExplicitSystemAndExplicitCode(
                        (s, c) -> {
                          // We don't currently support any explicit systems for search by code
                          throw new IllegalStateException(
                              "Unsupported Status System: "
                                  + s
                                  + " Cannot build ExplicitSystemSpecification.");
                        })
                    .onExplicitSystemAndAnyCode(
                        s -> {
                          // We don't currently support any explicit systems for search by code
                          throw new IllegalStateException(
                              "Unsupported Status System: "
                                  + s
                                  + " Cannot build ExplicitSystemSpecification.");
                        })
                    .build().execute().stream())
        .collect(Collectors.toSet());
  }

  /** Determines Datamart CategoryCode(s) based on the fhir category code provided. */
  private Set<DiagnosticReportEntity.CategoryCodes> datamartCategoryCodesFor(String fhirCategory) {
    if ("LAB".equals(fhirCategory)) {
      return Set.of(
          DiagnosticReportEntity.CategoryCodes.CH, DiagnosticReportEntity.CategoryCodes.MB);
    }
    try {
      return Set.of(EnumSearcher.of(DiagnosticReportEntity.CategoryCodes.class).find(fhirCategory));
    } catch (IllegalArgumentException e) {
      log.info(e.getMessage());
      return emptySet();
    }
  }

  private DiagnosticReportEntity findById(String publicId) {
    Optional<DiagnosticReportEntity> entity =
        repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  Pageable page(int page, int count) {
    return PageRequest.of(page - 1, Math.max(count, 1), DiagnosticReportEntity.naturalOrder());
  }

  /** Read resource By PublicId. */
  @GetMapping(value = "/{publicId}")
  public DiagnosticReport read(@PathVariable("publicId") String publicId) {
    DatamartDiagnosticReport dm = findById(publicId).asDatamartDiagnosticReport();
    replaceReferences(List.of(dm));
    return R4DiagnosticReportTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Read raw Datamart resource by PublicId. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    DiagnosticReportEntity entity = findById(publicId);
    IncludesIcnMajig.addHeader(response, entity.icn());
    return entity.payload();
  }

  void replaceReferences(Collection<DatamartDiagnosticReport> resources) {
    witnessProtection.registerAndUpdateReferences(
        resources,
        resource -> Stream.concat(Stream.of(resource.patient()), resource.results().stream()));
  }

  /** Search resource by _id. */
  @GetMapping(params = {"_id"})
  public DiagnosticReport.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchByIdentifier(id, page, count);
  }

  /** Search resource by identifier. */
  @GetMapping(params = {"identifier"})
  public DiagnosticReport.Bundle searchByIdentifier(
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("identifier", identifier)
            .add("page", page)
            .add("_count", count)
            .build();
    DiagnosticReport dr = read(identifier);
    int totalRecords = dr == null ? 0 : 1;
    if (dr == null || page != 1 || count <= 0) {
      return bundle(parameters, emptyList(), totalRecords);
    }
    return bundle(parameters, asList(dr), totalRecords);
  }

  /** Search resource by patient. */
  @GetMapping(params = {"patient"})
  public DiagnosticReport.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build();
    String cdwId = witnessProtection.toCdwId(patient);
    int pageParam = Parameters.pageOf(parameters);
    int countParam = Parameters.countOf(parameters);
    Page<DiagnosticReportEntity> entitiesPage =
        repository.findByIcn(
            cdwId,
            PageRequest.of(
                pageParam - 1,
                countParam == 0 ? 1 : countParam,
                DiagnosticReportEntity.naturalOrder()));
    return bundle(parameters, entitiesPage);
  }

  /** Search resource by patient and category (and date if provided). */
  @GetMapping(params = {"patient", "category"})
  public DiagnosticReport.Bundle searchByPatientAndCategory(
      @RequestParam("patient") String patient,
      @RequestParam("category") String category,
      @RequestParam(value = "date", required = false) @Valid @DateTimeParameter @Size(max = 2)
          String[] date,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    String cdwId = witnessProtection.toCdwId(patient);
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("patient", patient)
            .add("category", category)
            .addAll("date", date)
            .add("page", page)
            .add("_count", count)
            .build();
    TokenParameter categoryToken = TokenParameter.parse(category);
    DiagnosticReportRepository.PatientAndCategoryAndDateSpecification spec =
        DiagnosticReportRepository.PatientAndCategoryAndDateSpecification.builder()
            .patient(cdwId)
            .categories(categoriesFor(categoryToken))
            .dates(date)
            .build();
    Page<DiagnosticReportEntity> entitiesPage = repository.findAll(spec, page(page, count));
    return bundle(parameters, entitiesPage);
  }

  /** Search resources by patient and code (and date if provided). */
  @GetMapping(params = {"patient", "code"})
  public DiagnosticReport.Bundle searchByPatientAndCode(
      @RequestParam("patient") String patient,
      @RequestParam("code") String codeCsv,
      @RequestParam(value = "date", required = false) @Valid @DateTimeParameter @Size(max = 2)
          String[] date,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    String cdwId = witnessProtection.toCdwId(patient);
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("patient", patient)
            .add("code", codeCsv)
            .addAll("date", date)
            .add("page", page)
            .add("_count", count)
            .build();
    List<TokenParameter> codeTokens =
        Splitter.on(",").trimResults().splitToList(codeCsv).stream()
            .filter(c -> !"".equals(c))
            .map(TokenParameter::parse)
            .filter(t -> !t.isCodeExplicitAndUnsupported("panel") || !t.hasExplicitlyNoSystem())
            .collect(Collectors.toList());
    DiagnosticReportRepository.PatientAndCodeAndDateSpecification spec =
        DiagnosticReportRepository.PatientAndCodeAndDateSpecification.builder()
            .patient(cdwId)
            .codes(codesFor(codeTokens))
            .dates(date)
            .build();
    Page<DiagnosticReportEntity> entitiesPage = repository.findAll(spec, page(page, count));
    return bundle(parameters, entitiesPage);
  }

  /** Search resource by patient and status. */
  @GetMapping(params = {"patient", "status"})
  public DiagnosticReport.Bundle searchByPatientAndStatus(
      @RequestParam("patient") String patient,
      @RequestParam("status") String statusCsv,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    String cdwId = witnessProtection.toCdwId(patient);
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("patient", patient)
            .add("status", statusCsv)
            .add("page", page)
            .add("_count", count)
            .build();
    // The status for all diagnosticReports returned will be 'final'
    // (see R4DiagnosticReportTransformer) if any other status code is
    // requested, return an empty bundle
    List<TokenParameter> statusTokens =
        Splitter.on(",").trimResults().splitToList(statusCsv).stream()
            .map(TokenParameter::parse)
            .filter(
                t ->
                    !t.isSystemExplicitAndUnsupported(DR_STATUS_SYSTEM)
                        || !t.isCodeExplicitAndUnsupported("final")
                        || !t.hasExplicitlyNoSystem())
            .collect(Collectors.toList());
    if (statusTokens.isEmpty() || !statusFinal(statusTokens)) {
      return bundle(parameters, emptyList(), 0);
    }
    int pageParam = Parameters.pageOf(parameters);
    int countParam = Parameters.countOf(parameters);
    Page<DiagnosticReportEntity> entitiesPage =
        repository.findByIcn(
            cdwId,
            PageRequest.of(
                pageParam - 1,
                countParam == 0 ? 1 : countParam,
                DiagnosticReportEntity.naturalOrder()));
    return bundle(parameters, entitiesPage);
  }

  private Boolean statusFinal(List<TokenParameter> codeTokens) {
    return codeTokens.stream()
        .flatMap(
            t ->
                t.behavior().onExplicitSystemAndExplicitCode((s, c) -> List.of(c))
                    .onAnySystemAndExplicitCode(List::of).onNoSystemAndExplicitCode(List::of)
                    .onExplicitSystemAndAnyCode(
                        s -> {
                          if (DR_STATUS_SYSTEM.equals(s)) {
                            return List.of("final");
                          }
                          throw new IllegalStateException(
                              "Unsupported Status System: "
                                  + s
                                  + " Cannot build ExplicitSystemSpecification.");
                        })
                    .build().execute().stream())
        .distinct()
        .anyMatch("final"::equals);
  }
}
