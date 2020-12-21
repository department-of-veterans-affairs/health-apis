package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import com.google.common.base.Splitter;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.DateTimeParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportEntity.CategoryCode;
import gov.va.api.health.dstu2.api.resources.DiagnosticReport;
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
import lombok.SneakyThrows;
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
 * Request Mappings for Diagnostic Report Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-diagnosticreport.html for
 * implementation details.
 */
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

  /** All args constructor. */
  public Dstu2DiagnosticReportController(
      @Autowired Dstu2Bundler bundler,
      @Autowired WitnessProtection witnessProtection,
      @Autowired DiagnosticReportRepository repository) {
    this.bundler = bundler;
    this.witnessProtection = witnessProtection;
    this.repository = repository;
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
  public DiagnosticReport read(@PathVariable("publicId") String publicId) {
    DatamartDiagnosticReport dm = findById(publicId).asDatamartDiagnosticReport();
    replaceReferences(List.of(dm));
    return Dstu2DiagnosticReportTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Return the raw Datamart document for the given identifier. */
  @SneakyThrows
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
        resource ->
            Stream.concat(
                Stream.of(resource.patient(), resource.accessionInstitution().orElse(null)),
                resource.results().stream()));
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public DiagnosticReport.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchByIdentifier(id, page, count);
  }

  /** Search by identifier. */
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

  /** Search by patient. */
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

  /** Search by Patient+Category + Date if provided. */
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
    DiagnosticReportRepository.PatientAndCategoryAndDateSpecification spec =
        DiagnosticReportRepository.PatientAndCategoryAndDateSpecification.builder()
            .patient(cdwId)
            .categories(CategoryCode.forFhirCategory(category))
            .dates(date)
            .build();
    Page<DiagnosticReportEntity> entitiesPage = repository.findAll(spec, page(page, count));
    return bundle(parameters, entitiesPage);
  }

  /** Search by Patient+Code. */
  @GetMapping(params = {"patient", "code"})
  public DiagnosticReport.Bundle searchByPatientAndCode(
      @RequestParam("patient") String patient,
      @RequestParam("code") String codeCsv,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    String cdwId = witnessProtection.toCdwId(patient);
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("patient", patient)
            .add("code", codeCsv)
            .add("page", page)
            .add("_count", count)
            .build();
    Set<String> codes =
        Splitter.on(",").trimResults().splitToList(codeCsv).stream()
            .filter(c -> !"".equals(c))
            .collect(Collectors.toSet());
    DiagnosticReportRepository.PatientAndCodeAndDateSpecification spec =
        DiagnosticReportRepository.PatientAndCodeAndDateSpecification.builder()
            .patient(cdwId)
            .codes(codes)
            .build();
    Page<DiagnosticReportEntity> entitiesPage = repository.findAll(spec, page(page, count));
    return bundle(parameters, entitiesPage);
  }
}
