package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportSamples.Dstu2.link;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.v1.DatamartDiagnosticReports;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.ids.api.IdentityService;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
public class Dstu2DiagnosticReportControllerTest {

  HttpServletResponse response = mock(HttpServletResponse.class);

  @Autowired private TestEntityManager entityManager;

  public Dstu2DiagnosticReportController controller() {
    return new Dstu2DiagnosticReportController(
        new Dstu2Bundler(
            new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        WitnessProtection.builder().identityService(mock(IdentityService.class)).build(),
        entityManager.getEntityManager());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @Test
  void read() {
    assertThat(controller().read(true, "800260864479:L"))
        .isEqualTo(DiagnosticReport.builder().build());
  }

  @Test
  void readRaw() {
    assertThat(controller().readRaw(true, "800260864479:L", response))
        .isEqualTo(DatamartDiagnosticReports.DiagnosticReport.builder().build());
  }

  @Test
  void searchById() {
    assertThat(controller().searchById(true, "800260864479:L", 1, 15))
        .isEqualTo(
            DiagnosticReportSamples.Dstu2.asBundle(
                "http://fonzy.com/cool",
                emptyList(),
                0,
                link(
                    BundleLink.LinkRelation.first,
                    "http://fonzy.com/cool/DiagnosticReport?identifier=800260864479:L",
                    1,
                    15),
                link(
                    BundleLink.LinkRelation.self,
                    "http://fonzy.com/cool/DiagnosticReport?identifier=800260864479:L",
                    1,
                    15),
                link(
                    BundleLink.LinkRelation.last,
                    "http://fonzy.com/cool/DiagnosticReport?identifier=800260864479:L",
                    0,
                    15)));
  }

  @Test
  void searchByIdentifier() {
    assertThat(json(controller().searchByIdentifier(true, "800260864479:L", 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    emptyList(),
                    0,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport?identifier=800260864479:L",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?identifier=800260864479:L",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport?identifier=800260864479:L",
                        0,
                        15))));
  }

  @Test
  void searchByPatient() {
    assertThat(json(controller().searchByPatient(true, "800260864479:L", 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    emptyList(),
                    0,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport?patient=800260864479:L",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?patient=800260864479:L",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport?patient=800260864479:L",
                        0,
                        15))));
  }

  @Test
  void searchByPatientAndCategoryNoDate() {
    assertThat(
            json(
                controller()
                    .searchByPatientAndCategory(true, "800260864479:L", "LAB", null, 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    emptyList(),
                    0,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?category=LAB"
                            + "&patient=800260864479:L",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?category=LAB"
                            + "&patient=800260864479:L",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?category=LAB"
                            + "&patient=800260864479:L",
                        0,
                        15))));
  }

  @Test
  void searchByPatientAndCode() {
    assertThat(json(controller().searchByPatientAndCode(true, "800260864479:L", "x", 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    emptyList(),
                    0,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?code=x"
                            + "&patient=800260864479:L",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?code=x"
                            + "&patient=800260864479:L",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?code=x"
                            + "&patient=800260864479:L",
                        0,
                        15))));
  }
}
