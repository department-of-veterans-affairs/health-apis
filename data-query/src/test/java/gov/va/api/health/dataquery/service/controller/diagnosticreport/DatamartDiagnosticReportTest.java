package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.Transformers.parseLocalDateTime;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport.Bundle;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root;
import java.math.BigInteger;
import java.time.ZoneId;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public final class DatamartDiagnosticReportTest {

  @Autowired private TestEntityManager entityManager;

  @Test
  @SneakyThrows
  public void basicRead() {
    String icn = "1011537977V693883";
    String reportId = "800260864479:L";
    DiagnosticReportsEntity entity =
        DiagnosticReportsEntity.builder()
            .icn(icn)
            .payload(
                JacksonConfig.createMapper()
                    .writeValueAsString(
                        DatamartDiagnosticReports.builder()
                            .reports(
                                asList(
                                    DatamartDiagnosticReports.DiagnosticReport.builder()
                                        .identifier(reportId)
                                        .build()))
                            .build()))
            .build();
    entityManager.persistAndFlush(entity);
    DiagnosticReportCrossEntity crossEntity =
        DiagnosticReportCrossEntity.builder()
            .reportId(reportId)
            .icn(icn)
            .reportsEntity(entity)
            .build();
    entityManager.persistAndFlush(crossEntity);
    DiagnosticReportController controller =
        new DiagnosticReportController(
            null,
            null,
            null,
            WitnessProtection.builder().identityService(mock(IdentityService.class)).build(),
            entityManager.getEntityManager());
    DiagnosticReport report = controller.read("true", reportId);
    assertThat(report)
        .isEqualTo(
            DiagnosticReport.builder()
                .id(reportId)
                .resourceType("DiagnosticReport")
                .status(DiagnosticReport.Code._final)
                .category(
                    CodeableConcept.builder()
                        .coding(
                            asList(
                                Coding.builder()
                                    .system(
                                        "http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
                                    .code("LAB")
                                    .display("Laboratory")
                                    .build()))
                        .build())
                .code(CodeableConcept.builder().text("panel").build())
                .build());
  }

  @Test
  @SneakyThrows
  public void both() {
    CdwDiagnosticReport102Root.CdwDiagnosticReports wrapper =
        new CdwDiagnosticReport102Root.CdwDiagnosticReports();
    wrapper
        .getDiagnosticReport()
        .add(new CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport());
    CdwDiagnosticReport102Root root = new CdwDiagnosticReport102Root();
    root.setDiagnosticReports(wrapper);
    root.setRecordCount(BigInteger.ZERO);
    MrAndersonClient mrAnderson = mock(MrAndersonClient.class);
    when(mrAnderson.search(any())).thenReturn(root);
    DiagnosticReportController controller =
        new DiagnosticReportController(
            new DiagnosticReportTransformer(),
            mrAnderson,
            new Bundler(new ConfigurableBaseUrlPageLinks("", "")),
            WitnessProtection.builder().identityService(mock(IdentityService.class)).build(),
            entityManager.getEntityManager());
    DiagnosticReport.Bundle bundle = controller.searchByPatient("both", "1011537977V693883", 1, 15);
    assertThat(Iterables.getOnlyElement(bundle.entry()).resource())
        .isEqualTo(
            DiagnosticReport.builder()
                .resourceType("DiagnosticReport")
                ._performer(
                    Extension.builder()
                        .extension(
                            asList(
                                Extension.builder()
                                    .url(
                                        "http://hl7.org/fhir/StructureDefinition/data-absent-reason")
                                    .valueCode("unknown")
                                    .build()))
                        .build())
                .build());
  }

  @Test
  @SneakyThrows
  public void dateSearch() {
    String icn = "1011537977V693883";
    String reportId1 = "1:L";
    String reportId2 = "2:L";
    String time1 = "2009-09-24T03:15:24";
    String time2 = "2009-09-25T03:15:24";
    DiagnosticReportsEntity entity =
        DiagnosticReportsEntity.builder()
            .icn(icn)
            .payload(
                JacksonConfig.createMapper()
                    .writeValueAsString(
                        DatamartDiagnosticReports.builder()
                            .fullIcn(icn)
                            .reports(
                                asList(
                                    DatamartDiagnosticReports.DiagnosticReport.builder()
                                        .identifier(reportId1)
                                        .effectiveDateTime(time1)
                                        .build(),
                                    DatamartDiagnosticReports.DiagnosticReport.builder()
                                        .identifier(reportId2)
                                        .effectiveDateTime(time2)
                                        .build()))
                            .build()))
            .build();
    entityManager.persistAndFlush(entity);
    DiagnosticReportController controller =
        new DiagnosticReportController(
            null,
            null,
            new Bundler(new ConfigurableBaseUrlPageLinks("", "")),
            WitnessProtection.builder().identityService(mock(IdentityService.class)).build(),
            entityManager.getEntityManager());
    Bundle bundle =
        controller.searchByPatientAndCategoryAndDate(
            "true",
            icn,
            "LAB",
            new String[] {"gt2008", "ge2008", "eq2009", "le2010", "lt2010"},
            1,
            15);
    assertThat(bundle.entry().stream().map(e -> e.resource()).collect(Collectors.toList()))
        .isEqualTo(
            asList(
                DiagnosticReport.builder()
                    .id(reportId2)
                    .resourceType("DiagnosticReport")
                    .status(DiagnosticReport.Code._final)
                    .category(
                        CodeableConcept.builder()
                            .coding(
                                asList(
                                    Coding.builder()
                                        .system(
                                            "http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
                                        .code("LAB")
                                        .display("Laboratory")
                                        .build()))
                            .build())
                    .code(CodeableConcept.builder().text("panel").build())
                    .subject(Reference.builder().reference("Patient/" + icn).build())
                    .effectiveDateTime(parseLocalDateTime(time2).atZone(ZoneId.of("Z")).toString())
                    .build(),
                DiagnosticReport.builder()
                    .id(reportId1)
                    .resourceType("DiagnosticReport")
                    .status(DiagnosticReport.Code._final)
                    .category(
                        CodeableConcept.builder()
                            .coding(
                                asList(
                                    Coding.builder()
                                        .system(
                                            "http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
                                        .code("LAB")
                                        .display("Laboratory")
                                        .build()))
                            .build())
                    .code(CodeableConcept.builder().text("panel").build())
                    .subject(Reference.builder().reference("Patient/" + icn).build())
                    .effectiveDateTime(parseLocalDateTime(time1).atZone(ZoneId.of("Z")).toString())
                    .build()));
  }

  @Test
  @SneakyThrows
  public void patientSearch() {
    String icn = "1011537977V693883";
    String reportId = "800260864479:L";
    String effectiveDateTime = "2009-09-24T03:15:24";
    String issuedDateTime = "2009-09-24T03:36:35";
    String performer = "655775";
    String performerDisplay = "MANILA-RO";
    DiagnosticReportsEntity entity =
        DiagnosticReportsEntity.builder()
            .icn(icn)
            .payload(
                JacksonConfig.createMapper()
                    .writeValueAsString(
                        DatamartDiagnosticReports.builder()
                            .fullIcn(icn)
                            .reports(
                                asList(
                                    DatamartDiagnosticReports.DiagnosticReport.builder()
                                        .identifier(reportId)
                                        .effectiveDateTime(effectiveDateTime)
                                        .issuedDateTime(issuedDateTime)
                                        .accessionInstitutionSid(performer)
                                        .accessionInstitutionName(performerDisplay)
                                        .build()))
                            .build()))
            .build();
    entityManager.persistAndFlush(entity);
    DiagnosticReportController controller =
        new DiagnosticReportController(
            null,
            null,
            new Bundler(new ConfigurableBaseUrlPageLinks("", "")),
            WitnessProtection.builder().identityService(mock(IdentityService.class)).build(),
            entityManager.getEntityManager());
    DiagnosticReport.Bundle bundle = controller.searchByPatient("true", icn, 1, 15);
    assertThat(Iterables.getOnlyElement(bundle.entry()).resource())
        .isEqualTo(
            DiagnosticReport.builder()
                .id(reportId)
                .resourceType("DiagnosticReport")
                .status(DiagnosticReport.Code._final)
                .category(
                    CodeableConcept.builder()
                        .coding(
                            asList(
                                Coding.builder()
                                    .system(
                                        "http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
                                    .code("LAB")
                                    .display("Laboratory")
                                    .build()))
                        .build())
                .code(CodeableConcept.builder().text("panel").build())
                .subject(Reference.builder().reference("Patient/" + icn).build())
                .effectiveDateTime(
                    parseLocalDateTime(effectiveDateTime).atZone(ZoneId.of("Z")).toString())
                .issued(parseLocalDateTime(issuedDateTime).atZone(ZoneId.of("Z")).toString())
                .performer(
                    Reference.builder()
                        .reference("Organization/" + performer)
                        .display(performerDisplay)
                        .build())
                .build());
  }
}
