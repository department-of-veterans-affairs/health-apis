package gov.va.api.health.dataquery.service.controller.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.condition.ConditionController;
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
  private static String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
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
  public void searchById() {
    DatamartLocation dm = DatamartLocationSamples.Datamart.create().location("x");
    repository.save(asEntity(dm));
    mockLocationIdentity("x", dm.cdwId());
    Location.Bundle actual = controller().searchById("true", "x", 1, 1);
    Location location = DatamartLocationSamples.Fhir.create().location("x");
    assertThat(json(actual))
        .isEqualTo(
            json(
                DatamartLocationSamples.Fhir.asBundle(
                    "http://fonzy.com/cool",
                    List.of(location),
                    DatamartLocationSamples.Fhir.link(
                        LinkRelation.first, "http://fonzy.com/cool/Location?identifier=x", 1, 1),
                    DatamartLocationSamples.Fhir.link(
                        LinkRelation.self, "http://fonzy.com/cool/Location?identifier=x", 1, 1),
                    DatamartLocationSamples.Fhir.link(
                        LinkRelation.last, "http://fonzy.com/cool/Location?identifier=x", 1, 1))));
  }
}
