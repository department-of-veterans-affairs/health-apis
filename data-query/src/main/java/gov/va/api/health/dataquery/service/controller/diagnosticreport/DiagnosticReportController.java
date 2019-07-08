package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;
import static gov.va.api.health.dataquery.service.controller.Transformers.parseLocalDateTime;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.DateTimeParameter;
import gov.va.api.health.dataquery.service.controller.DateTimeParameters;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
@AllArgsConstructor(onConstructor = @__({@Autowired}))
@RequestMapping(
  value = {"DiagnosticReport", "/api/DiagnosticReport"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
public class DiagnosticReportController {
  private Transformer transformer;

  private MrAndersonClient mrAndersonClient;

  private Bundler bundler;

  private WitnessProtection witnessProtection;

  private EntityManager entityManager;

  private static boolean datesAreSatisfied(
      DatamartDiagnosticReports.DiagnosticReport report, List<DateTimeParameters> parameters) {
    for (DateTimeParameters parameter : parameters) {
      LocalDateTime lowerLocal = parseLocalDateTime(report.effectiveDateTime());
      LocalDateTime upperLocal = parseLocalDateTime(report.issuedDateTime());
      if (lowerLocal == null && upperLocal == null) {
        return false;
      }

      ZonedDateTime lower =
          lowerLocal == null
              ? upperLocal.atZone(ZoneId.of("Z"))
              : lowerLocal.atZone(ZoneId.of("Z"));
      ZonedDateTime upper =
          upperLocal == null
              ? lowerLocal.atZone(ZoneId.of("Z"))
              : upperLocal.atZone(ZoneId.of("Z"));

      if (!parameter.isSatisfied(
          lower.toInstant().toEpochMilli(), upper.toInstant().toEpochMilli())) {
        return false;
      }
    }

    return true;
  }

  private static List<DatamartDiagnosticReports.DiagnosticReport> filterDates(
      List<DatamartDiagnosticReports.DiagnosticReport> datamartReports,
      List<String> dateParameters) {
    if (isEmpty(dateParameters)) {
      return datamartReports;
    }

    List<DateTimeParameters> parameters =
        dateParameters
            .stream()
            .map(date -> new DateTimeParameters(date))
            .collect(Collectors.toList());

    return datamartReports
        .stream()
        .filter(r -> datesAreSatisfied(r, parameters))
        .collect(Collectors.toList());
  }

  private DiagnosticReport.Bundle bundle(
      MultiValueMap<String, String> parameters, List<DiagnosticReport> reports, int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("DiagnosticReport")
            .queryParams(parameters)
            .page(Integer.parseInt(parameters.getOrDefault("page", asList("1")).get(0)))
            .recordsPerPage(
                Integer.parseInt(parameters.getOrDefault("_count", asList("15")).get(0)))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        Bundler.BundleContext.of(
            linkConfig,
            reports,
            Function.identity(),
            DiagnosticReport.Entry::new,
            DiagnosticReport.Bundle::new));
  }

  @SneakyThrows
  private DiagnosticReport.Bundle datamartBundle(MultiValueMap<String, String> publicParameters) {
    MultiValueMap<String, String> cdwParameters =
        witnessProtection.replacePublicIdsWithCdwIds(publicParameters);

    // Filter category
    String category = cdwParameters.getFirst("category");
    if (isNotBlank(category) && !equalsIgnoreCase(category, "LAB")) {
      // All diagnostic reports are labs
      return bundle(publicParameters, emptyList(), 0);
    }

    // Filter code
    String code = cdwParameters.getFirst("code");
    if (isNotBlank(code)) {
      // LOINC codes are not available in CDW
      return bundle(publicParameters, emptyList(), 0);
    }

    DiagnosticReportEntity entity =
        entityManager.find(DiagnosticReportEntity.class, cdwParameters.getFirst("patient"));
    if (entity == null) {
      return bundle(publicParameters, emptyList(), 0);
    }

    DatamartDiagnosticReports payload = entity.asDatamartDiagnosticReports();
    List<DatamartDiagnosticReports.DiagnosticReport> filtered =
        filterDates(payload.reports(), cdwParameters.get("date"));

    // Most recent reports first
    Collections.sort(
        filtered,
        (left, right) -> {
          int result = StringUtils.compare(right.effectiveDateTime(), left.effectiveDateTime());
          if (result != 0) {
            return result;
          }
          return StringUtils.compare(right.issuedDateTime(), left.issuedDateTime());
        });

    int page = Integer.parseInt(cdwParameters.getOrDefault("page", asList("1")).get(0));
    int count = Integer.parseInt(cdwParameters.getOrDefault("_count", asList("15")).get(0));

    int fromIndex = Math.min((page - 1) * count, filtered.size());
    int toIndex = Math.min(fromIndex + count, filtered.size());
    List<DatamartDiagnosticReports.DiagnosticReport> paged = filtered.subList(fromIndex, toIndex);

    replaceCdwIdsWithPublicIds(paged);

    List<DiagnosticReport> fhir =
        paged
            .stream()
            .map(
                dm ->
                    DatamartDiagnosticReportTransformer.builder()
                        .datamart(dm)
                        .icn(payload.fullIcn())
                        .patientName(payload.patientName())
                        .build()
                        .toFhir())
            .collect(Collectors.toList());

    return bundle(publicParameters, fhir, filtered.size());
  }

  @SneakyThrows
  private DiagnosticReport datamartRead(String publicId) {
    MultiValueMap<String, String> publicParameters = Parameters.forIdentity(publicId);
    MultiValueMap<String, String> cdwParameters =
        witnessProtection.replacePublicIdsWithCdwIds(publicParameters);
    String cdwReportId = cdwParameters.getFirst("identifier");

    DiagnosticReportPatientEntity crossEntity =
        entityManager.find(DiagnosticReportPatientEntity.class, cdwReportId);
    if (crossEntity == null) {
      return null;
    }

    DiagnosticReportEntity entity =
        entityManager.find(DiagnosticReportEntity.class, crossEntity.icn());
    if (entity == null) {
      return null;
    }

    DatamartDiagnosticReports payload = entity.asDatamartDiagnosticReports();
    Optional<DatamartDiagnosticReports.DiagnosticReport> maybeReport =
        payload
            .reports()
            .stream()
            .filter(r -> StringUtils.equals(r.identifier(), cdwReportId))
            .findFirst();
    if (!maybeReport.isPresent()) {
      return null;
    }

    DatamartDiagnosticReports.DiagnosticReport report = maybeReport.get();

    replaceCdwIdsWithPublicIds(asList(report));

    return DatamartDiagnosticReportTransformer.builder()
        .datamart(report)
        .icn(payload.fullIcn())
        .patientName(payload.patientName())
        .build()
        .toFhir();
  }

  private DiagnosticReport.Bundle mrAndersonBundle(MultiValueMap<String, String> parameters) {
    CdwDiagnosticReport102Root root = mrAndersonSearch(parameters);
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("DiagnosticReport")
            .queryParams(parameters)
            .page(Integer.parseInt(parameters.getOrDefault("page", asList("1")).get(0)))
            .recordsPerPage(
                Integer.parseInt(parameters.getOrDefault("_count", asList("15")).get(0)))
            .totalRecords(root.getRecordCount().intValue())
            .build();
    return bundler.bundle(
        Bundler.BundleContext.of(
            linkConfig,
            root.getDiagnosticReports() == null
                ? Collections.emptyList()
                : root.getDiagnosticReports().getDiagnosticReport(),
            transformer,
            DiagnosticReport.Entry::new,
            DiagnosticReport.Bundle::new));
  }

  private CdwDiagnosticReport102Root mrAndersonSearch(MultiValueMap<String, String> params) {
    Query<CdwDiagnosticReport102Root> query =
        Query.forType(CdwDiagnosticReport102Root.class)
            .profile(Query.Profile.ARGONAUT)
            .resource("DiagnosticReport")
            .version("1.02")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Read by identifier. */
  @SneakyThrows
  @GetMapping(value = {"/{publicId}"})
  public DiagnosticReport read(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamart,
      @PathVariable("publicId") String publicId) {
    if (BooleanUtils.isTrue(BooleanUtils.toBooleanObject(datamart))) {
      return datamartRead(publicId);
    }

    StopWatch mraWatch = StopWatch.createStarted();
    DiagnosticReport mrAndersonReport =
        transformer.apply(
            firstPayloadItem(
                hasPayload(
                        mrAndersonSearch(Parameters.forIdentity(publicId)).getDiagnosticReports())
                    .getDiagnosticReport()));
    mraWatch.stop();

    if ("both".equalsIgnoreCase(datamart)) {
      StopWatch datamartWatch = StopWatch.createStarted();
      DiagnosticReport datamartReport = datamartRead(publicId);
      datamartWatch.stop();
      log.info(
          "mr-anderson took {} millis and datamart took {} millis."
              + " mr-anderson is {} and datamart is {}",
          mraWatch.getTime(),
          datamartWatch.getTime(),
          JacksonConfig.createMapper().writeValueAsString(mrAndersonReport),
          JacksonConfig.createMapper().writeValueAsString(datamartReport));
    }

    return mrAndersonReport;
  }

  private void replaceCdwIdsWithPublicIds(
      List<DatamartDiagnosticReports.DiagnosticReport> reports) {
    Set<ResourceIdentity> ids =
        reports
            .stream()
            .flatMap(
                report ->
                    Stream.of(
                        ResourceIdentity.builder()
                            .system("CDW")
                            .resource("DIAGNOSTIC_REPORT")
                            .identifier(report.identifier())
                            .build(),
                        ResourceIdentity.builder()
                            .system("CDW")
                            .resource("ORGANIZATION")
                            .identifier(report.accessionInstitutionSid())
                            .build()))
            .collect(Collectors.toSet());

    List<Registration> registrations = witnessProtection.register(ids);

    final Table<String, String, String> idsTable = HashBasedTable.create();
    for (Registration r : registrations) {
      for (ResourceIdentity id : r.resourceIdentities()) {
        idsTable.put(id.identifier(), id.resource(), r.uuid());
      }
    }

    for (DatamartDiagnosticReports.DiagnosticReport report : reports) {
      String identifier = idsTable.get(report.identifier(), "DIAGNOSTIC_REPORT");
      if (identifier != null) {
        report.identifier(identifier);
      }

      String accessionInstitutionSid =
          idsTable.get(report.accessionInstitutionSid(), "ORGANIZATION");
      if (accessionInstitutionSid != null) {
        report.accessionInstitutionSid(accessionInstitutionSid);
      }
    }
  }

  @SneakyThrows
  private DiagnosticReport.Bundle search(
      String datamart, MultiValueMap<String, String> parameters) {
    if (BooleanUtils.isTrue(BooleanUtils.toBooleanObject(datamart))) {
      return datamartBundle(parameters);
    }

    StopWatch mraWatch = StopWatch.createStarted();
    DiagnosticReport.Bundle mrAndersonBundle = mrAndersonBundle(parameters);
    mraWatch.stop();

    if ("both".equalsIgnoreCase(datamart)) {
      StopWatch datamartWatch = StopWatch.createStarted();
      DiagnosticReport.Bundle datamartBundle = datamartBundle(parameters);
      datamartWatch.stop();
      log.info(
          "mr-anderson took {} millis and datamart took {} millis."
              + " {} mr-anderson results, {} datamart results."
              + " mr-anderson bundle is {} and datamart bundle is {}",
          mraWatch.getTime(),
          datamartWatch.getTime(),
          mrAndersonBundle.total(),
          datamartBundle.total(),
          JacksonConfig.createMapper().writeValueAsString(mrAndersonBundle),
          JacksonConfig.createMapper().writeValueAsString(datamartBundle));
    }

    return mrAndersonBundle;
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public DiagnosticReport.Bundle searchById(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamart,
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchByIdentifier(datamart, id, page, count);
  }

  /** Search by identifier. */
  @GetMapping(params = {"identifier"})
  public DiagnosticReport.Bundle searchByIdentifier(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamart,
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    DiagnosticReport report = read(datamart, identifier);
    List<DiagnosticReport> reports = report == null ? emptyList() : asList(report);
    return bundle(
        Parameters.builder()
            .add("identifier", identifier)
            .add("page", page)
            .add("_count", count)
            .build(),
        reports,
        reports.size());
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public DiagnosticReport.Bundle searchByPatient(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamart,
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return search(
        datamart,
        Parameters.builder()
            .add("patient", patient)
            .add("page", page)
            .add("_count", count)
            .build());
  }

  /** Search by Patient+Category + Date if provided. */
  @GetMapping(params = {"patient", "category"})
  public DiagnosticReport.Bundle searchByPatientAndCategoryAndDate(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamart,
      @RequestParam("patient") String patient,
      @RequestParam("category") String category,
      @RequestParam(value = "date", required = false) @Valid @DateTimeParameter @Size(max = 2)
          String[] date,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return search(
        datamart,
        Parameters.builder()
            .add("patient", patient)
            .add("category", category)
            .addAll("date", date)
            .add("page", page)
            .add("_count", count)
            .build());
  }

  /** Search by Patient+Code. */
  @GetMapping(params = {"patient", "code"})
  public DiagnosticReport.Bundle searchByPatientAndCode(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamart,
      @RequestParam("patient") String patient,
      @RequestParam("code") String code,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return search(
        datamart,
        Parameters.builder()
            .add("patient", patient)
            .add("code", code)
            .add("page", page)
            .add("_count", count)
            .build());
  }

  /** Validate Endpoint. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody DiagnosticReport.Bundle bundle) {
    return Validator.create().validate(bundle);
  }

  public interface Transformer
      extends Function<
          CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport, DiagnosticReport> {}
}
