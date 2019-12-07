package gov.va.api.health.dataquery.service.controller.medicationorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.argonaut.api.resources.MedicationOrder.Bundle;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.dvp.cdw.xsd.model.CdwMedicationOrder103Root;
import gov.va.dvp.cdw.xsd.model.CdwMedicationOrder103Root.CdwMedicationOrders;
import gov.va.dvp.cdw.xsd.model.CdwMedicationOrder103Root.CdwMedicationOrders.CdwMedicationOrder;
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

public class MedicationOrderControllerTest {
  @Mock MrAndersonClient client;

  @Mock MedicationOrderController.Transformer tx;

  MedicationOrderController controller;
  @Mock Bundler bundler;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    controller = new MedicationOrderController(false, tx, client, bundler, null, null);
  }

  private void assertSearch(Supplier<Bundle> invocation, MultiValueMap<String, String> params) {
    CdwMedicationOrder103Root root = new CdwMedicationOrder103Root();
    root.setPageNumber(1);
    root.setRecordsPerPage(10);
    root.setRecordCount(3);
    root.setMedicationOrders(new CdwMedicationOrders());
    CdwMedicationOrder cdwItem1 = new CdwMedicationOrder();
    CdwMedicationOrder cdwItem2 = new CdwMedicationOrder();
    CdwMedicationOrder cdwItem3 = new CdwMedicationOrder();
    root.getMedicationOrders()
        .getMedicationOrder()
        .addAll(Arrays.asList(cdwItem1, cdwItem2, cdwItem3));
    MedicationOrder medicationOrder1 = MedicationOrder.builder().build();
    MedicationOrder medicationOrder2 = MedicationOrder.builder().build();
    MedicationOrder medicationOrder3 = MedicationOrder.builder().build();
    when(tx.apply(cdwItem1)).thenReturn(medicationOrder1);
    when(tx.apply(cdwItem2)).thenReturn(medicationOrder2);
    when(tx.apply(cdwItem3)).thenReturn(medicationOrder3);
    when(client.search(Mockito.any())).thenReturn(root);

    Bundle mockBundle = new Bundle();
    when(bundler.bundle(Mockito.any())).thenReturn(mockBundle);

    Bundle actual = invocation.get();

    assertThat(actual).isSameAs(mockBundle);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<
            BundleContext<CdwMedicationOrder, MedicationOrder, MedicationOrder.Entry, Bundle>>
        captor = ArgumentCaptor.forClass(BundleContext.class);

    verify(bundler).bundle(captor.capture());

    LinkConfig expectedLinkConfig =
        LinkConfig.builder()
            .page(1)
            .recordsPerPage(10)
            .totalRecords(3)
            .path("MedicationOrder")
            .queryParams(params)
            .build();
    assertThat(captor.getValue().linkConfig()).isEqualTo(expectedLinkConfig);
    assertThat(captor.getValue().xmlItems())
        .isEqualTo(root.getMedicationOrders().getMedicationOrder());
    assertThat(captor.getValue().newBundle().get()).isInstanceOf(Bundle.class);
    assertThat(captor.getValue().newEntry().get()).isInstanceOf(MedicationOrder.Entry.class);
    assertThat(captor.getValue().transformer()).isSameAs(tx);
  }

  private Bundle bundleOf(MedicationOrder resource) {
    return Bundle.builder()
        .type(BundleType.searchset)
        .resourceType("Bundle")
        .entry(
            Collections.singletonList(
                MedicationOrder.Entry.builder()
                    .fullUrl("http://example.com")
                    .resource(resource)
                    .build()))
        .build();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void read() {
    CdwMedicationOrder103Root root = new CdwMedicationOrder103Root();
    root.setMedicationOrders(new CdwMedicationOrders());
    CdwMedicationOrder xmlMedicationOrder = new CdwMedicationOrder();
    root.getMedicationOrders().getMedicationOrder().add(xmlMedicationOrder);
    MedicationOrder item = MedicationOrder.builder().build();
    when(client.search(Mockito.any())).thenReturn(root);
    when(tx.apply(xmlMedicationOrder)).thenReturn(item);
    MedicationOrder actual = controller.read("false", "hello");
    assertThat(actual).isSameAs(item);
    ArgumentCaptor<Query<CdwMedicationOrder103Root>> captor = ArgumentCaptor.forClass(Query.class);
    verify(client).search(captor.capture());
    assertThat(captor.getValue().parameters()).isEqualTo(Parameters.forIdentity("hello"));
  }

  @Test
  public void searchById() {
    assertSearch(
        () -> controller.searchById("false", "me", 1, 10),
        Parameters.builder().add("identifier", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  public void searchByIdentifier() {
    assertSearch(
        () -> controller.searchByIdentifier("false", "me", 1, 10),
        Parameters.builder().add("identifier", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  public void searchByPatient() {
    assertSearch(
        () -> controller.searchByPatient("false", "me", 1, 10),
        Parameters.builder().add("patient", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  @SneakyThrows
  public void validateAcceptsValidBundle() {
    MedicationOrder resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-medicationorder-1.04.json"),
                MedicationOrder.class);

    Bundle bundle = bundleOf(resource);
    assertThat(controller.validate(bundle)).isEqualTo(Validator.ok());
  }

  @Test(expected = ConstraintViolationException.class)
  @SneakyThrows
  public void validateThrowsExceptionForInvalidBundle() {
    MedicationOrder resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-medicationorder-1.04.json"),
                MedicationOrder.class);
    resource.resourceType(null);

    Bundle bundle = bundleOf(resource);
    controller.validate(bundle);
  }
}
