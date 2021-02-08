package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.lighthouse.vulcan.Rules.atLeastOneParameterOf;
import static gov.va.api.lighthouse.vulcan.Rules.forbidUnknownParameters;
import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportEntity.CategoryCode;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.DiagnosticReport;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import gov.va.api.lighthouse.vulcan.mappings.TokenParameter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
    value = {"/r4/vulcanized/DiagnosticReport"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class VulcanizedR4DiagnosticReportController {

  private static final String DR_CATEGORY_SYSTEM = "http://terminology.hl7.org/CodeSystem/v2-0074";
  private static final String DR_STATUS_SYSTEM = "http://hl7.org/fhir/diagnostic-report-status";

  private final WitnessProtection witnessProtection;
  private final DiagnosticReportRepository repository;
  private final LinkProperties linkProperties;

  private VulcanConfiguration<DiagnosticReportEntity> configuration() {
    return VulcanConfiguration.forEntity(DiagnosticReportEntity.class)
        .paging(
            linkProperties.pagingConfiguration(
                "DiagnosticReport", DiagnosticReportEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(DiagnosticReportEntity.class)
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .value("identifier", "cdwId", witnessProtection::toCdwId)
                .value("patient", "icn")
                .dateAsInstant("date", "dateUtc")
                .tokenList("code", this::tokenCodeIsSupported, this::tokenCodeValues)
                .tokenList("category", this::tokenCategoryIsSupported, this::tokenCategoryValues)
                .tokenList("status", this::tokenStatusIsSupported, this::tokenStatusValues)
                .get())
        .defaultQuery(returnNothing())
        .rule(atLeastOneParameterOf("patient", "_id", "identifier"))
        .rule(parametersNeverSpecifiedTogether("patient", "_id", "identifier"))
        .rule(ifParameter("status").thenAlsoAtLeastOneParameterOf("patient"))
        .rule(forbidUnknownParameters())
        .build();
  }

  @GetMapping(value = "/{publicId}")
  public DiagnosticReport read(@PathVariable("publicId") String publicId) {
    return vulcanizedReader().read(Function.identity(), publicId);
  }

  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return vulcanizedReader().readRaw(Function.identity(), publicId, response);
  }

  /** Search support. */
  @GetMapping
  public DiagnosticReport.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<
          DiagnosticReportEntity,
          DatamartDiagnosticReport,
          DiagnosticReport,
          DiagnosticReport.Entry,
          DiagnosticReport.Bundle>
      toBundle() {
    return VulcanizedBundler.forTransformation(transformation())
        .bundling(
            Bundling.newBundle(DiagnosticReport.Bundle::new)
                .newEntry(DiagnosticReport.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .build();
  }

  @SuppressWarnings("RedundantIfStatement")
  boolean tokenCategoryIsSupported(TokenParameter token) {
    if (token.isSystemExplicitAndUnsupported(DR_CATEGORY_SYSTEM)
        || token.isCodeExplicitAndUnsupported("CH", "LAB", "MB")
        || token.hasExplicitlyNoSystem()) {
      return false;
    }
    return true;
  }

  Collection<String> tokenCategoryValues(TokenParameter token) {
    return token
        .behavior()
        .onExplicitSystemAndExplicitCode((s, c) -> CategoryCode.forFhirCategory(c))
        .onAnySystemAndExplicitCode(CategoryCode::forFhirCategory)
        .onNoSystemAndExplicitCode(CategoryCode::forFhirCategory)
        .onExplicitSystemAndAnyCode(s -> Set.of(CategoryCode.CH, CategoryCode.MB))
        .build()
        .execute()
        .stream()
        .map(CategoryCode::toString)
        .collect(toList());
  }

  boolean tokenCodeIsSupported(TokenParameter token) {
    return (token.hasSupportedCode("panel") && !token.hasExplicitlyNoSystem())
        && !token.hasExplicitSystem();
  }

  Collection<String> tokenCodeValues(TokenParameter token) {
    return token.behavior().onAnySystemAndExplicitCode(List::of).build().execute();
  }

  boolean tokenStatusIsSupported(TokenParameter token) {
    return token.hasSupportedCode("final")
        && (token.hasSupportedSystem(DR_STATUS_SYSTEM) || token.hasAnySystem());
  }

  Collection<String> tokenStatusValues(TokenParameter token) {
    /*
     * There are no values of status that are searchable. All diagnostic reports are "final", if the
     * token is supported, then we effectively "select all". By returning no values, the token
     * mapping will abstain from contributing to any additional where clauses. We rely on `patient`
     * clause to find all records for this patient.
     */
    return List.of();
  }

  VulcanizedTransformation<DiagnosticReportEntity, DatamartDiagnosticReport, DiagnosticReport>
      transformation() {
    return VulcanizedTransformation.toDatamart(DiagnosticReportEntity::asDatamartDiagnosticReport)
        .toResource(dm -> R4DiagnosticReportTransformer.builder().datamart(dm).build().toFhir())
        .witnessProtection(witnessProtection)
        .replaceReferences(
            resource -> Stream.concat(Stream.of(resource.patient()), resource.results().stream()))
        .build();
  }

  VulcanizedReader<DiagnosticReportEntity, DatamartDiagnosticReport, DiagnosticReport, String>
      vulcanizedReader() {
    return VulcanizedReader
        .<DiagnosticReportEntity, DatamartDiagnosticReport, DiagnosticReport, String>
            forTransformation(transformation())
        .repository(repository)
        .toPatientId(e -> Optional.of(e.icn()))
        .toPayload(DiagnosticReportEntity::payload)
        .build();
  }
}
