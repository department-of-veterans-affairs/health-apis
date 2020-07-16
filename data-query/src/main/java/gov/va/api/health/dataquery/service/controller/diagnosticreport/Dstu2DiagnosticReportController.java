package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static java.util.Collections.emptyList;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.DateTimeParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.v1.DatamartDiagnosticReports;
import java.util.List;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
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
@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
    value = {"/dstu2/DiagnosticReport"},
    produces = {"application/json", "application/json+fhir", "application/fhir+json"})
public class Dstu2DiagnosticReportController {
  private Dstu2Bundler bundler;

  private WitnessProtection witnessProtection;

  private EntityManager entityManager;

  /** All args constructor. */
  public Dstu2DiagnosticReportController(
      @Autowired Dstu2Bundler bundler,
      @Autowired WitnessProtection witnessProtection,
      @Autowired EntityManager entityManager) {
    this.bundler = bundler;
    this.witnessProtection = witnessProtection;
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

  /** Read by identifier. */
  @GetMapping(value = {"/{publicId}"})
  public DiagnosticReport read(
      @RequestHeader(name = "v2", defaultValue = "false") Boolean v2,
      @PathVariable("publicId") String publicId) {
    if (v2) {
      return DiagnosticReport.builder().build();
    }
    return v1Controller().read(publicId);
  }

  /** Return the raw Datamart document for the given identifier. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public DatamartDiagnosticReports.DiagnosticReport readRaw(
      @RequestHeader(name = "v2", defaultValue = "false") Boolean v2,
      @PathVariable("publicId") String publicId,
      HttpServletResponse response) {
    if (v2) {
      return DatamartDiagnosticReports.DiagnosticReport.builder().build();
    }
    return v1Controller().readRaw(publicId, response);
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public DiagnosticReport.Bundle searchById(
      @RequestHeader(name = "v2", defaultValue = "false") Boolean v2,
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (v2) {
      MultiValueMap<String, String> parameters =
          Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build();
      return bundle(parameters, emptyList(), 0);
    }
    return v1Controller().searchById(id, page, count);
  }

  /** Search by identifier. */
  @GetMapping(params = {"identifier"})
  public DiagnosticReport.Bundle searchByIdentifier(
      @RequestHeader(name = "v2", defaultValue = "false") Boolean v2,
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (v2) {
      return searchById(true, identifier, page, count);
    }
    return searchById(false, identifier, page, count);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public DiagnosticReport.Bundle searchByPatient(
      @RequestHeader(name = "v2", defaultValue = "false") Boolean v2,
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (v2) {
      MultiValueMap<String, String> parameters =
          Parameters.builder()
              .add("patient", patient)
              .add("page", page)
              .add("_count", count)
              .build();
      return bundle(parameters, emptyList(), 0);
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
    if (v2) {
      MultiValueMap<String, String> parameters =
          Parameters.builder()
              .add("patient", patient)
              .add("category", category)
              .addAll("date", date)
              .add("page", page)
              .add("_count", count)
              .build();
      return bundle(parameters, emptyList(), 0);
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
    if (v2) {
      MultiValueMap<String, String> parameters =
          Parameters.builder()
              .add("patient", patient)
              .add("code", code)
              .add("page", page)
              .add("_count", count)
              .build();
      return bundle(parameters, emptyList(), 0);
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
