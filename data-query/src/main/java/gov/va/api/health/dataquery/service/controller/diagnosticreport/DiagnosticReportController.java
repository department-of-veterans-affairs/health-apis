package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.api.resources.DiagnosticReport.Bundle;
import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.DateTimeParameter;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Request Mappings for Diagnostic Report Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-diagnosticreport.html for
 * implementation details.
 */
@Slf4j
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
  value = {"/api/DiagnosticReport"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class DiagnosticReportController {
  // PETERTODO should inject witness protection as a component instead
  private IdentityService identityService;

  private Transformer transformer;

  private MrAndersonClient mrAndersonClient;

  private Bundler bundler;

  private DiagnosticReportCrudRepository repo;

  private Bundle bundle(MultiValueMap<String, String> parameters, int page, int count) {
    CdwDiagnosticReport102Root cdwRoot = search(parameters);
    final List<CdwDiagnosticReport> reports =
        cdwRoot.getDiagnosticReports() == null
            ? Collections.emptyList()
            : cdwRoot.getDiagnosticReports().getDiagnosticReport();
    return bundle(parameters, page, count, cdwRoot.getRecordCount().intValue(), reports);
  }

  private Bundle bundle(
      MultiValueMap<String, String> parameters,
      int page,
      int count,
      int totalRecords,
      List<CdwDiagnosticReport> xmlReports) {
    LinkConfig linkConfig =
        LinkConfig.builder()
            .path("DiagnosticReport")
            .queryParams(parameters)
            .page(page)
            .recordsPerPage(count)
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        BundleContext.of(
            linkConfig,
            xmlReports == null ? Collections.emptyList() : xmlReports,
            transformer,
            DiagnosticReport.Entry::new,
            DiagnosticReport.Bundle::new));
  }

  @SneakyThrows
  private Bundle jpaBundle(
      String id, MultiValueMap<String, String> parameters, int page, int count) {
    log.error("original parameters: {}", parameters);
    MultiValueMap<String, String> protectedParameters =
        WitnessProtection.replacePublicIdsWithCdwIds(identityService, parameters);
    log.error("witness-protected parameters: {}", protectedParameters);
    log.error("Sanity check, diagnostic report count is {}", repo.count());
    // PETERTODO do a JPQL query on id, page, count
    // PETERTODO use paging and sorting repository
    Optional<DiagnosticReportEntity> entity = repo.findById(1L);
    log.error("Diagnostic report for id {} is {}", 1L, entity);
    String taggedReport = "<diagnosticReport>" + entity.get().document() + "</diagnosticReport>";
    String allReports = "<diagnosticReports>" + taggedReport + "</diagnosticReports>";
    String rootDocument = "<root>" + allReports + "</root>";
    try (Reader reader = new StringReader(rootDocument)) {
      JAXBContext jaxbContext = JAXBContext.newInstance(CdwDiagnosticReport102Root.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      CdwDiagnosticReport102Root sampleReports =
          (CdwDiagnosticReport102Root) jaxbUnmarshaller.unmarshal(reader);

      //           Document xml = parse(originalQuery, originalXml);
      //
      // XmlResponseValidator.builder().query(originalQuery).response(xml).build().validate();
      //           xml = replaceCdwIdsWithPublicIds(originalQuery, xml);
      //           return write(query, xml);

      return bundle(
          parameters,
          page,
          count,
          sampleReports.getDiagnosticReports().getDiagnosticReport().size(),
          sampleReports.getDiagnosticReports().getDiagnosticReport());
    }
  }

  /** Read by identifier. */
  @GetMapping(value = {"/{publicId}"})
  public DiagnosticReport read(@PathVariable("publicId") String publicId) {
    return transformer.apply(
        firstPayloadItem(
            hasPayload(search(Parameters.forIdentity(publicId)).getDiagnosticReports())
                .getDiagnosticReport()));
  }

  private CdwDiagnosticReport102Root search(MultiValueMap<String, String> params) {
    Query<CdwDiagnosticReport102Root> query =
        Query.forType(CdwDiagnosticReport102Root.class)
            .profile(Query.Profile.ARGONAUT)
            .resource("DiagnosticReport")
            .version("1.02")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Search by _id. */
  @SneakyThrows
  @GetMapping(params = {"_id"})
  public DiagnosticReport.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build();
    Bundle mrAndersonBundle = bundle(parameters, page, count);
    log.error(
        "mr-anderson bundle is {}",
        JacksonConfig.createMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(mrAndersonBundle));

    Bundle jpaBundle = jpaBundle(id, parameters, page, count);
    log.error(
        "JPA bundle is {}",
        JacksonConfig.createMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(jpaBundle));

    return mrAndersonBundle;
  }

  /** Search by identifier. */
  @GetMapping(params = {"identifier"})
  public DiagnosticReport.Bundle searchByIdentifier(
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return bundle(
        Parameters.builder()
            .add("identifier", identifier)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public DiagnosticReport.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return bundle(
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Search by Patient+Category. */
  @GetMapping(params = {"patient", "category"})
  public DiagnosticReport.Bundle searchByPatientAndCategory(
      @RequestParam("patient") String patient,
      @RequestParam("category") String category,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return bundle(
        Parameters.builder()
            .add("patient", patient)
            .add("category", category)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Search by Patient+Category+Date. */
  @GetMapping(params = {"patient", "category", "date"})
  public DiagnosticReport.Bundle searchByPatientAndCategoryAndDate(
      @RequestParam("patient") String patient,
      @RequestParam("category") String category,
      @RequestParam(value = "date", required = false) @Valid @DateTimeParameter @Size(max = 2)
          String[] date,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return bundle(
        Parameters.builder()
            .add("patient", patient)
            .add("category", category)
            .addAll("date", date)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Search by Patient+Code. */
  @GetMapping(params = {"patient", "code"})
  public DiagnosticReport.Bundle searchByPatientAndCode(
      @RequestParam("patient") String patient,
      @RequestParam("code") String code,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return bundle(
        Parameters.builder()
            .add("patient", patient)
            .add("code", code)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Validate Endpoint. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Bundle bundle) {
    return Validator.create().validate(bundle);
  }

  public interface Transformer extends Function<CdwDiagnosticReport, DiagnosticReport> {}
}
