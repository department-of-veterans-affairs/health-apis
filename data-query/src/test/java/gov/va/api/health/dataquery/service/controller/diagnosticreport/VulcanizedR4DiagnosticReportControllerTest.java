package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.MockRequests.paging;
import static gov.va.api.health.dataquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportSamples.id;
import static gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportSamples.registration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Splitter;
import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportSamples.DatamartV2;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportSamples.R4;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation;
import gov.va.api.lighthouse.vulcan.InvalidRequest;
import gov.va.api.lighthouse.vulcan.VulcanResult;
import gov.va.api.lighthouse.vulcan.mappings.TokenParameter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class VulcanizedR4DiagnosticReportControllerTest {

  @Mock IdentityService ids;

  @Mock DiagnosticReportRepository repository;

  VulcanizedR4DiagnosticReportController controller() {
    return new VulcanizedR4DiagnosticReportController(
        WitnessProtection.builder().identityService(ids).build(),
        repository,
        LinkProperties.builder()
            .publicUrl("http://fonzy.com")
            .publicR4BasePath("r4")
            .publicStu3BasePath("stu3")
            .publicDstu2BasePath("dstu2")
            .maxPageSize(20)
            .defaultPageSize(15)
            .build());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "?category=LAB",
        "?patient=p1&_id=456",
        "?patient=p1&identifier=456",
        "?_id=p1&identifier=456",
        "?status=final",
        "?patient=p1&nachos=true"
      })
  @SneakyThrows
  void invalidRequests(String query) {
    var r = requestFromUri("http://fonzy.com/r4/DiagnosticReport" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> controller().search(r));
  }

  @Test
  void read() {
    when(ids.register(any())).thenReturn(List.of(registration("dr1", "pdr1")));
    when(ids.lookup("pdr1")).thenReturn(List.of(id("dr1")));
    DiagnosticReportEntity entity = DatamartV2.create().entity("dr1", "p1");
    when(repository.findById("dr1")).thenReturn(Optional.of(entity));
    assertThat(controller().read("pdr1"))
        .isEqualTo(DiagnosticReportSamples.R4.create().diagnosticReport("pdr1", "p1"));
  }

  @Test
  void readRaw() {
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(ids.lookup("pdr1")).thenReturn(List.of(id("dr1")));
    DiagnosticReportEntity entity =
        DiagnosticReportEntity.builder().icn("p1").payload("payload!").build();
    when(repository.findById("dr1")).thenReturn(Optional.of(entity));
    assertThat(controller().readRaw("pdr1", response)).isEqualTo("payload!");
  }

  @Test
  void toBundle() {
    when(ids.register(any()))
        .thenReturn(
            List.of(
                registration("dr1", "pdr1"),
                registration("dr2", "pdr2"),
                registration("dr3", "pdr3")));

    var bundler = controller().toBundle();
    DatamartV2 datamart = DatamartV2.create();
    var vr =
        VulcanResult.<DiagnosticReportEntity>builder()
            .paging(
                paging(
                    "http://fonzy.com/r4/DiagnosticReport?patient=p1&page=%d&_count=%d",
                    1, 4, 5, 6, 9, 15))
            .entities(
                Stream.of(
                    datamart.entity("dr1", "p1"),
                    datamart.entity("dr2", "p1"),
                    datamart.entity("dr3", "p1")))
            .build();

    R4 r4 = R4.create();
    var expected =
        R4.asBundle(
            "http://fonzy.com/r4",
            List.of(
                r4.diagnosticReport("pdr1", "p1"),
                r4.diagnosticReport("pdr2", "p1"),
                r4.diagnosticReport("pdr3", "p1")),
            999,
            R4.link(LinkRelation.first, "http://fonzy.com/r4/DiagnosticReport?patient=p1", 1, 15),
            R4.link(LinkRelation.prev, "http://fonzy.com/r4/DiagnosticReport?patient=p1", 4, 15),
            R4.link(LinkRelation.self, "http://fonzy.com/r4/DiagnosticReport?patient=p1", 5, 15),
            R4.link(LinkRelation.next, "http://fonzy.com/r4/DiagnosticReport?patient=p1", 6, 15),
            R4.link(LinkRelation.last, "http://fonzy.com/r4/DiagnosticReport?patient=p1", 9, 15));

    assertThat(bundler.apply(vr)).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
    "LAB,true",
    "CH,true",
    "MB,true",
    "http://terminology.hl7.org/CodeSystem/v2-0074|LAB,true",
    "http://terminology.hl7.org/CodeSystem/v2-0074|CH,true",
    "http://terminology.hl7.org/CodeSystem/v2-0074|MB,true",
    "http://terminology.hl7.org/CodeSystem/v2-0074|,true",
    "http://nope|LAB,false",
    "http://nope|CH,false",
    "http://nope|MB,false",
    "|LAB,false",
    "|CH,false",
    "|MB,false",
    "NOPE,false",
    "http://terminology.hl7.org/CodeSystem/v2-0074|NOPE,false"
  })
  void tokenCategoryIsSupported(String parameterValue, boolean expectedSupport) {
    var token = TokenParameter.parse("category", parameterValue);
    assertThat(controller().tokenCategoryIsSupported(token)).isEqualTo(expectedSupport);
  }

  @ParameterizedTest
  @CsvSource({
    "LAB,CH+MB",
    "CH,CH",
    "MB,MB",
    "http://terminology.hl7.org/CodeSystem/v2-0074|LAB,CH+MB",
    "http://terminology.hl7.org/CodeSystem/v2-0074|CH,CH",
    "http://terminology.hl7.org/CodeSystem/v2-0074|MB,MB",
    "http://terminology.hl7.org/CodeSystem/v2-0074|,CH+MB"
  })
  void tokenCategoryValues(String parameterValue, String expected) {
    var token = TokenParameter.parse("category", parameterValue);
    assertThat(controller().tokenCategoryValues(token))
        .containsExactlyInAnyOrderElementsOf(Splitter.on('+').splitToList(expected));
  }

  @ParameterizedTest
  @CsvSource({"panel,true", "http://nope|panel,false", "|panel,false", "nope,false"})
  void tokenCodeIsSupported(String parameterValue, boolean expectedSupport) {
    var token = TokenParameter.parse("code", parameterValue);
    assertThat(controller().tokenCodeIsSupported(token)).isEqualTo(expectedSupport);
  }

  @Test
  void tokenCodeValues() {
    var token = TokenParameter.parse("code", "panel");
    assertThat(controller().tokenCodeValues(token)).containsExactly("panel");
  }

  @ParameterizedTest
  @CsvSource({
    "final,true",
    "http://hl7.org/fhir/diagnostic-report-status|final,true",
    "http://nope|final,false",
    "|final,false",
    "NOPE,false",
    "http://hl7.org/fhir/diagnostic-report-status|NOPE,false"
  })
  void tokenStatusIsSupported(String parameterValue, boolean expectedSupport) {
    var token = TokenParameter.parse("status", parameterValue);
    assertThat(controller().tokenStatusIsSupported(token)).isEqualTo(expectedSupport);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "final",
        "http://hl7.org/fhir/diagnostic-report-status|final",
      })
  void tokenStatusValues(String parameterValue) {
    var token = TokenParameter.parse("status", parameterValue);
    assertThat(controller().tokenStatusValues(token)).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=pdr1",
        "?identifier=pdr1",
        "?patient=p1",
        "?patient=p1&category=LAB",
        "?patient=p1&category=LAB&date=2005",
        "?patient=p1&category=LAB&date=gt2005&date=lt2006",
        "?patient=p1&code=panel",
        "?patient=p1&code=panel&date=2005",
        "?patient=p1&code=panel&date=gt2005&date=lt2006",
        "?patient=p1&status=final"
      })
  @SneakyThrows
  void validRequests(String query) {
    when(ids.register(any()))
        .thenReturn(
            List.of(
                registration("dr1", "pdr1"),
                registration("dr2", "pdr2"),
                registration("dr3", "pdr3")));

    DatamartV2 datamart = DatamartV2.create();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl(
                    List.of(
                        datamart.entity("dr1", "p1"),
                        datamart.entity("dr2", "p1"),
                        datamart.entity("dr3", "p1")),
                    i.getArgument(1, Pageable.class),
                    3));

    var r = requestFromUri("http://fonzy.com/r4/DiagnosticReport" + query);
    var actual = controller().search(r);
    /*
     * The bundler and transformer are tested independently, we only care that bundle was returned.
     */
    assertThat(actual.entry()).hasSize(3);
  }
}
