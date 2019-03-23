package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.DateTimeParameter;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.XmlDocuments;
import gov.va.api.health.dataquery.service.controller.XmlDocuments.WriteFailed;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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
import org.w3c.dom.Document;

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

  private EntityManager entityManager;

  @SneakyThrows
  private DiagnosticReport.Bundle bothWays(
      MultiValueMap<String, String> parameters, int page, int count) {
    DiagnosticReport.Bundle jpaBundle = jpaBundle(parameters, page, count);
    DiagnosticReport.Bundle mrAndersonBundle = bundle(parameters, page, count);
    if (!jpaBundle.equals(mrAndersonBundle)) {
      log.error("JPA bundle and mr-anderson bundle do not match");
      log.error(
          "JPA bundle is {}",
          JacksonConfig.createMapper()
              .writerWithDefaultPrettyPrinter()
              .writeValueAsString(jpaBundle));
      log.error(
          "mr-anderson bundle is {}",
          JacksonConfig.createMapper()
              .writerWithDefaultPrettyPrinter()
              .writeValueAsString(mrAndersonBundle));
    }
    return mrAndersonBundle;
  }

  private DiagnosticReport.Bundle bundle(
      MultiValueMap<String, String> parameters, int page, int count) {
    CdwDiagnosticReport102Root cdwRoot = search(parameters);
    final List<CdwDiagnosticReport> reports =
        cdwRoot.getDiagnosticReports() == null
            ? Collections.emptyList()
            : cdwRoot.getDiagnosticReports().getDiagnosticReport();
    return bundle(parameters, page, count, cdwRoot.getRecordCount().intValue(), reports);
  }

  private DiagnosticReport.Bundle bundle(
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
  private DiagnosticReport.Bundle jpaBundle(
      MultiValueMap<String, String> parameters, int page, int count) {
    log.error("original parameters: {}", parameters);
    MultiValueMap<String, String> protectedParameters =
        WitnessProtection.replacePublicIdsWithCdwIds(identityService, parameters);
    log.error("witness-protected parameters: {}", protectedParameters);

    TypedQuery<Long> totalQuery =
        entityManager.createQuery(queryTotal(protectedParameters), Long.class);
    queryAddParameters(totalQuery, protectedParameters);
    int totalRecords = totalQuery.getSingleResult().intValue();
    log.error("total records: " + totalRecords);

    TypedQuery<DiagnosticReportEntity> jpqlQuery =
        entityManager.createQuery(query(protectedParameters), DiagnosticReportEntity.class);
    jpqlQuery.setFirstResult((page - 1) * count);
    jpqlQuery.setMaxResults(count);
    queryAddParameters(jpqlQuery, protectedParameters);
    List<DiagnosticReportEntity> entities = jpqlQuery.getResultList();

    log.error(
        "Diagnostic reports for identifier {} are {}",
        protectedParameters.getFirst("identifier"),
        entities);

    String taggedReports =
        entities
            .stream()
            .map(entity -> "<diagnosticReport>" + entity.document() + "</diagnosticReport>")
            .collect(Collectors.joining());
    String allReports = "<diagnosticReports>" + taggedReports + "</diagnosticReports>";
    String rootDocument = "<root>" + allReports + "</root>";

    Document xml = WitnessProtection.parse(parameters, rootDocument);
    // XmlResponseValidator.builder().parameters(parameters).response(xml).build().validate();
    xml =
        WitnessProtection.replaceCdwIdsWithPublicIds(
            identityService, "DiagnosticReport", parameters, xml);
    String protectedDocument = write(parameters, xml);

    try (Reader reader = new StringReader(protectedDocument)) {
      JAXBContext jaxbContext = JAXBContext.newInstance(CdwDiagnosticReport102Root.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      CdwDiagnosticReport102Root sampleReports =
          (CdwDiagnosticReport102Root) jaxbUnmarshaller.unmarshal(reader);
      return bundle(
          parameters,
          page,
          count,
          totalRecords,
          sampleReports.getDiagnosticReports().getDiagnosticReport());
    }
  }

  private String query(MultiValueMap<String, String> parameters) {
    if (parameters.containsKey("identifier")) {
      return "Select dr from DiagnosticReportEntity dr where dr.identifier is :identifier";
    } else if (parameters.containsKey("patient")) {
      return "Select dr from DiagnosticReportEntity dr where dr.patientId is :patient";
    } else {
      throw new IllegalArgumentException("Cannot determine query");
    }
  }

  private void queryAddParameters(TypedQuery<?> query, MultiValueMap<String, String> parameters) {
    if (parameters.containsKey("identifier")) {
      query.setParameter("identifier", parameters.getFirst("identifier"));
    }
    if (parameters.containsKey("patient")) {
      query.setParameter("patient", Long.valueOf(parameters.getFirst("patient")));
    }
  }

  private String queryTotal(MultiValueMap<String, String> parameters) {
    if (parameters.containsKey("identifier")) {
      return "Select count(dr.id) from DiagnosticReportEntity dr where dr.identifier is :identifier";
    } else if (parameters.containsKey("patient")) {
      return "Select count(dr.id) from DiagnosticReportEntity dr where dr.patientId is :patient";
    } else {
      throw new IllegalArgumentException("Cannot determine total-query");
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
  @GetMapping(params = {"_id"})
  public DiagnosticReport.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return searchByIdentifier(id, page, count);
  }

  /** Search by identifier. */
  @GetMapping(params = {"identifier"})
  public DiagnosticReport.Bundle searchByIdentifier(
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("identifier", identifier)
            .add("page", page)
            .add("_count", count)
            .build();
    return bothWays(parameters, page, count);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public DiagnosticReport.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build();
    return bothWays(parameters, page, count);
  }

  /** Search by Patient+Category. */
  @GetMapping(params = {"patient", "category"})
  public DiagnosticReport.Bundle searchByPatientAndCategory(
      @RequestParam("patient") String patient,
      @RequestParam("category") String category,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    // PETERTODO
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
    // PETERTODO
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
    // PETERTODO
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
  public OperationOutcome validate(@RequestBody DiagnosticReport.Bundle bundle) {
    return Validator.create().validate(bundle);
  }

  private String write(MultiValueMap<String, String> parameters, Document xml) {
    try {
      return XmlDocuments.create().write(xml);
    } catch (WriteFailed e) {
      log.error("Failed to write XML: {}", e.getMessage());
      throw new WitnessProtection.SearchFailed(parameters, e);
    }
  }

  public interface Transformer extends Function<CdwDiagnosticReport, DiagnosticReport> {}
}
