package gov.va.api.health.dataquery.service.controller.practitioner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.Dstu2Validator;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import gov.va.api.health.dstu2.api.resources.Practitioner.Bundle;
import gov.va.api.health.dstu2.api.resources.Practitioner.Entry;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner;
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
public class PractitionerControllerTest {
  @Mock MrAndersonClient client;

  @Mock PractitionerController.Transformer tx;

  PractitionerController controller;
  @Mock Dstu2Bundler bundler;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    controller = new PractitionerController(false, tx, client, bundler, null, null);
  }

  private void assertSearch(Supplier<Bundle> invocation, MultiValueMap<String, String> params) {
    CdwPractitioner100Root root = new CdwPractitioner100Root();
    root.setPageNumber(BigInteger.valueOf(1));
    root.setRecordsPerPage(BigInteger.valueOf(10));
    root.setRecordCount(BigInteger.valueOf(3));
    root.setPractitioners(new CdwPractitioners());
    CdwPractitioner cdwItem1 = new CdwPractitioner();
    CdwPractitioner cdwItem2 = new CdwPractitioner();
    CdwPractitioner cdwItem3 = new CdwPractitioner();
    root.getPractitioners().getPractitioner().addAll(Arrays.asList(cdwItem1, cdwItem2, cdwItem3));
    Practitioner practitioner1 = Practitioner.builder().build();
    Practitioner practitioner2 = Practitioner.builder().build();
    Practitioner practitioner3 = Practitioner.builder().build();
    when(tx.apply(cdwItem1)).thenReturn(practitioner1);
    when(tx.apply(cdwItem2)).thenReturn(practitioner2);
    when(tx.apply(cdwItem3)).thenReturn(practitioner3);
    when(client.search(Mockito.any())).thenReturn(root);

    Bundle mockBundle = new Bundle();
    when(bundler.bundle(Mockito.any())).thenReturn(mockBundle);

    Bundle actual = invocation.get();

    assertThat(actual).isSameAs(mockBundle);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<BundleContext<CdwPractitioner, Practitioner, Entry, Bundle>> captor =
        ArgumentCaptor.forClass(BundleContext.class);

    verify(bundler).bundle(captor.capture());

    LinkConfig expectedLinkConfig =
        LinkConfig.builder()
            .page(1)
            .recordsPerPage(10)
            .totalRecords(3)
            .path("Practitioner")
            .queryParams(params)
            .build();
    assertThat(captor.getValue().linkConfig()).isEqualTo(expectedLinkConfig);
    assertThat(captor.getValue().xmlItems()).isEqualTo(root.getPractitioners().getPractitioner());
    assertThat(captor.getValue().newBundle().get()).isInstanceOf(Bundle.class);
    assertThat(captor.getValue().newEntry().get()).isInstanceOf(Practitioner.Entry.class);
    assertThat(captor.getValue().transformer()).isSameAs(tx);
  }

  private Bundle bundleOf(Practitioner resource) {
    return Bundle.builder()
        .type(BundleType.searchset)
        .resourceType("Bundle")
        .entry(
            Collections.singletonList(
                Practitioner.Entry.builder()
                    .fullUrl("http://example.com")
                    .resource(resource)
                    .build()))
        .build();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void read() {
    CdwPractitioner100Root root = new CdwPractitioner100Root();
    root.setPractitioners(new CdwPractitioners());
    CdwPractitioner xmlPractitioner = new CdwPractitioner();
    root.getPractitioners().getPractitioner().add(xmlPractitioner);
    Practitioner item = Practitioner.builder().build();
    when(client.search(Mockito.any())).thenReturn(root);
    when(tx.apply(xmlPractitioner)).thenReturn(item);
    Practitioner actual = controller.read("false", "hello");
    assertThat(actual).isSameAs(item);
    ArgumentCaptor<Query<CdwPractitioner100Root>> captor = ArgumentCaptor.forClass(Query.class);
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
  @SneakyThrows
  public void validateAcceptsValidBundle() {
    Practitioner resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-practitioner-1.00.json"),
                Practitioner.class);

    Bundle bundle = bundleOf(resource);
    assertThat(controller.validate(bundle)).isEqualTo(Dstu2Validator.ok());
  }

  @Test(expected = ConstraintViolationException.class)
  @SneakyThrows
  public void validateThrowsExceptionForInvalidBundle() {
    Practitioner resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-practitioner-1.00.json"),
                Practitioner.class);
    resource.resourceType(null);

    Bundle bundle = bundleOf(resource);
    controller.validate(bundle);
  }
}
