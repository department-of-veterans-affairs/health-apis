package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Iterables;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport.Bundle;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.ids.api.IdentityService;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
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

  public void assertReadable(String json) throws java.io.IOException {
    DatamartDiagnosticReports dmDr =
        createMapper()
            .readValue(getClass().getResourceAsStream(json), DatamartDiagnosticReports.class);
    assertThat(dmDr).isEqualTo(datamartDrSample());
  }

  public DiagnosticReportController controller() {
    return new DiagnosticReportController(
        true,
        null,
        null,
        new Bundler(new ConfigurableBaseUrlPageLinks("", "")),
        WitnessProtection.builder().identityService(mock(IdentityService.class)).build(),
        entityManager.getEntityManager());
  }

  private DatamartDiagnosticReports datamartDrSample() {
    DatamartDiagnosticReports.DiagnosticReport dr =
        DatamartDiagnosticReports.DiagnosticReport.builder()
            .identifier("111:L")
            .sta3n("111")
            .effectiveDateTime("2019-06-30T10:51:06Z")
            .issuedDateTime("2019-07-01T10:51:06Z")
            .accessionInstitutionSid("999")
            .accessionInstitutionName("ABC-DEF")
            .topographySid("777")
            .topographyName("PLASMA")
            .orders(
                asList(
                    DatamartDiagnosticReports.Order.builder()
                        .sid("555")
                        .display("RENAL PANEL")
                        .build()))
            .results(
                asList(
                    DatamartDiagnosticReports.Result.builder()
                        .result("111:L")
                        .display("ALBUMIN")
                        .build(),
                    DatamartDiagnosticReports.Result.builder()
                        .result("222:L")
                        .display("ALB/GLOB RATIO")
                        .build(),
                    DatamartDiagnosticReports.Result.builder()
                        .result("333:L")
                        .display("PROTEIN,TOTAL")
                        .build()))
            .build();
    return DatamartDiagnosticReports.builder()
        .objectType("DiagnosticReport")
        .objectVersion(1)
        .fullIcn("666V666")
        .patientName("VETERAN,HERNAM MINAM")
        .reports(asList(dr))
        .build();
  }

  @Test
  public void edtIsEmpty() {
    DatamartDiagnosticReports.DiagnosticReport dm =
        DatamartDiagnosticReports.DiagnosticReport.builder().effectiveDateTime("").build();
    assertThat(tx(dm).effectiveDateTime()).isNull();
  }

  @Test
  public void edtIsUnparseable() {
    DatamartDiagnosticReports.DiagnosticReport dm =
        DatamartDiagnosticReports.DiagnosticReport.builder().effectiveDateTime("aDateTime").build();
    assertThat(tx(dm).effectiveDateTime()).isNull();
  }

  @Test
  public void emptyReports() {
    DatamartDiagnosticReports emptyReports = DatamartDiagnosticReports.builder().build();
    assertThat(emptyReports.reports()).isEmpty();
  }

  @Test
  public void idtIsUnparseable() {
    DatamartDiagnosticReports.DiagnosticReport dm =
        DatamartDiagnosticReports.DiagnosticReport.builder().issuedDateTime("aDateTime").build();
    assertThat(tx(dm).issued()).isNull();
  }

  @Test
  public void read() {
    DatamartData dm = DatamartData.create();
    FhirData fhir = FhirData.from(dm);
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReport report = controller().read("true", dm.reportId());
    assertThat(report).isEqualTo(fhir.report());
  }

  @Test
  public void readRaw() {
    DatamartData dm = DatamartData.create();
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DatamartDiagnosticReports.DiagnosticReport report = controller().readRaw(dm.reportId());
    assertThat(report).isEqualTo(dm.report());
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void read_empty() {
    DiagnosticReportController controller = controller();
    controller.read("true", "800260864479:L");
  }

  @Test
  public void searchById() {
    DatamartData dm = DatamartData.create();
    FhirData fhir = FhirData.from(dm);
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReport.Bundle bundle = controller().searchById("true", dm.reportId(), 1, 15);
    assertThat(Iterables.getOnlyElement(bundle.entry()).resource()).isEqualTo(fhir.report());
  }

  @Test
  public void searchByIdentifier() {
    DatamartData dm = DatamartData.create();
    FhirData fhir = FhirData.from(dm);
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReport.Bundle bundle = controller().searchByIdentifier("true", dm.reportId(), 1, 15);
    assertThat(Iterables.getOnlyElement(bundle.entry()).resource()).isEqualTo(fhir.report());
  }

  @Test
  public void searchByPatient() {
    DatamartData dm = DatamartData.create();
    FhirData fhir = FhirData.from(dm);
    entityManager.persistAndFlush(dm.entity());
    DiagnosticReport.Bundle bundle = controller().searchByPatient("true", dm.icn(), 1, 15);
    assertThat(Iterables.getOnlyElement(bundle.entry()).resource()).isEqualTo(fhir.report());
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
                createMapper()
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
    Bundle bundle =
        controller()
            .searchByPatientAndCategoryAndDate(
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
  public void searchByPatientAndCategoryAndDate_exactEffective() {
    DatamartData dm = DatamartData.create();
    FhirData fhir = FhirData.from(dm);
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    Bundle bundle =
        controller()
            .searchByPatientAndCategoryAndDate(
                "true", dm.icn(), "LAB", new String[] {dm.effectiveDateTime()}, 1, 15);
    assertThat(bundle.entry().size()).isEqualTo(1);
  }

  @Test
  public void searchByPatientAndCategoryAndDate_exactIssued() {
    DatamartData dm = DatamartData.create();
    FhirData fhir = FhirData.from(dm);
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    Bundle bundle =
        controller()
            .searchByPatientAndCategoryAndDate(
                "true", dm.icn(), "LAB", new String[] {dm.issuedDateTime()}, 1, 15);
    assertThat(bundle.entry().size()).isEqualTo(1);
  }

  @Test
  public void searchByPatientAndCategoryAndDate_greaterThan() {
    DatamartData dm =
        DatamartData.builder()
            .issuedDateTime("2009-09-24T00:00:00")
            .effectiveDateTime("2009-09-24T01:00:00")
            .build();
    FhirData fhir = FhirData.from(dm);
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReportController controller = controller();
    assertThat(
            controller
                .searchByPatientAndCategoryAndDate(
                    "true", dm.icn(), "LAB", new String[] {"gt2009-09-24T00:00:00Z"}, 1, 15)
                .entry()
                .size())
        .isEqualTo(1);
    assertThat(
            controller
                .searchByPatientAndCategoryAndDate(
                    "true", dm.icn(), "LAB", new String[] {"gt2009-09-24T01:00:00Z"}, 1, 15)
                .entry())
        .isEmpty();
  }

  @Test
  public void searchByPatientAndCategoryAndDate_noDates() {
    DatamartData dm = DatamartData.builder().issuedDateTime("").effectiveDateTime("").build();
    FhirData fhir = FhirData.from(dm);
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    Bundle bundle =
        controller()
            .searchByPatientAndCategoryAndDate(
                "true", dm.icn(), "LAB", new String[] {"ge2000"}, 1, 15);
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  public void searchByPatientAndCategoryAndDate_notLab() {
    DatamartData dm = DatamartData.create();
    FhirData fhir = FhirData.from(dm);
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    Bundle bundle =
        controller()
            .searchByPatientAndCategoryAndDate("true", dm.icn(), "CHEM", new String[] {}, 1, 15);
    // Searching for any category except LAB yields no results
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  public void searchByPatientAndCode() {
    DatamartData dm = DatamartData.create();
    FhirData fhir = FhirData.from(dm);
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReport.Bundle bundle =
        controller().searchByPatientAndCode("true", dm.icn(), "x", 1, 15);
    // Searching by any code yields no results
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  public void searchByPatientAndCode_noCode() {
    DatamartData dm = DatamartData.create();
    FhirData fhir = FhirData.from(dm);
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReport.Bundle bundle =
        controller().searchByPatientAndCode("true", dm.icn(), "", 1, 15);
    assertThat(Iterables.getOnlyElement(bundle.entry()).resource()).isEqualTo(fhir.report());
  }

  @Test
  public void searchByPatientRaw() {
    DatamartData dm = DatamartData.create();
    entityManager.persistAndFlush(dm.entity());
    String json = controller().searchByPatientRaw(dm.icn());
    assertThat(
            DiagnosticReportsEntity.builder().payload(json).build().asDatamartDiagnosticReports())
        .isEqualTo(dm.reports());
  }

  @Test
  public void subjectIsEmpty() {
    DiagnosticReport dm =
        DatamartDiagnosticReportTransformer.builder()
            .icn("")
            .patientName("")
            .datamart(DatamartDiagnosticReports.DiagnosticReport.builder().build())
            .build()
            .toFhir();
    assertThat(dm.subject()).isNull();
  }

  private DiagnosticReport tx(DatamartDiagnosticReports.DiagnosticReport dmDr) {
    return DatamartDiagnosticReportTransformer.builder().datamart(dmDr).build().toFhir();
  }

  @Test
  @SneakyThrows
  public void unmarshalSample() {
    assertReadable("datamart-diagnostic-report.json");
  }

  @Test
  @SneakyThrows
  public void unmarshalSampleV0() {
    assertReadable("datamart-diagnostic-report-v0.json");
  }

  @Value
  @Builder
  private static class DatamartData {

    @Builder.Default String icn = "1011537977V693883";

    @Builder.Default String reportId = "800260864479:L";

    @Builder.Default String effectiveDateTime = "2009-09-24T03:15:24Z";

    @Builder.Default String issuedDateTime = "2009-09-24T03:36:35Z";

    @Builder.Default String performer = "655775";

    @Builder.Default String performerDisplay = "MANILA-RO";

    static DatamartData create() {
      return DatamartData.builder().build();
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

    DatamartDiagnosticReports.DiagnosticReport report() {
      return DatamartDiagnosticReports.DiagnosticReport.builder()
          .identifier(reportId)
          .effectiveDateTime(effectiveDateTime)
          .issuedDateTime(issuedDateTime)
          .accessionInstitutionSid(performer)
          .accessionInstitutionName(performerDisplay)
          .build();
    }

    DatamartDiagnosticReports reports() {
      return DatamartDiagnosticReports.builder().fullIcn(icn).reports(asList(report())).build();
    }
  }

  @Value
  @Builder
  private static class FhirData {

    @Builder.Default String icn = "1011537977V693883";

    @Builder.Default String reportId = "800260864479:L";

    @Builder.Default String effectiveDateTime = "2009-09-24T03:15:24";

    @Builder.Default String issuedDateTime = "2009-09-24T03:36:35";

    @Builder.Default String performer = "655775";

    @Builder.Default String performerDisplay = "MANILA-RO";

    static FhirData from(DatamartData dm) {
      return FhirData.builder()
          .icn(dm.icn())
          .reportId(dm.reportId())
          .effectiveDateTime(dm.effectiveDateTime())
          .issuedDateTime(dm.issuedDateTime())
          .performer(dm.performer())
          .performerDisplay(dm.performerDisplay())
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
