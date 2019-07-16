package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;
import javax.validation.ConstraintViolationException;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.MultiValueMap;

public class DiagnosticReportControllerTest {
  @Mock MrAndersonClient mraClient;
  @Mock DiagnosticReportController.Transformer tx;
  @Mock Bundler bundler;

  DiagnosticReportController controller;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    controller = new DiagnosticReportController(tx, mraClient, bundler, null, null);
  }

  private void assertSearch(
      Supplier<DiagnosticReport.Bundle> invocation, MultiValueMap<String, String> params) {
    CdwDiagnosticReport102Root root = new CdwDiagnosticReport102Root();
    root.setPageNumber(BigInteger.valueOf(1));
    root.setRecordsPerPage(BigInteger.valueOf(10));
    root.setRecordCount(BigInteger.valueOf(3));
    root.setDiagnosticReports(new CdwDiagnosticReport102Root.CdwDiagnosticReports());
    CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport xmlDr1 =
        new CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport();
    CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport xmlDr2 =
        new CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport();
    CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport xmlDr3 =
        new CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport();
    root.getDiagnosticReports().getDiagnosticReport().addAll(Arrays.asList(xmlDr1, xmlDr2, xmlDr3));
    DiagnosticReport dr1 = DiagnosticReport.builder().build();
    DiagnosticReport dr2 = DiagnosticReport.builder().build();
    DiagnosticReport dr3 = DiagnosticReport.builder().build();
    when(tx.apply(xmlDr1)).thenReturn(dr1);
    when(tx.apply(xmlDr2)).thenReturn(dr2);
    when(tx.apply(xmlDr3)).thenReturn(dr3);
    when(mraClient.search(any())).thenReturn(root);

    DiagnosticReport.Bundle mockBundle = new DiagnosticReport.Bundle();
    when(bundler.bundle(any())).thenReturn(mockBundle);

    DiagnosticReport.Bundle actual = invocation.get();

    assertThat(actual).isSameAs(mockBundle);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<
            Bundler.BundleContext<
                CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport,
                DiagnosticReport, DiagnosticReport.Entry, DiagnosticReport.Bundle>>
        captor = ArgumentCaptor.forClass(Bundler.BundleContext.class);

    verify(bundler).bundle(captor.capture());

    LinkConfig expectedLinkConfig =
        LinkConfig.builder()
            .page(1)
            .recordsPerPage(10)
            .totalRecords(3)
            .path("DiagnosticReport")
            .queryParams(params)
            .build();
    assertThat(captor.getValue().linkConfig()).isEqualTo(expectedLinkConfig);
    assertThat(captor.getValue().xmlItems())
        .isEqualTo(root.getDiagnosticReports().getDiagnosticReport());
    assertThat(captor.getValue().newBundle().get()).isInstanceOf(DiagnosticReport.Bundle.class);
    assertThat(captor.getValue().newEntry().get()).isInstanceOf(DiagnosticReport.Entry.class);
    assertThat(captor.getValue().transformer()).isSameAs(tx);
  }

  private DiagnosticReport.Bundle bundleOf(DiagnosticReport resource) {
    return DiagnosticReport.Bundle.builder()
        .type(BundleType.searchset)
        .resourceType("Bundle")
        .entry(
            Collections.singletonList(
                DiagnosticReport.Entry.builder()
                    .fullUrl("http://example.com")
                    .resource(resource)
                    .build()))
        .build();
  }

  @Test
  public void read() {
    CdwDiagnosticReport102Root.CdwDiagnosticReports wrapper =
        new CdwDiagnosticReport102Root.CdwDiagnosticReports();
    CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport cdwDr =
        new CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport();
    wrapper.getDiagnosticReport().add(cdwDr);

    CdwDiagnosticReport102Root root = new CdwDiagnosticReport102Root();
    root.setDiagnosticReports(wrapper);

    when(mraClient.search(any())).thenReturn(root);

    DiagnosticReport dr = DiagnosticReport.builder().build();
    when(tx.apply(cdwDr)).thenReturn(dr);

    DiagnosticReport actual = controller.read("", "hello");
    assertThat(actual).isSameAs(dr);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Query<CdwDiagnosticReport102Root>> captor = ArgumentCaptor.forClass(Query.class);
    verify(mraClient).search(captor.capture());
    assertThat(captor.getValue().parameters()).isEqualTo(Parameters.forIdentity("hello"));
  }

  @Test
  public void searchById() {
    controller =
        new DiagnosticReportController(
            tx, mraClient, new Bundler(new ConfigurableBaseUrlPageLinks("", "")), null, null);

    CdwDiagnosticReport102Root.CdwDiagnosticReports wrapper =
        new CdwDiagnosticReport102Root.CdwDiagnosticReports();
    CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport cdwDr =
        new CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport();
    wrapper.getDiagnosticReport().add(cdwDr);

    CdwDiagnosticReport102Root root = new CdwDiagnosticReport102Root();
    root.setDiagnosticReports(wrapper);

    when(mraClient.search(any())).thenReturn(root);

    DiagnosticReport dr = DiagnosticReport.builder().build();
    when(tx.apply(cdwDr)).thenReturn(dr);

    DiagnosticReport.Bundle actual = controller.searchById("", "me", 1, 10);
    System.out.println(actual);
    assertThat(Iterables.getOnlyElement(actual.entry()).resource()).isSameAs(dr);
  }

  @Test
  public void searchByIdentifier() {
    controller =
        new DiagnosticReportController(
            tx, mraClient, new Bundler(new ConfigurableBaseUrlPageLinks("", "")), null, null);

    CdwDiagnosticReport102Root.CdwDiagnosticReports wrapper =
        new CdwDiagnosticReport102Root.CdwDiagnosticReports();
    CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport cdwDr =
        new CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport();
    wrapper.getDiagnosticReport().add(cdwDr);

    CdwDiagnosticReport102Root root = new CdwDiagnosticReport102Root();
    root.setDiagnosticReports(wrapper);

    when(mraClient.search(any())).thenReturn(root);

    DiagnosticReport dr = DiagnosticReport.builder().build();
    when(tx.apply(cdwDr)).thenReturn(dr);

    DiagnosticReport.Bundle actual = controller.searchByIdentifier("", "me", 1, 10);
    System.out.println(actual);
    assertThat(Iterables.getOnlyElement(actual.entry()).resource()).isSameAs(dr);
  }

  @Test
  public void searchByPatient() {
    assertSearch(
        () -> controller.searchByPatient("", "me", 1, 10),
        Parameters.builder().add("patient", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  public void searchByPatientAndCategoryAndDateRange() {
    assertSearch(
        () ->
            controller.searchByPatientAndCategoryAndDate(
                "", "me", "foo", new String[] {"1000", "2000"}, 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .add("category", "foo")
            .addAll("date", "1000", "2000")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  public void searchByPatientAndCategoryAndOneDate() {
    assertSearch(
        () ->
            controller.searchByPatientAndCategoryAndDate(
                "", "me", "foo", new String[] {"1000"}, 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .add("category", "foo")
            .addAll("date", "1000")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  public void searchByPatientAndCategoryandNoDate() {
    assertSearch(
        () -> controller.searchByPatientAndCategoryAndDate("", "me", "example", null, 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .add("category", "example")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  public void searchByPatientAndCode() {
    assertSearch(
        () -> controller.searchByPatientAndCode("", "me", "foo", 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .add("code", "foo")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  public void searchReturnsEmptyResults() {
    CdwDiagnosticReport102Root.CdwDiagnosticReports wrapper =
        new CdwDiagnosticReport102Root.CdwDiagnosticReports();
    CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport cdwDr =
        new CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport();
    wrapper.getDiagnosticReport().add(cdwDr);

    CdwDiagnosticReport102Root root = new CdwDiagnosticReport102Root();
    root.setDiagnosticReports(wrapper);
    root.setPageNumber(BigInteger.valueOf(1));
    root.setRecordsPerPage(BigInteger.valueOf(10));
    root.setRecordCount(BigInteger.valueOf(0));

    when(mraClient.search(any())).thenReturn(root);

    DiagnosticReport.Bundle mockBundle = new DiagnosticReport.Bundle();
    when(bundler.bundle(any())).thenReturn(mockBundle);

    DiagnosticReport.Bundle actual = controller.searchById("", "me", 1, 10);
    assertThat(actual).isSameAs(mockBundle);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<
            Bundler.BundleContext<
                CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport,
                DiagnosticReport, DiagnosticReport.Entry, DiagnosticReport.Bundle>>
        captor = ArgumentCaptor.forClass(Bundler.BundleContext.class);

    verify(bundler).bundle(captor.capture());
    LinkConfig expectedLinkConfig =
        LinkConfig.builder()
            .page(1)
            .recordsPerPage(10)
            .totalRecords(0)
            .path("DiagnosticReport")
            .queryParams(
                Parameters.builder()
                    .add("identifier", "me")
                    .add("page", 1)
                    .add("_count", 10)
                    .build())
            .build();
    assertThat(captor.getValue().linkConfig()).isEqualTo(expectedLinkConfig);
    assertThat(captor.getValue().xmlItems()).isEmpty();
  }

  @Test
  @SneakyThrows
  public void validateAcceptsValidBundle() {
    DiagnosticReport resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-diagnosticreport-1.02.json"),
                DiagnosticReport.class);

    DiagnosticReport.Bundle bundle = bundleOf(resource);
    assertThat(controller.validate(bundle)).isEqualTo(Validator.ok());
  }

  @SneakyThrows
  @Test(expected = ConstraintViolationException.class)
  public void validateThrowsExceptionForInvalidBundle() {
    DiagnosticReport resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-diagnosticreport-1.02.json"),
                DiagnosticReport.class);
    resource.resourceType(null);

    DiagnosticReport.Bundle bundle = bundleOf(resource);
    controller.validate(bundle);
  }
}
