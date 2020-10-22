package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.vulcanizer.Vulcanizer.transformEntityUsing;
import static gov.va.api.lighthouse.vulcan.Vulcan.useUrl;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.r4.api.resources.DiagnosticReport;
import gov.va.api.health.r4.api.resources.DiagnosticReport.Bundle;
import gov.va.api.health.r4.api.resources.DiagnosticReport.Entry;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration.PagingConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import gov.va.api.lighthouse.vulcan.mappings.TokenParameter;
import java.util.List;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
    value = {"/r4/vulcanized/DiagnosticReport"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class VulcanizedR4DiagnosticReportController {
  private final WitnessProtection witnessProtection;

  private final DiagnosticReportRepository repository;

  private final LinkProperties linkProperties;

  private boolean codeIsPanel(TokenParameter t) {
    return (t.hasSupportedCode("panel") && !t.hasExplicitlyNoSystem()) && !t.hasExplicitSystem();
  }

  private List<String> codeValue(TokenParameter token) {
    return token
        .behavior()
        .onAnySystemAndExplicitCode(List::of)
        .onNoSystemAndExplicitCode(List::of)
        .build()
        .execute();
  }

  private VulcanConfiguration<DiagnosticReportEntity> configuration() {
    return VulcanConfiguration.forEntity(DiagnosticReportEntity.class)
        .paging(
            PagingConfiguration.builder()
                .baseUrlStrategy(useUrl(linkProperties.r4().resourceUrl("DiagnosticReport")))
                .pageParameter("page")
                .countParameter("_count")
                .defaultCount(15) // TODO inject via properties
                .maxCount(100) // TODO inject via properties
                .sort(DiagnosticReportEntity.naturalOrder())
                .build())
        .mappings(
            Mappings.forEntity(DiagnosticReportEntity.class)
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .value("identifier", "cdwId", witnessProtection::toCdwId)
                .value("patient", "icn")
                .dateAsInstant("date", "dateUtc")
                .csvList("status", "code")
                .string("category", "category")
                .token("code", this::codeIsPanel, this::codeValue)
                // TODO category token
                // TODO csv token for code
                // TODO csv token for status that short circuits return nothing or returns search by
                // TODO ... patient
                .get())
        .build();
  }

  @GetMapping
  public DiagnosticReport.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .forge(request)
        .map(toBundle());
  }

  public VulcanizedBundler<
          DiagnosticReportEntity, DatamartDiagnosticReport, DiagnosticReport, Entry, Bundle>
      toBundle() {
    return transformEntityUsing(DiagnosticReportEntity::asDatamartDiagnosticReport)
        .withWitnessProtection(witnessProtection)
        .replacingReferences(
            resource -> Stream.concat(Stream.of(resource.patient()), resource.results().stream()))
        .thenTransformToResourceUsing(
            dm -> R4DiagnosticReportTransformer.builder().datamart(dm).build().toFhir())
        .andBundleAs(Bundle::new)
        .createEntriesUsing(Entry::new)
        .withLinkProperties(linkProperties)
        .get();
  }
}
