package gov.va.api.health.dataquery.service.controller.diagnosticreport.v1;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static java.util.Arrays.asList;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport.Bundle;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport.Entry;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DiagnosticReportSamples {

  @Builder
  public static class Datamart {

    @Builder.Default String icn = "1011537977V693883";

    @Builder.Default String reportId = "800260864479:L";

    @Builder.Default String effectiveDateTime = "2009-09-24T03:15:24Z";

    @Builder.Default String issuedDateTime = "2009-09-24T03:36:35Z";

    @Builder.Default String performer = "655775";

    @Builder.Default String performerDisplay = "MANILA-RO";

    public static Datamart create() {
      return Datamart.builder().build();
    }

    DiagnosticReportCrossEntity crossEntity() {
      return DiagnosticReportCrossEntity.builder().reportId(reportId).icn(icn).build();
    }

    @SneakyThrows
    DiagnosticReportsEntity entity() {
      return DiagnosticReportsEntity.builder()
          .icn(icn)
          .payload(createMapper().writeValueAsString(reports()))
          .build();
    }

    @SneakyThrows
    DiagnosticReportsEntity entityWithNoReport() {
      return DiagnosticReportsEntity.builder()
          .icn(icn)
          .payload(
              createMapper()
                  .writeValueAsString(DatamartDiagnosticReports.builder().fullIcn(icn).build()))
          .build();
    }

    public DatamartDiagnosticReports.DiagnosticReport report() {
      return DatamartDiagnosticReports.DiagnosticReport.builder()
          .identifier(reportId)
          .effectiveDateTime(effectiveDateTime)
          .issuedDateTime(issuedDateTime)
          .accessionInstitutionSid(performer)
          .accessionInstitutionName(performerDisplay)
          .results(List.of(DatamartDiagnosticReports.Result.builder().result("TEST").build()))
          .build();
    }

    public DatamartDiagnosticReports reports() {
      return DatamartDiagnosticReports.builder().fullIcn(icn).reports(asList(report())).build();
    }
  }

  @Builder
  public static class Dstu2 {

    @Builder.Default String icn = "1011537977V693883";

    @Builder.Default String reportId = "800260864479:L";

    @Builder.Default String effectiveDateTime = "2009-09-24T03:15:24";

    @Builder.Default String issuedDateTime = "2009-09-24T03:36:35";

    @Builder.Default String performer = "655775";

    @Builder.Default String performerDisplay = "MANILA-RO";

    static Bundle asBundle(
        String baseUrl,
        Collection<DiagnosticReport> reports,
        int totalRecords,
        BundleLink... links) {
      return Bundle.builder()
          .resourceType("Bundle")
          .type(AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              reports.stream()
                  .map(
                      c ->
                          Entry.builder()
                              .fullUrl(baseUrl + "/DiagnosticReport/" + c.id())
                              .resource(c)
                              .search(
                                  AbstractEntry.Search.builder()
                                      .mode(AbstractEntry.SearchMode.match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static Dstu2 create() {
      return Dstu2.builder().build();
    }

    static BundleLink link(BundleLink.LinkRelation rel, String base, int page, int count) {
      return BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    DiagnosticReport report() {
      return DiagnosticReport.builder()
          .id(reportId)
          .resourceType("DiagnosticReport")
          .status(DiagnosticReport.Code._final)
          .category(
              CodeableConcept.builder()
                  .coding(
                      asList(
                          Coding.builder()
                              .system("http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
                              .code("LAB")
                              .display("Laboratory")
                              .build()))
                  .build())
          .code(CodeableConcept.builder().text("panel").build())
          .subject(Reference.builder().reference("Patient/" + icn).build())
          .effectiveDateTime(parseInstant(effectiveDateTime).toString())
          .issued(parseInstant(issuedDateTime).toString())
          .performer(
              Reference.builder()
                  .reference("Organization/" + performer)
                  .display(performerDisplay)
                  .build())
          .build();
    }
  }
}
