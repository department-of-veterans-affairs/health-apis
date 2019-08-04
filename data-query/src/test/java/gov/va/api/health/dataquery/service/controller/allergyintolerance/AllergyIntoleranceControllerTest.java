package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.dvp.cdw.xsd.model.CdwAllergyIntolerance105Root;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;
import javax.validation.ConstraintViolationException;
import lombok.SneakyThrows;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.util.MultiValueMap;

public class AllergyIntoleranceControllerTest {
  AllergyIntoleranceController.Transformer tx =
      mock(AllergyIntoleranceController.Transformer.class);

  MrAndersonClient client = mock(MrAndersonClient.class);

  Bundler bundler = mock(Bundler.class);

  AllergyIntoleranceController controller =
      new AllergyIntoleranceController(false, tx, client, bundler, null, null);

  private void assertSearch(
      Supplier<AllergyIntolerance.Bundle> invocation, MultiValueMap<String, String> params) {
    CdwAllergyIntolerance105Root root = new CdwAllergyIntolerance105Root();
    root.setPageNumber(BigInteger.valueOf(1));
    root.setRecordsPerPage(BigInteger.valueOf(10));
    root.setRecordCount(BigInteger.valueOf(3));
    root.setAllergyIntolerances(new CdwAllergyIntolerance105Root.CdwAllergyIntolerances());
    CdwAllergyIntolerance105Root.CdwAllergyIntolerances.CdwAllergyIntolerance
        xmlAllergyIntolerance1 =
            new CdwAllergyIntolerance105Root.CdwAllergyIntolerances.CdwAllergyIntolerance();
    CdwAllergyIntolerance105Root.CdwAllergyIntolerances.CdwAllergyIntolerance
        xmlAllergyIntolerance2 =
            new CdwAllergyIntolerance105Root.CdwAllergyIntolerances.CdwAllergyIntolerance();
    CdwAllergyIntolerance105Root.CdwAllergyIntolerances.CdwAllergyIntolerance
        xmlAllergyIntolerance3 =
            new CdwAllergyIntolerance105Root.CdwAllergyIntolerances.CdwAllergyIntolerance();
    root.getAllergyIntolerances()
        .getAllergyIntolerance()
        .addAll(
            Arrays.asList(xmlAllergyIntolerance1, xmlAllergyIntolerance2, xmlAllergyIntolerance3));
    AllergyIntolerance allergyIntolerance1 = AllergyIntolerance.builder().build();
    AllergyIntolerance allergyIntolerance2 = AllergyIntolerance.builder().build();
    AllergyIntolerance allergyIntolerance3 = AllergyIntolerance.builder().build();
    when(tx.apply(xmlAllergyIntolerance1)).thenReturn(allergyIntolerance1);
    when(tx.apply(xmlAllergyIntolerance2)).thenReturn(allergyIntolerance2);
    when(tx.apply(xmlAllergyIntolerance3)).thenReturn(allergyIntolerance3);
    when(client.search(any())).thenReturn(root);

    AllergyIntolerance.Bundle mockBundle = new AllergyIntolerance.Bundle();
    when(bundler.bundle(any())).thenReturn(mockBundle);

    AllergyIntolerance.Bundle actual = invocation.get();

    assertThat(actual).isSameAs(mockBundle);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<
            Bundler.BundleContext<
                CdwAllergyIntolerance105Root.CdwAllergyIntolerances.CdwAllergyIntolerance,
                AllergyIntolerance, AllergyIntolerance.Entry, AllergyIntolerance.Bundle>>
        captor = ArgumentCaptor.forClass(Bundler.BundleContext.class);

    verify(bundler).bundle(captor.capture());

    PageLinks.LinkConfig expectedLinkConfig =
        PageLinks.LinkConfig.builder()
            .page(1)
            .recordsPerPage(10)
            .totalRecords(3)
            .path("AllergyIntolerance")
            .queryParams(params)
            .build();
    assertThat(captor.getValue().linkConfig()).isEqualTo(expectedLinkConfig);
    assertThat(captor.getValue().xmlItems())
        .isEqualTo(root.getAllergyIntolerances().getAllergyIntolerance());
    assertThat(captor.getValue().newBundle().get()).isInstanceOf(AllergyIntolerance.Bundle.class);
    assertThat(captor.getValue().newEntry().get()).isInstanceOf(AllergyIntolerance.Entry.class);
    assertThat(captor.getValue().transformer()).isSameAs(tx);
  }

  private AllergyIntolerance.Bundle bundleOf(AllergyIntolerance resource) {
    return AllergyIntolerance.Bundle.builder()
        .type(BundleType.searchset)
        .resourceType("Bundle")
        .entry(
            Collections.singletonList(
                AllergyIntolerance.Entry.builder()
                    .fullUrl("hhtp://example.com")
                    .resource(resource)
                    .build()))
        .build();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void read() {
    CdwAllergyIntolerance105Root root = new CdwAllergyIntolerance105Root();
    root.setAllergyIntolerances(new CdwAllergyIntolerance105Root.CdwAllergyIntolerances());
    CdwAllergyIntolerance105Root.CdwAllergyIntolerances.CdwAllergyIntolerance
        xmlAllergyIntolerance =
            new CdwAllergyIntolerance105Root.CdwAllergyIntolerances.CdwAllergyIntolerance();
    root.getAllergyIntolerances().getAllergyIntolerance().add(xmlAllergyIntolerance);
    AllergyIntolerance allergyIntolerance = AllergyIntolerance.builder().build();
    when(client.search(any())).thenReturn(root);
    when(tx.apply(xmlAllergyIntolerance)).thenReturn(allergyIntolerance);
    AllergyIntolerance actual = controller.read("", "hello");
    assertThat(actual).isSameAs(allergyIntolerance);
    ArgumentCaptor<Query<CdwAllergyIntolerance105Root>> captor =
        ArgumentCaptor.forClass(Query.class);
    verify(client).search(captor.capture());
    assertThat(captor.getValue().parameters()).isEqualTo(Parameters.forIdentity("hello"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchById() {
    bundler = new Bundler(new ConfigurableBaseUrlPageLinks("", ""));
    controller = new AllergyIntoleranceController(false, tx, client, bundler, null, null);

    CdwAllergyIntolerance105Root root = new CdwAllergyIntolerance105Root();
    root.setAllergyIntolerances(new CdwAllergyIntolerance105Root.CdwAllergyIntolerances());
    CdwAllergyIntolerance105Root.CdwAllergyIntolerances.CdwAllergyIntolerance
        xmlAllergyIntolerance =
            new CdwAllergyIntolerance105Root.CdwAllergyIntolerances.CdwAllergyIntolerance();
    root.getAllergyIntolerances().getAllergyIntolerance().add(xmlAllergyIntolerance);
    when(client.search(any())).thenReturn(root);

    AllergyIntolerance allergyIntolerance = AllergyIntolerance.builder().build();
    when(tx.apply(xmlAllergyIntolerance)).thenReturn(allergyIntolerance);

    AllergyIntolerance.Bundle actual = controller.searchById("", "me", 1, 10);

    assertThat(Iterables.getOnlyElement(actual.entry()).resource()).isSameAs(allergyIntolerance);

    ArgumentCaptor<Query<CdwAllergyIntolerance105Root>> captor =
        ArgumentCaptor.forClass(Query.class);
    verify(client).search(captor.capture());
    assertThat(captor.getValue().parameters())
        .isEqualTo(Parameters.builder().add("identifier", "me").build());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchByIdentifier() {
    bundler = new Bundler(new ConfigurableBaseUrlPageLinks("", ""));
    controller = new AllergyIntoleranceController(false, tx, client, bundler, null, null);

    CdwAllergyIntolerance105Root root = new CdwAllergyIntolerance105Root();
    root.setAllergyIntolerances(new CdwAllergyIntolerance105Root.CdwAllergyIntolerances());
    CdwAllergyIntolerance105Root.CdwAllergyIntolerances.CdwAllergyIntolerance
        xmlAllergyIntolerance =
            new CdwAllergyIntolerance105Root.CdwAllergyIntolerances.CdwAllergyIntolerance();
    root.getAllergyIntolerances().getAllergyIntolerance().add(xmlAllergyIntolerance);
    when(client.search(any())).thenReturn(root);

    AllergyIntolerance allergyIntolerance = AllergyIntolerance.builder().build();
    when(tx.apply(xmlAllergyIntolerance)).thenReturn(allergyIntolerance);

    AllergyIntolerance.Bundle actual = controller.searchByIdentifier("", "me", 1, 10);

    assertThat(Iterables.getOnlyElement(actual.entry()).resource()).isSameAs(allergyIntolerance);

    ArgumentCaptor<Query<CdwAllergyIntolerance105Root>> captor =
        ArgumentCaptor.forClass(Query.class);
    verify(client).search(captor.capture());
    assertThat(captor.getValue().parameters())
        .isEqualTo(Parameters.builder().add("identifier", "me").build());
  }

  @Test
  public void searchByPatient() {
    assertSearch(
        () -> controller.searchByPatient("", "me", 1, 10),
        Parameters.builder().add("patient", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void searchReturnsEmptyResults() {
    CdwAllergyIntolerance105Root root = new CdwAllergyIntolerance105Root();
    root.setPageNumber(BigInteger.valueOf(1));
    root.setRecordsPerPage(BigInteger.valueOf(10));
    root.setRecordCount(BigInteger.valueOf(0));
    when(client.search(any())).thenReturn(root);

    AllergyIntolerance.Bundle mockBundle = new AllergyIntolerance.Bundle();
    when(bundler.bundle(any())).thenReturn(mockBundle);

    AllergyIntolerance.Bundle actual = controller.searchByPatient("", "me", 1, 10);

    assertThat(actual).isSameAs(mockBundle);
    ArgumentCaptor<
            Bundler.BundleContext<
                CdwAllergyIntolerance105Root.CdwAllergyIntolerances.CdwAllergyIntolerance,
                AllergyIntolerance, AllergyIntolerance.Entry, AllergyIntolerance.Bundle>>
        captor = ArgumentCaptor.forClass(Bundler.BundleContext.class);

    verify(bundler).bundle(captor.capture());
    PageLinks.LinkConfig expectedLinkConfig =
        PageLinks.LinkConfig.builder()
            .page(1)
            .recordsPerPage(10)
            .totalRecords(0)
            .path("AllergyIntolerance")
            .queryParams(
                Parameters.builder().add("patient", "me").add("page", 1).add("_count", 10).build())
            .build();
    assertThat(captor.getValue().linkConfig()).isEqualTo(expectedLinkConfig);
    assertThat(captor.getValue().xmlItems()).isEmpty();
  }

  @Test
  @SneakyThrows
  public void validateAcceptsValidBundle() {
    AllergyIntolerance resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-allergyintolerance-1.05.json"),
                AllergyIntolerance.class);
    AllergyIntolerance.Bundle bundle = bundleOf(resource);
    assertThat(controller.validate(bundle)).isEqualTo(Validator.ok());
  }

  @SneakyThrows
  @Test(expected = ConstraintViolationException.class)
  public void validateThrowsExceptionForInvalidBundle() {
    AllergyIntolerance resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-allergyintolerance-1.05.json"),
                AllergyIntolerance.class);
    resource.resourceType(null);
    AllergyIntolerance.Bundle bundle = bundleOf(resource);
    controller.validate(bundle);
  }
}
