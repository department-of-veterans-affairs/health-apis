package gov.va.api.health.dataquery.service.controller.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.location.LocationEntity;
import gov.va.api.health.dataquery.service.controller.location.DatamartLocation;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dstu2.api.resources.Location;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import lombok.SneakyThrows;

@DataJpaTest
@RunWith(SpringRunner.class)
public class DatamartLocationControllerTest {
  @Autowired private LocationRepository repository;

  private IdentityService ids = mock(IdentityService.class);

  @SneakyThrows
  private static LocationEntity asEntity(DatamartLocation dm) {
    return LocationEntity.builder()
        .cdwId(dm.cdwId())
        .name(dm.name())
        .street(dm.address().line1())
        .city(dm.address().city())
        .state(dm.address().state())
        .postalCode(dm.address().postalCode())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  @SneakyThrows
  private static String asJson(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @SneakyThrows
  private DatamartLocation asObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartLocation.class);
  }

  private LocationController controller() {
    return new LocationController(
        true,
        null,
        null,
        new Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  private void mockLocationIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder().system("CDW").resource("LOCATION").identifier(cdwId).build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(any()))
        .thenReturn(
            List.of(
                Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build()));
  }

  @Test
  public void read() {
    DatamartLocation dm = DatamartLocationSamples.Datamart.create().location("x");
    repository.save(asEntity(dm));
    mockLocationIdentity("x", dm.cdwId());
    Location actual = controller().read("", "x");
    assertThat(actual).isEqualTo(DatamartLocationSamples.Fhir.create().location("x"));
  }

  @Test
  public void readRaw() {
    String publicId = "abc";
    String cdwId = "123";
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);

    DatamartLocation dm = DatamartLocationSamples.Datamart.create().location(cdwId);
    repository.save(asEntity(dm));
    mockLocationIdentity(publicId, cdwId);
    String json = controller().readRaw(publicId, servletResponse);
    assertThat(asObject(json)).isEqualTo(dm);
    verify(servletResponse).addHeader("X-VA-INCLUDES-ICN", "NONE");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockLocationIdentity("x", "x");
    controller().readRaw("x", mock(HttpServletResponse.class));
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("x", mock(HttpServletResponse.class));
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenDataIsMissing() {
    mockLocationIdentity("x", "x");
    controller().read("true", "x");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenIdIsUnknown() {
    controller().read("true", "x");
  }

  @Test
  public void searchById() {
    String publicId = "abc";
    String cdwId = "123";
    DatamartLocation dm = DatamartLocationSamples.Datamart.create().location(cdwId);
    repository.save(asEntity(dm));
    mockLocationIdentity(publicId, cdwId);
    Location.Bundle actual = controller().searchById("true", publicId, 1, 1);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                DatamartLocationSamples.Fhir.asBundle(
                    "http://fonzy.com/cool",
                    List.of(DatamartLocationSamples.Fhir.create().location(publicId)),
                    DatamartLocationSamples.Fhir.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/Location?identifier=" + publicId,
                        1,
                        1),
                    DatamartLocationSamples.Fhir.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/Location?identifier=" + publicId,
                        1,
                        1),
                    DatamartLocationSamples.Fhir.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Location?identifier=" + publicId,
                        1,
                        1))));
  }
}
