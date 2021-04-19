package gov.va.api.health.dataquery.service.controller.location;

import static gov.va.api.health.dataquery.service.controller.MockRequests.requestFromUri;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.lighthouse.vulcan.InvalidRequest;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public class R4LocationControllerTest {
  @Mock IdentityService ids;

  @Mock LocationRepository repository;

  private static ResourceIdentity id(String cdwId) {
    return ResourceIdentity.builder().system("CDW").resource("LOCATION").identifier(cdwId).build();
  }

  @SneakyThrows
  static String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  private static Registration registration(String cdwId, String publicId) {
    return Registration.builder().uuid(publicId).resourceIdentities(List.of(id(cdwId))).build();
  }

  private R4LocationController controller() {
    return new R4LocationController(
        LinkProperties.builder()
            .publicUrl("http://fonzy.com")
            .publicR4BasePath("r4")
            .publicStu3BasePath("stu3")
            .publicDstu2BasePath("dstu2")
            .maxPageSize(20)
            .defaultPageSize(15)
            .build(),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = {"?_id=321&identifier=123"})
  void invalidRequest(String query) {
    var r = requestFromUri("http://fonzy.com/r4/Location" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> controller().search(r));
  }

  private void mockLocationIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity = id(cdwId);
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
    when(ids.register(any())).thenReturn(List.of(registration("loc1", "x")));
    when(ids.lookup("x")).thenReturn(List.of(id("loc1")));
    var entity = LocationSamples.Datamart.create().entity("x");
    when(repository.findById("loc1")).thenReturn(Optional.of(entity));
    assertThat(json(controller().read("x")))
        .isEqualTo(json(LocationSamples.R4.create().location("x")));
  }

  @Test
  void readRaw() {
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(ids.lookup("x")).thenReturn(List.of(id("loc1")));
    var entity = LocationEntity.builder().cdwId("loc1").payload("payload!").build();
    when(repository.findById("loc1")).thenReturn(Optional.of(entity));
    var actual = controller().readRaw("loc1", response);
    assertThat(actual).isEqualTo("payload!");
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

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=loc1",
        "?identifier=loc1",
        "?name=TEM+MH+PSO+TRS+IND93EH",
        "?address=1901+VETERANS+MEMORIAL+DRIVE",
        "?address-city=TEMPLE",
        "?address-state=TEXAS",
        "?address-postalcode=76504"
      })
  @SneakyThrows
  void validRequests(String query) {
    when(ids.register(any())).thenReturn(List.of(LocationSamples.registration("loc1", "ploc1")));
    var dm = LocationSamples.Datamart.create();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i -> new PageImpl(List.of(dm.entity("loc1")), i.getArgument(1, Pageable.class), 1));
    var request = requestFromUri("http://fonzy.com/r4/Location" + query);
    var actual = controller().search(request);
    assertThat(actual.entry()).hasSize(1);
  }
}
