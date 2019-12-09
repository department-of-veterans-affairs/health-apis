package gov.va.api.health.dataquery.service.controller.encounter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dstu2.api.resources.Encounter;
import gov.va.api.health.dstu2.api.resources.Encounter.Bundle;
import gov.va.dvp.cdw.xsd.model.CdwEncounter101Root;
import gov.va.dvp.cdw.xsd.model.CdwEncounter101Root.CdwEncounters;
import gov.va.dvp.cdw.xsd.model.CdwEncounter101Root.CdwEncounters.CdwEncounter;
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
public class EncounterControllerTest {
  @Mock MrAndersonClient client;

  @Mock EncounterController.Transformer tx;

  EncounterController controller;
  @Mock Dstu2Bundler bundler;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    controller = new EncounterController(tx, client, bundler);
  }

  private void assertSearch(
      Supplier<Encounter.Bundle> invocation, MultiValueMap<String, String> params) {
    CdwEncounter101Root root = new CdwEncounter101Root();
    root.setPageNumber(BigInteger.valueOf(1));
    root.setRecordsPerPage(BigInteger.valueOf(10));
    root.setRecordCount(BigInteger.valueOf(3));
    root.setEncounters(new CdwEncounters());
    CdwEncounter cdwItem1 = new CdwEncounter();
    CdwEncounter cdwItem2 = new CdwEncounter();
    CdwEncounter cdwItem3 = new CdwEncounter();
    root.getEncounters().getEncounter().addAll(Arrays.asList(cdwItem1, cdwItem2, cdwItem3));
    Encounter encounter1 = Encounter.builder().build();
    Encounter encounter2 = Encounter.builder().build();
    Encounter encounter3 = Encounter.builder().build();
    when(tx.apply(cdwItem1)).thenReturn(encounter1);
    when(tx.apply(cdwItem2)).thenReturn(encounter2);
    when(tx.apply(cdwItem3)).thenReturn(encounter3);
    when(client.search(Mockito.any())).thenReturn(root);

    Encounter.Bundle mockBundle = new Encounter.Bundle();
    when(bundler.bundle(Mockito.any())).thenReturn(mockBundle);

    Encounter.Bundle actual = invocation.get();

    assertThat(actual).isSameAs(mockBundle);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<BundleContext<CdwEncounter, Encounter, Encounter.Entry, Encounter.Bundle>>
        captor = ArgumentCaptor.forClass(BundleContext.class);

    verify(bundler).bundle(captor.capture());

    LinkConfig expectedLinkConfig =
        LinkConfig.builder()
            .page(1)
            .recordsPerPage(10)
            .totalRecords(3)
            .path("Encounter")
            .queryParams(params)
            .build();
    assertThat(captor.getValue().linkConfig()).isEqualTo(expectedLinkConfig);
    assertThat(captor.getValue().xmlItems()).isEqualTo(root.getEncounters().getEncounter());
    assertThat(captor.getValue().newBundle().get()).isInstanceOf(Encounter.Bundle.class);
    assertThat(captor.getValue().newEntry().get()).isInstanceOf(Encounter.Entry.class);
    assertThat(captor.getValue().transformer()).isSameAs(tx);
  }

  private Bundle bundleOf(Encounter resource) {
    return Bundle.builder()
        .type(BundleType.searchset)
        .resourceType("Bundle")
        .entry(
            Collections.singletonList(
                Encounter.Entry.builder().fullUrl("http://example.com").resource(resource).build()))
        .build();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void read() {
    CdwEncounter101Root root = new CdwEncounter101Root();
    root.setEncounters(new CdwEncounters());
    CdwEncounter101Root.CdwEncounters.CdwEncounter xmlEncounter =
        new CdwEncounter101Root.CdwEncounters.CdwEncounter();
    root.getEncounters().getEncounter().add(xmlEncounter);
    Encounter encounter = Encounter.builder().build();
    when(client.search(Mockito.any())).thenReturn(root);
    when(tx.apply(xmlEncounter)).thenReturn(encounter);
    Encounter actual = controller.read("hello");
    assertThat(actual).isSameAs(encounter);
    ArgumentCaptor<Query<CdwEncounter101Root>> captor = ArgumentCaptor.forClass(Query.class);
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
    Encounter resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-encounter-1.01.json"), Encounter.class);

    Bundle bundle = bundleOf(resource);
    assertThat(controller.validate(bundle)).isEqualTo(Validator.ok());
  }

  @Test(expected = ConstraintViolationException.class)
  @SneakyThrows
  public void validateThrowsExceptionForInvalidBundle() {
    Encounter resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-encounter-1.01.json"), Encounter.class);
    resource.resourceType(null);

    Bundle bundle = bundleOf(resource);
    controller.validate(bundle);
  }
}
