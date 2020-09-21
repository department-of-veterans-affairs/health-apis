package gov.va.api.health.dataquery.service.controller.location;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.Location;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
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
public class R4LocationControllerTest {
  private final IdentityService ids = mock(IdentityService.class);

  @Autowired LocationRepository repository;

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

  private Location.Bundle bundleOf(
      String queryString, Collection<Location> resources, int page, int count) {
    return LocationSamples.R4.asBundle(
        "http://fonzy.com/cool",
        resources,
        resources.size(),
        LocationSamples.R4.link(
            BundleLink.LinkRelation.first,
            "http://fonzy.com/cool/Location?" + queryString,
            page,
            count),
        LocationSamples.R4.link(
            BundleLink.LinkRelation.self,
            "http://fonzy.com/cool/Location?" + queryString,
            page,
            count),
        LocationSamples.R4.link(
            BundleLink.LinkRelation.last,
            "http://fonzy.com/cool/Location?" + queryString,
            page,
            count));
  }

  private R4LocationController controller() {
    return new R4LocationController(
        new R4Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private void mockLocationIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder().system("CDW").resource("LOCATION").identifier(cdwId).build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(publicId)
                    .resourceIdentities(List.of(resourceIdentity))
                    .build()));
  }

  @Test
  void read() {
    DatamartLocation dm = LocationSamples.Datamart.create().location();
    mockLocationIdentity("x", dm.cdwId());
    repository.save(asEntity(dm));
    Location actual = controller().read("x");
    assertThat(actual).isEqualTo(LocationSamples.R4.create().location("x"));
  }

  @Test
  void readRaw() {
    HttpServletResponse response = mock(HttpServletResponse.class);
    DatamartLocation dm = LocationSamples.Datamart.create().location();
    mockLocationIdentity("x", dm.cdwId());
    repository.save(asEntity(dm));
    DatamartLocation actual = asObject(controller().readRaw("x", response));
    assertThat(actual).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", "NONE");
  }

  @Test
  void readThrowsNotFoundWhenDataIsMissing() {
    mockLocationIdentity("x", "24");
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("x"));
  }

  @Test
  void readThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("x"));
  }

  @Test
  void searchByAddress() {
    String addressStreet = LocationSamples.LOCATION_ADDRESS_STREET;
    DatamartLocation dm = LocationSamples.Datamart.create().location();
    repository.save(asEntity(dm));
    Location.Bundle actual = controller().searchByAddress(addressStreet, null, null, null, 1, 15);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                bundleOf(
                    "address=" + encode(addressStreet),
                    List.of(LocationSamples.R4.create().location()),
                    1,
                    15)));
  }

  @Test
  void searchByAddressCity() {
    String city = LocationSamples.LOCATION_ADDRESS_CITY;
    DatamartLocation dm = LocationSamples.Datamart.create().location();
    repository.save(asEntity(dm));
    Location.Bundle actual = controller().searchByAddress(null, city, null, null, 1, 15);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                bundleOf(
                    "address-city=" + city,
                    List.of(LocationSamples.R4.create().location()),
                    1,
                    15)));
  }

  @Test
  void searchByAddressPostalCode() {
    String postalCode = LocationSamples.LOCATION_ADDRESS_POSTAL_CODE;
    DatamartLocation dm = LocationSamples.Datamart.create().location();
    repository.save(asEntity(dm));
    Location.Bundle actual = controller().searchByAddress(null, null, null, postalCode, 1, 15);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                bundleOf(
                    "address-postalcode=" + postalCode,
                    List.of(LocationSamples.R4.create().location()),
                    1,
                    15)));
  }

  @Test
  void searchByAddressState() {
    String state = LocationSamples.LOCATION_ADDRESS_STATE;
    DatamartLocation dm = LocationSamples.Datamart.create().location();
    repository.save(asEntity(dm));
    Location.Bundle actual = controller().searchByAddress(null, null, state, null, 1, 15);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                bundleOf(
                    "address-state=" + state,
                    List.of(LocationSamples.R4.create().location()),
                    1,
                    15)));
  }

  @Test
  void searchByAddressThrowsMissingSearchParametersWhenAllParamsAreNull() {
    assertThrows(
        ResourceExceptions.MissingSearchParameters.class,
        () -> controller().searchByAddress(null, null, null, null, 1, 15));
  }

  @Test
  void searchById() {
    DatamartLocation dm = LocationSamples.Datamart.create().location();
    mockLocationIdentity("x", dm.cdwId());
    repository.save(asEntity(dm));
    Location.Bundle actual = controller().searchById("x", 1, 1);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                bundleOf(
                    "identifier=x", List.of(LocationSamples.R4.create().location("x")), 1, 1)));
  }

  @Test
  void searchByIdentifier() {
    DatamartLocation dm = LocationSamples.Datamart.create().location();
    mockLocationIdentity("x", dm.cdwId());
    repository.save(asEntity(dm));
    Location.Bundle actual = controller().searchByIdentifier("x", 1, 1);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                bundleOf(
                    "identifier=x", List.of(LocationSamples.R4.create().location("x")), 1, 1)));
  }

  @Test
  void searchByIdentifierWithCount0() {
    DatamartLocation dm = LocationSamples.Datamart.create().location();
    mockLocationIdentity("x", dm.cdwId());
    repository.save(asEntity(dm));
    Location.Bundle actual = controller().searchByIdentifier("x", 1, 0);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                LocationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    emptyList(),
                    1,
                    LocationSamples.R4.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Location?identifier=x",
                        1,
                        0))));
  }

  @Test
  void searchByName() {
    String name = LocationSamples.LOCATION_NAME;
    DatamartLocation dm = LocationSamples.Datamart.create().location();
    repository.save(asEntity(dm));
    Location.Bundle actual = controller().searchByName(name, 1, 15);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                bundleOf(
                    "name=" + encode(name),
                    List.of(LocationSamples.R4.create().location()),
                    1,
                    15)));
  }

  @Test
  void searchByNameWithCount0() {
    String name = LocationSamples.LOCATION_NAME;
    DatamartLocation dm = LocationSamples.Datamart.create().location();
    repository.save(asEntity(dm));
    Location.Bundle actual = controller().searchByName(name, 1, 0);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                LocationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    emptyList(),
                    1,
                    LocationSamples.R4.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Location?name=" + encode(name),
                        1,
                        0))));
  }
}
