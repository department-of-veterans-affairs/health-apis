package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Iterables;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport.Bundle;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.ids.api.IdentityService;
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
  public void read() {
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
        DiagnosticReportCrossEntity.builder().reportId(reportId).icn(icn).build();
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
  public void read_empty() {
    DiagnosticReportController controller =
        new DiagnosticReportController(
            null,
            null,
            null,
            WitnessProtection.builder().identityService(mock(IdentityService.class)).build(),
            entityManager.getEntityManager());
    DiagnosticReport report = controller.read("true", "800260864479:L");
    assertThat(report).isNull();
  }

  @Test
  @SneakyThrows
  public void searchById() {
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
        DiagnosticReportCrossEntity.builder().reportId(reportId).icn(icn).build();
    entityManager.persistAndFlush(crossEntity);
    DiagnosticReportController controller =
        new DiagnosticReportController(
            null,
            null,
            new Bundler(new ConfigurableBaseUrlPageLinks("", "")),
            WitnessProtection.builder().identityService(mock(IdentityService.class)).build(),
            entityManager.getEntityManager());
    DiagnosticReport.Bundle bundle = controller.searchById("true", reportId, 1, 15);
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
                .build());
  }

  @Test
  @SneakyThrows
  public void searchByIdentifier() {
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
        DiagnosticReportCrossEntity.builder().reportId(reportId).icn(icn).build();
    entityManager.persistAndFlush(crossEntity);
    DiagnosticReportController controller =
        new DiagnosticReportController(
            null,
            null,
            new Bundler(new ConfigurableBaseUrlPageLinks("", "")),
            WitnessProtection.builder().identityService(mock(IdentityService.class)).build(),
            entityManager.getEntityManager());
    DiagnosticReport.Bundle bundle = controller.searchByIdentifier("true", reportId, 1, 15);
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
                .build());
  }

  @Test
  @SneakyThrows
  public void searchByPatient() {
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
                .effectiveDateTime(parseInstant(effectiveDateTime).toString())
                .issued(parseInstant(issuedDateTime).toString())
                .performer(
                    Reference.builder()
                        .reference("Organization/" + performer)
                        .display(performerDisplay)
                        .build())
                .build());
  }

  @Test
  @SneakyThrows
  public void searchByPatientAndCategoryAndDate() {
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
                    .effectiveDateTime(parseInstant(time2).toString())
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
                    .effectiveDateTime(parseInstant(time1).toString())
                    .build()));
  }

  @Test
  @SneakyThrows
  public void searchByPatientAndCategoryAndDate_noDates() {
    String icn = "1011537977V693883";
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
                                        .identifier("1:L")
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
            "true", icn, "LAB", new String[] {"ge2000"}, 1, 15);

    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  @SneakyThrows
  public void searchByPatientAndCategoryAndDate_exactIssued() {
    String icn = "1011537977V693883";
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
                                        .identifier("1:L")
                                        .issuedDateTime("2009-09-24T03:15:24")
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
            "true", icn, "LAB", new String[] {"2009-09-24T03:15:24Z"}, 1, 15);

    assertThat(bundle.entry().size()).isEqualTo(1);
  }

  @Test
  @SneakyThrows
  public void searchByPatientAndCategoryAndDate_exactEffective() {
    String icn = "1011537977V693883";
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
                                        .identifier("1:L")
                                        .effectiveDateTime("2009-09-24T03:15:24")
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
            "true", icn, "LAB", new String[] {"2009-09-24T03:15:24Z"}, 1, 15);

    assertThat(bundle.entry().size()).isEqualTo(1);
  }

  @Test
  @SneakyThrows
  public void searchByPatientAndCategoryAndDate_greaterThan() {
    String icn = "1011537977V693883";
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
                                        .identifier("1:L")
                                        .issuedDateTime("2009-09-24T00:00:00")
                                        .effectiveDateTime("2009-09-24T01:00:00")
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

    assertThat(
            controller
                .searchByPatientAndCategoryAndDate(
                    "true", icn, "LAB", new String[] {"gt2009-09-24T00:00:00Z"}, 1, 15)
                .entry()
                .size())
        .isEqualTo(1);
    assertThat(
            controller
                .searchByPatientAndCategoryAndDate(
                    "true", icn, "LAB", new String[] {"gt2009-09-24T01:00:00Z"}, 1, 15)
                .entry())
        .isEmpty();
  }

  @Test
  @SneakyThrows
  public void searchByPatientAndCategoryAndDate_notLab() {
    String icn = "1011537977V693883";
    String reportId = "1:L";
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
        controller.searchByPatientAndCategoryAndDate("true", icn, "CHEM", new String[] {}, 1, 15);
    // Searching for any category except LAB yields no results
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  @SneakyThrows
  public void searchByPatientAndCode() {
    String icn = "1011537977V693883";
    String reportId = "800260864479:L";
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
    DiagnosticReport.Bundle bundle = controller.searchByPatientAndCode("true", icn, "x", 1, 15);
    // Searching by any code yields no results
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  @SneakyThrows
  public void searchByPatientAndCode_noCode() {
    String icn = "1011537977V693883";
    String reportId = "800260864479:L";
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
    DiagnosticReport.Bundle bundle = controller.searchByPatientAndCode("true", icn, "", 1, 15);
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
                .build());
  }
}
