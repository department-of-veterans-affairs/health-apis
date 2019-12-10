package gov.va.api.health.dataquery.service.controller.observation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.argonaut.api.resources.Observation.Bundle;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.Dstu2Validator;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation;
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

@SuppressWarnings("WeakerAccess")
public class ObservationControllerTest {
  @Mock MrAndersonClient client;

  @Mock ObservationController.Transformer tx;

  ObservationController controller;

  @Mock Dstu2Bundler bundler;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    controller = new ObservationController(false, tx, client, bundler, null, null);
  }

  private void assertSearch(Supplier<Bundle> invocation, MultiValueMap<String, String> params) {
    CdwObservation104Root root = new CdwObservation104Root();
    root.setPageNumber(BigInteger.valueOf(1));
    root.setRecordsPerPage(BigInteger.valueOf(10));
    root.setRecordCount(BigInteger.valueOf(3));
    root.setObservations(new CdwObservations());
    CdwObservation cdwItem1 = new CdwObservation();
    CdwObservation cdwItem2 = new CdwObservation();
    CdwObservation cdwItem3 = new CdwObservation();
    root.getObservations().getObservation().addAll(Arrays.asList(cdwItem1, cdwItem2, cdwItem3));
    Observation observation1 = Observation.builder().build();
    Observation observation2 = Observation.builder().build();
    Observation observation3 = Observation.builder().build();
    when(tx.apply(cdwItem1)).thenReturn(observation1);
    when(tx.apply(cdwItem2)).thenReturn(observation2);
    when(tx.apply(cdwItem3)).thenReturn(observation3);
    when(client.search(Mockito.any())).thenReturn(root);

    Bundle mockBundle = new Bundle();
    when(bundler.bundle(Mockito.any())).thenReturn(mockBundle);

    Bundle actual = invocation.get();

    assertThat(actual).isSameAs(mockBundle);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<BundleContext<CdwObservation, Observation, Observation.Entry, Bundle>> captor =
        ArgumentCaptor.forClass(BundleContext.class);

    verify(bundler).bundle(captor.capture());

    LinkConfig expectedLinkConfig =
        LinkConfig.builder()
            .page(1)
            .recordsPerPage(10)
            .totalRecords(3)
            .path("Observation")
            .queryParams(params)
            .build();
    assertThat(captor.getValue().linkConfig()).isEqualTo(expectedLinkConfig);
    assertThat(captor.getValue().xmlItems()).isEqualTo(root.getObservations().getObservation());
    assertThat(captor.getValue().newBundle().get()).isInstanceOf(Bundle.class);
    assertThat(captor.getValue().newEntry().get()).isInstanceOf(Observation.Entry.class);
    assertThat(captor.getValue().transformer()).isSameAs(tx);
  }

  private Bundle bundleOf(Observation resource) {
    return Bundle.builder()
        .type(BundleType.searchset)
        .resourceType("Bundle")
        .entry(
            Collections.singletonList(
                Observation.Entry.builder()
                    .fullUrl("http://example.com")
                    .resource(resource)
                    .build()))
        .build();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void read() {
    CdwObservation104Root root = new CdwObservation104Root();
    root.setObservations(new CdwObservations());
    CdwObservation xmlObservation = new CdwObservation();
    root.getObservations().getObservation().add(xmlObservation);
    Observation item = Observation.builder().build();
    when(client.search(Mockito.any())).thenReturn(root);
    when(tx.apply(xmlObservation)).thenReturn(item);
    Observation actual = controller.read("", "hello");
    assertThat(actual).isSameAs(item);
    ArgumentCaptor<Query<CdwObservation104Root>> captor = ArgumentCaptor.forClass(Query.class);
    verify(client).search(captor.capture());
    assertThat(captor.getValue().parameters()).isEqualTo(Parameters.forIdentity("hello"));
  }

  @Test
  public void searchById() {
    assertSearch(
        () -> controller.searchById("", "me", 1, 10),
        Parameters.builder().add("identifier", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  public void searchByIdentifier() {
    assertSearch(
        () -> controller.searchByIdentifier("", "me", 1, 10),
        Parameters.builder().add("identifier", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  public void searchByPatient() {
    assertSearch(
        () -> controller.searchByPatient("", "me", 1, 10),
        Parameters.builder().add("patient", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  public void searchByPatientAndCategoryDateRange() {
    assertSearch(
        () ->
            controller.searchByPatientAndCategory(
                "", "me", "laboratory,vital-signs", new String[] {"2005", "2006"}, 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .add("category", "laboratory,vital-signs")
            .addAll("date", "2005", "2006")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  public void searchByPatientAndCategoryNoDate() {
    assertSearch(
        () ->
            controller.searchByPatientAndCategory("", "me", "laboratory,vital-signs", null, 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .add("category", "laboratory,vital-signs")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  public void searchByPatientAndCategoryOneDate() {
    assertSearch(
        () ->
            controller.searchByPatientAndCategory(
                "", "me", "laboratory,vital-signs", new String[] {"2005"}, 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .add("category", "laboratory,vital-signs")
            .addAll("date", "2005")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  public void searchByPatientAndCode() {
    assertSearch(
        () -> controller.searchByPatientAndCode("", "me", "123,456", 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .add("code", "123,456")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  @SneakyThrows
  public void validateAcceptsValidBundle() {
    Observation resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-observation-1.04.json"),
                Observation.class);

    Bundle bundle = bundleOf(resource);
    assertThat(controller.validate(bundle)).isEqualTo(Dstu2Validator.ok());
  }

  @SneakyThrows
  @Test(expected = ConstraintViolationException.class)
  public void validateThrowsExceptionForInvalidBundle() {
    Observation resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-observation-1.04.json"),
                Observation.class);
    resource.resourceType(null);

    Bundle bundle = bundleOf(resource);
    controller.validate(bundle);
  }
}
