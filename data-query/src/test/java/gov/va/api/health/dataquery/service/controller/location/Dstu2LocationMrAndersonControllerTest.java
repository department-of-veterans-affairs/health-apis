package gov.va.api.health.dataquery.service.controller.location;

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
import gov.va.api.health.dstu2.api.resources.Location;
import gov.va.api.health.dstu2.api.resources.Location.Bundle;
import gov.va.dvp.cdw.xsd.model.CdwLocation100Root;
import gov.va.dvp.cdw.xsd.model.CdwLocation100Root.CdwLocations;
import gov.va.dvp.cdw.xsd.model.CdwLocation100Root.CdwLocations.CdwLocation;
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
public class Dstu2LocationMrAndersonControllerTest {
  @Mock MrAndersonClient client;

  @Mock Dstu2LocationController.Transformer tx;

  Dstu2LocationController controller;
  @Mock Dstu2Bundler bundler;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    controller = new Dstu2LocationController(false, tx, client, bundler, null, null);
  }

  private void assertSearch(
      Supplier<Location.Bundle> invocation, MultiValueMap<String, String> params) {
    CdwLocation100Root root = new CdwLocation100Root();
    root.setPageNumber(BigInteger.valueOf(1));
    root.setRecordsPerPage(BigInteger.valueOf(10));
    root.setRecordCount(BigInteger.valueOf(3));
    root.setLocations(new CdwLocations());
    CdwLocation xmlLocation1 = new CdwLocation();
    CdwLocation xmlLocation2 = new CdwLocation();
    CdwLocation xmlLocation3 = new CdwLocation();
    root.getLocations()
        .getLocation()
        .addAll(Arrays.asList(xmlLocation1, xmlLocation2, xmlLocation3));
    Location location1 = Location.builder().build();
    Location location2 = Location.builder().build();
    Location location3 = Location.builder().build();
    when(tx.apply(xmlLocation1)).thenReturn(location1);
    when(tx.apply(xmlLocation2)).thenReturn(location2);
    when(tx.apply(xmlLocation3)).thenReturn(location3);
    when(client.search(Mockito.any())).thenReturn(root);

    Location.Bundle mockBundle = new Location.Bundle();
    when(bundler.bundle(Mockito.any())).thenReturn(mockBundle);

    Location.Bundle actual = invocation.get();

    assertThat(actual).isSameAs(mockBundle);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<BundleContext<CdwLocation, Location, Location.Entry, Location.Bundle>> captor =
        ArgumentCaptor.forClass(BundleContext.class);

    verify(bundler).bundle(captor.capture());

    LinkConfig expectedLinkConfig =
        LinkConfig.builder()
            .page(1)
            .recordsPerPage(10)
            .totalRecords(3)
            .path("Location")
            .queryParams(params)
            .build();
    assertThat(captor.getValue().linkConfig()).isEqualTo(expectedLinkConfig);
    assertThat(captor.getValue().xmlItems()).isEqualTo(root.getLocations().getLocation());
    assertThat(captor.getValue().newBundle().get()).isInstanceOf(Location.Bundle.class);
    assertThat(captor.getValue().newEntry().get()).isInstanceOf(Location.Entry.class);
    assertThat(captor.getValue().transformer()).isSameAs(tx);
  }

  private Bundle bundleOf(Location resource) {
    return Bundle.builder()
        .type(BundleType.searchset)
        .resourceType("Bundle")
        .entry(
            Collections.singletonList(
                Location.Entry.builder().fullUrl("http://example.com").resource(resource).build()))
        .build();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void read() {
    CdwLocation100Root root = new CdwLocation100Root();
    root.setLocations(new CdwLocations());
    CdwLocation100Root.CdwLocations.CdwLocation xmlLocation =
        new CdwLocation100Root.CdwLocations.CdwLocation();
    root.getLocations().getLocation().add(xmlLocation);
    Location location = Location.builder().build();
    when(client.search(Mockito.any())).thenReturn(root);
    when(tx.apply(xmlLocation)).thenReturn(location);
    Location actual = controller.read("", "hello");
    assertThat(actual).isSameAs(location);
    ArgumentCaptor<Query<CdwLocation100Root>> captor = ArgumentCaptor.forClass(Query.class);
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
  @SneakyThrows
  public void validateAcceptsValidBundle() {
    Location resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-location-1.00.json"), Location.class);

    Bundle bundle = bundleOf(resource);
    assertThat(controller.validate(bundle)).isEqualTo(Dstu2Validator.ok());
  }

  @Test(expected = ConstraintViolationException.class)
  @SneakyThrows
  public void validateThrowsExceptionForInvalidBundle() {
    Location resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-location-1.00.json"), Location.class);
    resource.resourceType(null);

    Bundle bundle = bundleOf(resource);
    controller.validate(bundle);
  }
}
