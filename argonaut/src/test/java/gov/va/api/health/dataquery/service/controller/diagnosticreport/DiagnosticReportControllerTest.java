package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dataquery.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.api.resources.DiagnosticReport.Bundle;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root.CdwDiagnosticReports;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport;
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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.util.MultiValueMap;

public class DiagnosticReportControllerTest {

  @Mock MrAndersonClient client;
  @Mock DiagnosticReportController.Transformer tx;
  @Mock Bundler bundler;

  DiagnosticReportController controller;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    controller = new DiagnosticReportController(tx, client, bundler);
  }

  private void assertSearch(Supplier<Bundle> invocation, MultiValueMap<String, String> params) {
    CdwDiagnosticReport102Root root = new CdwDiagnosticReport102Root();
    root.setPageNumber(BigInteger.valueOf(1));
    root.setRecordsPerPage(BigInteger.valueOf(10));
    root.setRecordCount(BigInteger.valueOf(3));
    root.setDiagnosticReports(new CdwDiagnosticReports());
    CdwDiagnosticReport xmlDr1 = new CdwDiagnosticReport();
    CdwDiagnosticReport xmlDr2 = new CdwDiagnosticReport();
    CdwDiagnosticReport xmlDr3 = new CdwDiagnosticReport();
    root.getDiagnosticReports().getDiagnosticReport().addAll(Arrays.asList(xmlDr1, xmlDr2, xmlDr3));
    DiagnosticReport dr1 = DiagnosticReport.builder().build();
    DiagnosticReport dr2 = DiagnosticReport.builder().build();
    DiagnosticReport dr3 = DiagnosticReport.builder().build();
    when(tx.apply(xmlDr1)).thenReturn(dr1);
    when(tx.apply(xmlDr2)).thenReturn(dr2);
    when(tx.apply(xmlDr3)).thenReturn(dr3);
    when(client.search(Mockito.any())).thenReturn(root);

    Bundle mockBundle = new Bundle();
    when(bundler.bundle(Mockito.any())).thenReturn(mockBundle);

    Bundle actual = invocation.get();

    assertThat(actual).isSameAs(mockBundle);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<
            BundleContext<
                CdwDiagnosticReport, DiagnosticReport, DiagnosticReport.Entry,
                DiagnosticReport.Bundle>>
        captor = ArgumentCaptor.forClass(BundleContext.class);

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

  private Bundle bundleOf(DiagnosticReport resource) {
    return Bundle.builder()
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
    CdwDiagnosticReport102Root root = new CdwDiagnosticReport102Root();
    root.setDiagnosticReports(new CdwDiagnosticReport102Root.CdwDiagnosticReports());
    CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport cdwDR =
        new CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport();
    root.getDiagnosticReports().getDiagnosticReport().add(cdwDR);
    DiagnosticReport dr = DiagnosticReport.builder().build();
    when(client.search(Mockito.any())).thenReturn(root);
    when(tx.apply(cdwDR)).thenReturn(dr);
    DiagnosticReport actual = controller.read("hello");
    assertThat(actual).isSameAs(dr);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Query<CdwDiagnosticReport102Root>> captor = ArgumentCaptor.forClass(Query.class);
    verify(client).search(captor.capture());
    assertThat(captor.getValue().parameters()).isEqualTo(Parameters.forIdentity("hello"));
  }

  @Test
  public void searchById() {
    assertSearch(
        () -> controller.searchById("me", 1, 10),
        Parameters.builder().add("identifier", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  public void searchByIdAndCategory() {
    assertSearch(
        () -> controller.searchByPatientAndCategory("me", "example", 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .add("category", "example")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  public void searchByIdentifier() {
    assertSearch(
        () -> controller.searchByIdentifier("me", 1, 10),
        Parameters.builder().add("identifier", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  public void searchByPatient() {
    assertSearch(
        () -> controller.searchByPatient("me", 1, 10),
        Parameters.builder().add("patient", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  public void searchByPatientAndCategoryAndDate() {
    assertSearch(
        () ->
            controller.searchByPatientAndCategoryAndDate(
                "me", "foo", new String[] {"1000", "2000"}, 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .add("category", "foo")
            .addAll("date", "1000", "2000")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  public void searchByPatientAndCode() {
    assertSearch(
        () -> controller.searchByPatientAndCode("me", "foo", 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .add("code", "foo")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  public void searchReturnsEmptyResults() {
    CdwDiagnosticReport102Root root = new CdwDiagnosticReport102Root();
    root.setPageNumber(BigInteger.valueOf(1));
    root.setRecordsPerPage(BigInteger.valueOf(10));
    root.setRecordCount(BigInteger.valueOf(0));
    when(client.search(Mockito.any())).thenReturn(root);
    Bundle mockBundle = new Bundle();
    when(bundler.bundle(Mockito.any())).thenReturn(mockBundle);
    Bundle actual = controller.searchById("me", 1, 10);
    assertThat(actual).isSameAs(mockBundle);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<
            BundleContext<
                CdwDiagnosticReport, DiagnosticReport, DiagnosticReport.Entry,
                DiagnosticReport.Bundle>>
        captor = ArgumentCaptor.forClass(BundleContext.class);

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

    Bundle bundle = bundleOf(resource);
    assertThat(controller.validate(bundle)).isEqualTo(Validator.ok());
  }

  @Test(expected = ConstraintViolationException.class)
  @SneakyThrows
  public void validateThrowsExceptionForInvalidBundle() {
    DiagnosticReport resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-diagnosticreport-1.02.json"),
                DiagnosticReport.class);
    resource.resourceType(null);

    Bundle bundle = bundleOf(resource);
    controller.validate(bundle);
  }
}
