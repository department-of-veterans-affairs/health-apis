package gov.va.api.health.dataquery.service.controller.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.resources.Location;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public class Dstu2LocationControllerTest {
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

  private void addMockIdentities(
      String locPubId, String locCdwId, String orgPubId, String orgCdwId) {
    ResourceIdentity locResource =
        ResourceIdentity.builder().system("CDW").resource("LOCATION").identifier(locCdwId).build();
    ResourceIdentity orgResource =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("ORGANIZATION")
            .identifier(orgCdwId)
            .build();
    when(ids.lookup(locPubId)).thenReturn(List.of(locResource));
    when(ids.register(any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(locPubId)
                    .resourceIdentities(List.of(locResource))
                    .build(),
                Registration.builder()
                    .uuid(orgPubId)
                    .resourceIdentities(List.of(orgResource))
                    .build()));
  }

  @SneakyThrows
  private DatamartLocation asObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartLocation.class);
  }

  private Dstu2LocationController controller() {
    return new Dstu2LocationController(
        new Dstu2Bundler(
            new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @Test
  public void read() {
    String publicId = "abc";
    String cdwId = "123";
    String orgPubId = "def";
    String orgCdwId = "456";
    addMockIdentities(publicId, cdwId, orgPubId, orgCdwId);
    DatamartLocation dm = LocationSamples.Datamart.create().location(cdwId, orgCdwId);
    repository.save(asEntity(dm));
    Location actual = controller().read(publicId);
    assertThat(actual).isEqualTo(LocationSamples.Dstu2.create().location(publicId, orgPubId));
  }

  @Test
  public void readRaw() {
    String publicId = "abc";
    String cdwId = "123";
    String orgPubId = "def";
    String orgCdwId = "456";
    addMockIdentities(publicId, cdwId, orgPubId, orgCdwId);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    DatamartLocation dm = LocationSamples.Datamart.create().location(cdwId, orgCdwId);
    repository.save(asEntity(dm));
    String json = controller().readRaw(publicId, servletResponse);
    assertThat(asObject(json)).isEqualTo(dm);
    verify(servletResponse).addHeader("X-VA-INCLUDES-ICN", "NONE");
  }

  @Test
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    addMockIdentities("x", "x", "y", "y");
    assertThrows(
        ResourceExceptions.NotFound.class,
        () -> controller().readRaw("x", mock(HttpServletResponse.class)));
  }

  @Test
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(
        ResourceExceptions.NotFound.class,
        () -> controller().readRaw("x", mock(HttpServletResponse.class)));
  }

  @Test
  public void readThrowsNotFoundWhenDataIsMissing() {
    addMockIdentities("x", "x", "y", "y");
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("x"));
  }

  @Test
  public void readThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("x"));
  }

  @Test
  public void searchById() {
    String publicId = "abc";
    String cdwId = "123";
    String orgPubId = "def";
    String orgCdwId = "456";
    addMockIdentities(publicId, cdwId, orgPubId, orgCdwId);
    DatamartLocation dm = LocationSamples.Datamart.create().location(cdwId, orgCdwId);
    repository.save(asEntity(dm));
    Location.Bundle actual = controller().searchById(publicId, 1, 1);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                LocationSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(LocationSamples.Dstu2.create().location(publicId, orgPubId)),
                    LocationSamples.Dstu2.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Location?identifier=" + publicId,
                        1,
                        1),
                    LocationSamples.Dstu2.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Location?identifier=" + publicId,
                        1,
                        1),
                    LocationSamples.Dstu2.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Location?identifier=" + publicId,
                        1,
                        1))));
  }
}
