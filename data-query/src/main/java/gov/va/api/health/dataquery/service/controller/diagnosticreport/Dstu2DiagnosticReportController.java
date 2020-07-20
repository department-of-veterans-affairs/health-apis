package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.DateTimeParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Diagnostic Report Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-diagnosticreport.html for
 * implementation details.
 */
@Slf4j
@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
    value = {"/dstu2/DiagnosticReport"},
    produces = {"application/json", "application/json+fhir", "application/fhir+json"})
public class Dstu2DiagnosticReportController {
  private Dstu2Bundler bundler;

  private WitnessProtection witnessProtection;

  private DiagnosticReportRepository repository;

  private EntityManager entityManager;

  /** All args constructor. */
  public Dstu2DiagnosticReportController(
      @Autowired Dstu2Bundler bundler,
      @Autowired WitnessProtection witnessProtection,
      @Autowired DiagnosticReportRepository repository,
      @Autowired EntityManager entityManager) {
    this.bundler = bundler;
    this.witnessProtection = witnessProtection;
    this.repository = repository;
    this.entityManager = entityManager;
  }

  private DiagnosticReport.Bundle bundle(
      MultiValueMap<String, String> parameters, List<DiagnosticReport> reports, int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("DiagnosticReport")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        Dstu2Bundler.BundleContext.of(
            linkConfig, reports, DiagnosticReport.Entry::new, DiagnosticReport.Bundle::new));
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
            .map(dm -> Dstu2DiagnosticReportTransformer.builder().datamart(dm).build().toFhir())
            .collect(Collectors.toList());
    return bundle(parameters, fhir, (int) entitiesPage.getTotalElements());
  }

  private DiagnosticReportEntity findById(String publicId) {
    Optional<DiagnosticReportEntity> entity =
        repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  Pageable page(int page, int count) {
    return PageRequest.of(page - 1, Math.max(count, 1), DiagnosticReportEntity.naturalOrder());
  }

  /** Read by identifier. */
  @GetMapping(value = {"/{publicId}"})
  public DiagnosticReport read(
      @RequestHeader(name = "v2", defaultValue = "false") Boolean v2,
      @PathVariable("publicId") String publicId) {
    if (Boolean.TRUE.equals(v2)) {
      log.info("Using v2 DiagnosticReport.");
      DatamartDiagnosticReport dm = findById(publicId).asDatamartDiagnosticReport();
      replaceReferences(List.of(dm));
      return Dstu2DiagnosticReportTransformer.builder().datamart(dm).build().toFhir();
    }
    return v1Controller().read(publicId);
  }

  /** Return the raw Datamart document for the given identifier. */
  @SneakyThrows
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(
      @RequestHeader(name = "v2", defaultValue = "false") Boolean v2,
      @PathVariable("publicId") String publicId,
      HttpServletResponse response) {
    if (Boolean.TRUE.equals(v2)) {
      log.info("Using v2 DiagnosticReport.");
      DiagnosticReportEntity entity = findById(publicId);
      IncludesIcnMajig.addHeader(response, entity.icn());
      return entity.payload();
    }
    return JacksonConfig.createMapper()
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(v1Controller().readRaw(publicId, response));
  }

  void replaceReferences(Collection<DatamartDiagnosticReport> resources) {
    witnessProtection.registerAndUpdateReferences(
        resources,
        resource ->
            Stream.concat(
                Stream.of(resource.patient(), resource.accessionInstitution().orElse(null)),
                resource.results().stream()));
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public DiagnosticReport.Bundle searchById(
      @RequestHeader(name = "v2", defaultValue = "false") Boolean v2,
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchByIdentifier(v2, id, page, count);
  }

  /** Search by identifier. */
  @GetMapping(params = {"identifier"})
  public DiagnosticReport.Bundle searchByIdentifier(
      @RequestHeader(name = "v2", defaultValue = "false") Boolean v2,
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (Boolean.TRUE.equals(v2)) {
      log.info("Using v2 DiagnosticReport.");
      MultiValueMap<String, String> parameters =
          Parameters.builder()
              .add("identifier", identifier)
              .add("page", page)
              .add("_count", count)
              .build();
      DiagnosticReport dr = read(true, identifier);
      int totalRecords = dr == null ? 0 : 1;
      if (dr == null || page != 1 || count <= 0) {
        return bundle(parameters, emptyList(), totalRecords);
      }
      return bundle(parameters, asList(dr), totalRecords);
    }
    return v1Controller().searchByIdentifier(identifier, page, count);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public DiagnosticReport.Bundle searchByPatient(
      @RequestHeader(name = "v2", defaultValue = "false") Boolean v2,
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (Boolean.TRUE.equals(v2)) {
      log.info("Using v2 DiagnosticReport.");
      MultiValueMap<String, String> parameters =
          Parameters.builder()
              .add("patient", patient)
              .add("page", page)
              .add("_count", count)
              .build();
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
    return v1Controller().searchByPatient(patient, page, count);
  }

  /** Search by Patient+Category + Date if provided. */
  @GetMapping(params = {"patient", "category"})
  public DiagnosticReport.Bundle searchByPatientAndCategory(
      @RequestHeader(name = "v2", defaultValue = "false") Boolean v2,
      @RequestParam("patient") String patient,
      @RequestParam("category") String category,
      @RequestParam(value = "date", required = false) @Valid @DateTimeParameter @Size(max = 2)
          String[] date,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (Boolean.TRUE.equals(v2)) {
      log.info("Using v2 DiagnosticReport.");
      String cdwId = witnessProtection.toCdwId(patient);
      MultiValueMap<String, String> parameters =
          Parameters.builder()
              .add("patient", patient)
              .add("category", category)
              .addAll("date", date)
              .add("page", page)
              .add("_count", count)
              .build();
      DiagnosticReportRepository.PatientAndCategoryAndDateSpecification spec =
          DiagnosticReportRepository.PatientAndCategoryAndDateSpecification.builder()
              .patient(cdwId)
              .category(category)
              .dates(date)
              .build();
      Page<DiagnosticReportEntity> entitiesPage = repository.findAll(spec, page(page, count));
      return bundle(parameters, entitiesPage);
    }
    return v1Controller().searchByPatientAndCategoryAndDate(patient, category, date, page, count);
  }

  /** Search by Patient+Code. */
  @GetMapping(params = {"patient", "code"})
  public DiagnosticReport.Bundle searchByPatientAndCode(
      @RequestHeader(name = "v2", defaultValue = "false") Boolean v2,
      @RequestParam("patient") String patient,
      @RequestParam("code") String code,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (Boolean.TRUE.equals(v2)) {
      log.info("Using v2 DiagnosticReport.");
      String cdwId = witnessProtection.toCdwId(patient);
      MultiValueMap<String, String> parameters =
          Parameters.builder()
              .add("patient", patient)
              .add("code", code)
              .add("page", page)
              .add("_count", count)
              .build();
      DiagnosticReportRepository.PatientAndCodeSpecification spec =
          DiagnosticReportRepository.PatientAndCodeSpecification.builder()
              .patient(cdwId)
              .code(isBlank(code) ? null : code)
              .build();
      Page<DiagnosticReportEntity> entitiesPage = repository.findAll(spec, page(page, count));
      return bundle(parameters, entitiesPage);
    }
    return v1Controller().searchByPatientAndCode(patient, code, page, count);
  }

  /** Search by patient. Raw Reads ONLY in V2. */
  @GetMapping(
      value = "/raw",
      params = {"patient"})
  public String searchByPatientRaw(
      @RequestParam("patient") String patient, HttpServletResponse response) {
    return v1Controller().searchByPatientRaw(patient, response);
  }

  private gov.va.api.health.dataquery.service.controller.diagnosticreport.v1
          .Dstu2DiagnosticReportController
      v1Controller() {
    return new gov.va.api.health.dataquery.service.controller.diagnosticreport.v1
        .Dstu2DiagnosticReportController(bundler, witnessProtection, entityManager);
  }
}
