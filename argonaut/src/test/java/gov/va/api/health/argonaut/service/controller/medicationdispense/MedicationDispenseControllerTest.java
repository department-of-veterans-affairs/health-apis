package gov.va.api.health.argonaut.service.controller.medicationdispense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.argonaut.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.argonaut.api.resources.MedicationDispense;
import gov.va.api.health.argonaut.api.resources.MedicationDispense.Bundle;
import gov.va.api.health.argonaut.api.resources.MedicationDispense.Entry;
import gov.va.api.health.argonaut.service.controller.Bundler;
import gov.va.api.health.argonaut.service.controller.Bundler.BundleContext;
import gov.va.api.health.argonaut.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.argonaut.service.controller.Parameters;
import gov.va.api.health.argonaut.service.controller.Validator;
import gov.va.api.health.argonaut.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.argonaut.service.mranderson.client.Query;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses.CdwMedicationDispense;
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
public class MedicationDispenseControllerTest {

  @Mock MrAndersonClient client;

  @Mock MedicationDispenseController.Transformer tx;

  MedicationDispenseController controller;

  @Mock Bundler bundler;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    controller = new MedicationDispenseController(tx, client, bundler);
  }

  private void assertSearch(Supplier<Bundle> invocation, MultiValueMap<String, String> params) {
    CdwMedicationDispense100Root root = new CdwMedicationDispense100Root();
    root.setPageNumber(Integer.valueOf(1));
    root.setRecordsPerPage(Integer.valueOf(10));
    root.setRecordCount(Integer.valueOf(3));
    root.setMedicationDispenses(new CdwMedicationDispenses());
    CdwMedicationDispense cdwItem1 = new CdwMedicationDispense();
    CdwMedicationDispense cdwItem2 = new CdwMedicationDispense();
    CdwMedicationDispense cdwItem3 = new CdwMedicationDispense();
    root.getMedicationDispenses()
        .getMedicationDispense()
        .addAll(Arrays.asList(cdwItem1, cdwItem2, cdwItem3));
    MedicationDispense item1 = MedicationDispense.builder().build();
    MedicationDispense item2 = MedicationDispense.builder().build();
    MedicationDispense item3 = MedicationDispense.builder().build();
    when(tx.apply(cdwItem1)).thenReturn(item1);
    when(tx.apply(cdwItem2)).thenReturn(item2);
    when(tx.apply(cdwItem3)).thenReturn(item3);
    when(client.search(Mockito.any())).thenReturn(root);
    Bundle mockBundle = new Bundle();
    when(bundler.bundle(Mockito.any())).thenReturn(mockBundle);
    Bundle actual = invocation.get();
    assertThat(actual).isSameAs(mockBundle);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<BundleContext<CdwMedicationDispense, MedicationDispense, Entry, Bundle>> captor =
        ArgumentCaptor.forClass(BundleContext.class);
    verify(bundler).bundle(captor.capture());
    LinkConfig expectedLinkConfig =
        LinkConfig.builder()
            .page(1)
            .recordsPerPage(10)
            .totalRecords(3)
            .path("MedicationDispense")
            .queryParams(params)
            .build();
    assertThat(captor.getValue().linkConfig()).isEqualTo(expectedLinkConfig);
    assertThat(captor.getValue().xmlItems())
        .isEqualTo(root.getMedicationDispenses().getMedicationDispense());
    assertThat(captor.getValue().newBundle().get()).isInstanceOf(Bundle.class);
    assertThat(captor.getValue().newEntry().get()).isInstanceOf(Entry.class);
    assertThat(captor.getValue().transformer()).isSameAs(tx);
  }

  private Bundle bundleOf(MedicationDispense resource) {
    return Bundle.builder()
        .type(BundleType.searchset)
        .resourceType("Bundle")
        .entry(
            Collections.singletonList(
                Entry.builder().fullUrl("http://example.com").resource(resource).build()))
        .build();
  }

  @Test
  public void read() {
    CdwMedicationDispense100Root root = new CdwMedicationDispense100Root();
    root.setMedicationDispenses(new CdwMedicationDispenses());
    CdwMedicationDispense xmlMedicationDispense = new CdwMedicationDispense();
    root.getMedicationDispenses().getMedicationDispense().add(xmlMedicationDispense);
    MedicationDispense item = MedicationDispense.builder().build();
    when(client.search(Mockito.any())).thenReturn(root);
    when(tx.apply(xmlMedicationDispense)).thenReturn(item);
    MedicationDispense actual = controller.read("hello");
    assertThat(actual).isSameAs(item);
    ArgumentCaptor<Query<CdwMedicationDispense100Root>> captor =
        ArgumentCaptor.forClass(Query.class);
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
  public void searchByPatientAndStatus() {
    assertSearch(
        () -> controller.searchByPatientAndStatus("me", "stopped,completed", 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .add("status", "stopped,completed")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  public void searchByPatientAndType() {
    assertSearch(
        () -> controller.searchByPatientAndType("me", "FF,UD", 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .add("type", "FF,UD")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  @SneakyThrows
  public void validateAcceptsValidBundle() {
    MedicationDispense resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/medicationdispense-1.00.json"),
                MedicationDispense.class);
    Bundle bundle = bundleOf(resource);
    assertThat(controller.validate(bundle)).isEqualTo(Validator.ok());
  }

  @Test(expected = ConstraintViolationException.class)
  @SneakyThrows
  public void validateThrowsExceptionForInvalidBundle() {
    MedicationDispense resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/medicationdispense-1.00.json"),
                MedicationDispense.class);
    resource.resourceType(null);
    Bundle bundle = bundleOf(resource);
    controller.validate(bundle);
  }
}
