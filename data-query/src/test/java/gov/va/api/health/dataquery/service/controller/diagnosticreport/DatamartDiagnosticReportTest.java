package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Iterables;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.ids.api.IdentityService;
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

    DiagnosticReportPatientEntity crossEntity =
        DiagnosticReportPatientEntity.builder().reportId(reportId).icn(icn).build();
    entityManager.persistAndFlush(crossEntity);

    DiagnosticReportEntity entity =
        DiagnosticReportEntity.builder()
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
            null,
            WitnessProtection.builder().identityService(mock(IdentityService.class)).build(),
            entityManager.getEntityManager());

    DiagnosticReport report = controller.read("true", reportId);
    assertThat(report)
        .isEqualTo(
            DiagnosticReport.builder()
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
                .subject(Reference.builder().reference("Patient/1011537977V693883").build())
                .build());
  }

  @Test
  @SneakyThrows
  public void patientSearch() {
    String icn = "1011537977V693883";
    String reportId = "800260864479:L";

    DiagnosticReportEntity entity =
        DiagnosticReportEntity.builder()
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

    DiagnosticReport.Bundle bundle = controller.searchByPatient("true", icn, 1, 15);
    assertThat(Iterables.getOnlyElement(bundle.entry()).resource())
        .isEqualTo(
            DiagnosticReport.builder()
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
                .subject(Reference.builder().reference("Patient/1011537977V693883").build())
                .build());
  }
}
