package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportSamples.Dstu2.link;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
public class Dstu2DiagnosticReportControllerTest {
  HttpServletResponse response = mock(HttpServletResponse.class);

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private DiagnosticReportRepository repository;

  public Dstu2DiagnosticReportController controller() {
    return new Dstu2DiagnosticReportController(
        new Dstu2Bundler(
            new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        WitnessProtection.builder().identityService(ids).build(),
        repository,
        null);
  }

  @SneakyThrows
  DiagnosticReportEntity entity(DatamartDiagnosticReport dm) {
    return DiagnosticReportEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.patient().reference().orElse(null))
        .category("LAB")
        .code("panel")
        .dateUtc(Instant.parse(dm.issuedDateTime()))
        .payload(json(dm))
        .build();
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public void mockDiagnosticReportEntity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("DIAGNOSTIC_REPORT")
            .identifier(cdwId)
            .build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(publicId)
                    .resourceIdentities(List.of(resourceIdentity))
                    .build()));
  }

  @SneakyThrows
  private DatamartDiagnosticReport object(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartDiagnosticReport.class);
  }

  private Multimap<String, DiagnosticReport> populateData() {
    var fhir = DiagnosticReportSamples.Dstu2.create();
    var datamart = DiagnosticReportSamples.DatamartV2.create();
    var diagnosticReportsByPatient = LinkedHashMultimap.<String, DiagnosticReport>create();
    var registrations = new ArrayList<Registration>(10);
    for (int i = 0; i < 10; i++) {
      String date = "2020-01-2" + i + "T07:57:00Z";
      String patientId = "p" + i % 2;
      String cdwId = "cdw-" + i;
      String publicId = "public" + i;
      DatamartDiagnosticReport dm = datamart.diagnosticReport(cdwId, patientId, date);
      repository.save(entity(dm));
      DiagnosticReport dr = fhir.report(publicId, patientId, date);
      diagnosticReportsByPatient.put(patientId, dr);
      mockDiagnosticReportEntity(publicId, cdwId);
      ResourceIdentity resourceIdentity =
          ResourceIdentity.builder()
              .system("CDW")
              .resource("DIAGNOSTIC_REPORT")
              .identifier(cdwId)
              .build();
      Registration registration =
          Registration.builder()
              .uuid(publicId)
              .resourceIdentities(List.of(resourceIdentity))
              .build();
      registrations.add(registration);
      when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    }
    when(ids.register(Mockito.any())).thenReturn(registrations);
    return diagnosticReportsByPatient;
  }

  @Test
  void read() {
    DatamartDiagnosticReport dm = DiagnosticReportSamples.DatamartV2.create().diagnosticReport();
    repository.save(entity(dm));
    mockDiagnosticReportEntity("x", dm.cdwId());
    assertThat(controller().read(true, "x"))
        .isEqualTo(DiagnosticReportSamples.Dstu2.create().report("x"));
  }

  @Test
  void readRaw() {
    DatamartDiagnosticReport dm = DiagnosticReportSamples.DatamartV2.create().diagnosticReport();
    DiagnosticReportEntity entity = entity(dm);
    repository.save(entity);
    mockDiagnosticReportEntity("x", dm.cdwId());
    assertThat(object(controller().readRaw(true, "x", response))).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", entity.icn());
  }

  @Test
  void readWithUnknownIdIsNotFound() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read(true, "BIGoof"));
  }

  @Test
  void searchById() {
    DatamartDiagnosticReport dm = DiagnosticReportSamples.DatamartV2.create().diagnosticReport();
    repository.save(entity(dm));
    mockDiagnosticReportEntity("x", dm.cdwId());
    DiagnosticReport dstu2 = DiagnosticReportSamples.Dstu2.create().report("x");
    assertThat(controller().searchById(true, "x", 1, 15))
        .isEqualTo(
            DiagnosticReportSamples.Dstu2.asBundle(
                "http://fonzy.com/cool",
                List.of(dstu2),
                1,
                link(
                    BundleLink.LinkRelation.first,
                    "http://fonzy.com/cool/DiagnosticReport?identifier=x",
                    1,
                    15),
                link(
                    BundleLink.LinkRelation.self,
                    "http://fonzy.com/cool/DiagnosticReport?identifier=x",
                    1,
                    15),
                link(
                    BundleLink.LinkRelation.last,
                    "http://fonzy.com/cool/DiagnosticReport?identifier=x",
                    1,
                    15)));
  }

  @Test
  void searchByIdentifier() {
    DatamartDiagnosticReport dm = DiagnosticReportSamples.DatamartV2.create().diagnosticReport();
    repository.save(entity(dm));
    mockDiagnosticReportEntity("x", dm.cdwId());
    DiagnosticReport dstu2 = DiagnosticReportSamples.Dstu2.create().report("x");
    assertThat(json(controller().searchByIdentifier(true, "x", 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(dstu2),
                    1,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport?identifier=x",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?identifier=x",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport?identifier=x",
                        1,
                        15))));
  }

  @Test
  void searchByPatient() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports = diagnosticReportsByPatient.get("p0");
    assertThat(json(controller().searchByPatient(true, "p0", 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    diagnosticReports,
                    diagnosticReports.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport?patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport?patient=p0",
                        1,
                        15))));
  }

  @Test
  void searchByPatientAndCategoryAndOneDate() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports =
        diagnosticReportsByPatient.get("p0").stream()
            .filter(dr -> "LAB".equals(dr.category().coding().get(0).code()))
            .filter(dr -> "2020-01-20T07:57:00Z".equals(dr.issued()))
            .collect(Collectors.toList());
    assertThat(
            json(
                controller()
                    .searchByPatientAndCategory(
                        true, "p0", "LAB", new String[] {"2020-01-20T07:57:00Z"}, 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    diagnosticReports,
                    diagnosticReports.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?category=LAB"
                            + "&date=2020-01-20T07:57:00Z"
                            + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?category=LAB"
                            + "&date=2020-01-20T07:57:00Z"
                            + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?category=LAB"
                            + "&date=2020-01-20T07:57:00Z"
                            + "&patient=p0",
                        1,
                        15))));
  }

  @Test
  void searchByPatientAndCategoryAndTwoDates() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports =
        diagnosticReportsByPatient.get("p0").stream()
            .filter(dr -> "LAB".equals(dr.category().coding().get(0).code()))
            .filter(
                dr -> {
                  Instant issued = Instant.parse(dr.issued());
                  return issued.isAfter(Instant.parse("2020-01-20T07:57:00Z"))
                      && issued.isBefore(Instant.parse("2020-01-26T07:57:00Z"));
                })
            .collect(Collectors.toList());
    assertThat(
            json(
                controller()
                    .searchByPatientAndCategory(
                        true,
                        "p0",
                        "LAB",
                        new String[] {"gt2020-01-20T07:57:00Z", "le2020-01-25T07:57:00Z"},
                        1,
                        15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    diagnosticReports,
                    diagnosticReports.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?category=LAB"
                            + "&date=gt2020-01-20T07:57:00Z"
                            + "&date=le2020-01-25T07:57:00Z"
                            + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?category=LAB"
                            + "&date=gt2020-01-20T07:57:00Z"
                            + "&date=le2020-01-25T07:57:00Z"
                            + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?category=LAB"
                            + "&date=gt2020-01-20T07:57:00Z"
                            + "&date=le2020-01-25T07:57:00Z"
                            + "&patient=p0",
                        1,
                        15))));
  }

  @Test
  void searchByPatientAndCategoryNoDate() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports =
        diagnosticReportsByPatient.get("p0").stream()
            .filter(dr -> "LAB".equals(dr.category().coding().get(0).code()))
            .collect(Collectors.toList());
    assertThat(json(controller().searchByPatientAndCategory(true, "p0", "LAB", null, 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    diagnosticReports,
                    diagnosticReports.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport" + "?category=LAB" + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport" + "?category=LAB" + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport" + "?category=LAB" + "&patient=p0",
                        1,
                        15))));
  }

  @Test
  void searchByPatientAndCode() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports =
        diagnosticReportsByPatient.get("p0").stream()
            .filter(dr -> "panel".equals(dr.code().text()))
            .collect(Collectors.toList());
    assertThat(json(controller().searchByPatientAndCode(true, "p0", "panel", 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    diagnosticReports,
                    diagnosticReports.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport" + "?code=panel" + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport" + "?code=panel" + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport" + "?code=panel" + "&patient=p0",
                        1,
                        15))));
  }

  @Test
  void searchWithCount0() {
    DatamartDiagnosticReport datamart =
        DiagnosticReportSamples.DatamartV2.create().diagnosticReport();
    repository.save(entity(datamart));
    assertThat(json(controller().searchByIdentifier(true, "800260864479:L", 1, 0)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    emptyList(),
                    1,
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?identifier=800260864479:L",
                        1,
                        0))));
  }
}
