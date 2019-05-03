package gov.va.api.health.dataquery.service.controller.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.argonaut.api.resources.Condition.Bundle;
import gov.va.api.health.argonaut.api.resources.Condition.Entry;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.dvp.cdw.xsd.model.CdwCondition103Root;
import gov.va.dvp.cdw.xsd.model.CdwCondition103Root.CdwConditions;
import gov.va.dvp.cdw.xsd.model.CdwCondition103Root.CdwConditions.CdwCondition;
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

public class ConditionControllerTest {
  @Mock MrAndersonClient client;

  @Mock ConditionController.Transformer transformer;

  ConditionController controller;
  @Mock Bundler bundler;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    controller = new ConditionController(transformer, client, bundler);
  }

  private void assertSearch(Supplier<Bundle> invocation, MultiValueMap<String, String> params) {
    CdwCondition103Root root = new CdwCondition103Root();
    root.setPageNumber(BigInteger.valueOf(1));
    root.setRecordsPerPage(BigInteger.valueOf(10));
    root.setRecordCount(BigInteger.valueOf(3));
    root.setConditions(new CdwConditions());
    CdwCondition cdwItem1 = new CdwCondition();
    CdwCondition cdwItem2 = new CdwCondition();
    CdwCondition cdwItem3 = new CdwCondition();
    root.getConditions().getCondition().addAll(Arrays.asList(cdwItem1, cdwItem2, cdwItem3));
    Condition condition1 = Condition.builder().build();
    Condition condition2 = Condition.builder().build();
    Condition condition3 = Condition.builder().build();
    when(transformer.apply(cdwItem1)).thenReturn(condition1);
    when(transformer.apply(cdwItem2)).thenReturn(condition2);
    when(transformer.apply(cdwItem3)).thenReturn(condition3);
    when(client.search(Mockito.any())).thenReturn(root);

    Bundle mockBundle = new Bundle();
    when(bundler.bundle(Mockito.any())).thenReturn(mockBundle);

    Bundle actual = invocation.get();

    assertThat(actual).isSameAs(mockBundle);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<BundleContext<CdwCondition, Condition, Entry, Bundle>> captor =
        ArgumentCaptor.forClass(BundleContext.class);

    verify(bundler).bundle(captor.capture());

    LinkConfig expectedLinkConfig =
        LinkConfig.builder()
            .page(1)
            .recordsPerPage(10)
            .totalRecords(3)
            .path("Condition")
            .queryParams(params)
            .build();
    assertThat(captor.getValue().linkConfig()).isEqualTo(expectedLinkConfig);
    assertThat(captor.getValue().xmlItems()).isEqualTo(root.getConditions().getCondition());
    assertThat(captor.getValue().newBundle().get()).isInstanceOf(Bundle.class);
    assertThat(captor.getValue().newEntry().get()).isInstanceOf(Condition.Entry.class);
    assertThat(captor.getValue().transformer()).isSameAs(transformer);
  }

  private Bundle bundleOf(Condition resource) {
    return Bundle.builder()
        .type(BundleType.searchset)
        .resourceType("Bundle")
        .entry(
            Collections.singletonList(
                Condition.Entry.builder().fullUrl("http://example.com").resource(resource).build()))
        .build();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void read() {
    CdwCondition103Root root = new CdwCondition103Root();
    root.setConditions(new CdwConditions());
    CdwCondition xmlCondition = new CdwCondition();
    root.getConditions().getCondition().add(xmlCondition);
    Condition item = Condition.builder().build();
    when(client.search(Mockito.any())).thenReturn(root);
    when(transformer.apply(xmlCondition)).thenReturn(item);
    Condition actual = controller.read("hello");
    assertThat(actual).isSameAs(item);
    ArgumentCaptor<Query<CdwCondition103Root>> captor = ArgumentCaptor.forClass(Query.class);
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
  public void searchByPatientAndCategory() {
    assertSearch(
        () -> controller.searchByPatientAndCategory("me", "active", 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .add("category", "active")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  public void searchByPatientAndCode() {
    assertSearch(
        () -> controller.searchByPatientAndClinicalStatus("me", "provisional", 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .add("clinicalstatus", "provisional")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  @SneakyThrows
  public void validateAcceptsValidBundle() {
    Condition resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-condition-1.03.json"), Condition.class);

    Bundle bundle = bundleOf(resource);
    assertThat(controller.validate(bundle)).isEqualTo(Validator.ok());
  }

  @Test(expected = ConstraintViolationException.class)
  @SneakyThrows
  public void validateThrowsExceptionForInvalidBundle() {
    Condition resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-condition-1.03.json"), Condition.class);
    resource.resourceType(null);

    Bundle bundle = bundleOf(resource);
    controller.validate(bundle);
  }
}
