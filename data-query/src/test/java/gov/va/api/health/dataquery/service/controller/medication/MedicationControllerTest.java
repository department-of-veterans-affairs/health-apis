package gov.va.api.health.dataquery.service.controller.medication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dataquery.api.resources.Medication;
import gov.va.api.health.dataquery.api.resources.Medication.Bundle;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.dvp.cdw.xsd.model.CdwMedication101Root;
import gov.va.dvp.cdw.xsd.model.CdwMedication101Root.CdwMedications;
import gov.va.dvp.cdw.xsd.model.CdwMedication101Root.CdwMedications.CdwMedication;
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
public class MedicationControllerTest {
  @Mock MrAndersonClient client;

  @Mock MedicationController.Transformer tx;

  MedicationController controller;
  @Mock Bundler bundler;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    controller = new MedicationController(tx, client, bundler);
  }

  private void assertSearch(
      Supplier<Medication.Bundle> invocation, MultiValueMap<String, String> params) {
    CdwMedication101Root root = new CdwMedication101Root();
    root.setPageNumber(BigInteger.valueOf(1));
    root.setRecordsPerPage(BigInteger.valueOf(10));
    root.setRecordCount(BigInteger.valueOf(3));
    root.setMedications(new CdwMedications());
    CdwMedication xmlMedication1 = new CdwMedication();
    CdwMedication xmlMedication2 = new CdwMedication();
    CdwMedication xmlMedication3 = new CdwMedication();
    root.getMedications()
        .getMedication()
        .addAll(Arrays.asList(xmlMedication1, xmlMedication2, xmlMedication3));
    Medication medication1 = Medication.builder().build();
    Medication medication2 = Medication.builder().build();
    Medication medication3 = Medication.builder().build();
    when(tx.apply(xmlMedication1)).thenReturn(medication1);
    when(tx.apply(xmlMedication2)).thenReturn(medication2);
    when(tx.apply(xmlMedication3)).thenReturn(medication3);
    when(client.search(Mockito.any())).thenReturn(root);

    Medication.Bundle mockBundle = new Medication.Bundle();
    when(bundler.bundle(Mockito.any())).thenReturn(mockBundle);

    Medication.Bundle actual = invocation.get();

    assertThat(actual).isSameAs(mockBundle);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<BundleContext<CdwMedication, Medication, Medication.Entry, Medication.Bundle>>
        captor = ArgumentCaptor.forClass(BundleContext.class);

    verify(bundler).bundle(captor.capture());

    LinkConfig expectedLinkConfig =
        LinkConfig.builder()
            .page(1)
            .recordsPerPage(10)
            .totalRecords(3)
            .path("Medication")
            .queryParams(params)
            .build();
    assertThat(captor.getValue().linkConfig()).isEqualTo(expectedLinkConfig);
    assertThat(captor.getValue().xmlItems()).isEqualTo(root.getMedications().getMedication());
    assertThat(captor.getValue().newBundle().get()).isInstanceOf(Medication.Bundle.class);
    assertThat(captor.getValue().newEntry().get()).isInstanceOf(Medication.Entry.class);
    assertThat(captor.getValue().transformer()).isSameAs(tx);
  }

  private Bundle bundleOf(Medication resource) {
    return Bundle.builder()
        .type(BundleType.searchset)
        .resourceType("Bundle")
        .entry(
            Collections.singletonList(
                Medication.Entry.builder()
                    .fullUrl("http://example.com")
                    .resource(resource)
                    .build()))
        .build();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void read() {
    CdwMedication101Root root = new CdwMedication101Root();
    root.setMedications(new CdwMedications());
    CdwMedication101Root.CdwMedications.CdwMedication xmlMedication =
        new CdwMedication101Root.CdwMedications.CdwMedication();
    root.getMedications().getMedication().add(xmlMedication);
    Medication medication = Medication.builder().build();
    when(client.search(Mockito.any())).thenReturn(root);
    when(tx.apply(xmlMedication)).thenReturn(medication);
    Medication actual = controller.read("hello");
    assertThat(actual).isSameAs(medication);
    ArgumentCaptor<Query<CdwMedication101Root>> captor = ArgumentCaptor.forClass(Query.class);
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
  @SneakyThrows
  public void validateAcceptsValidBundle() {
    Medication resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-medication-1.01.json"), Medication.class);

    Bundle bundle = bundleOf(resource);
    assertThat(controller.validate(bundle)).isEqualTo(Validator.ok());
  }

  @Test(expected = ConstraintViolationException.class)
  @SneakyThrows
  public void validateThrowsExceptionForInvalidBundle() {
    Medication resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-medication-1.01.json"), Medication.class);
    resource.resourceType(null);

    Bundle bundle = bundleOf(resource);
    controller.validate(bundle);
  }
}
