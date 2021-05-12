package gov.va.api.health.dataquery.service.controller.immunization;

import static gov.va.api.health.dataquery.service.controller.MockRequests.paging;
import static gov.va.api.health.dataquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.dataquery.service.controller.immunization.ImmunizationSamples.id;
import static gov.va.api.health.dataquery.service.controller.immunization.ImmunizationSamples.registration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.lighthouse.vulcan.InvalidRequest;
import gov.va.api.lighthouse.vulcan.VulcanResult;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class R4ImmunizationControllerTest {

  @Mock IdentityService ids;

  @Mock ImmunizationRepository repository;

  @SneakyThrows
  private static DatamartImmunization toDatamart(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartImmunization.class);
  }

  private R4ImmunizationController _controller() {
    return new R4ImmunizationController(
        LinkProperties.builder()
            .publicUrl("http://fonzy.com")
            .publicR4BasePath("r4")
            .maxPageSize(20)
            .defaultPageSize(15)
            .build(),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?patient=p1&_id=123",
        "?patient=p1&identifier=123",
        "?_id=123&identifier=456",
        "?patient:badModifier=p1",
        "?patient=badReference/p1",
        "?patient=http://fonzy.com/r4/badReference/p1"
      })
  @SneakyThrows
  void invalidRequests(String query) {
    var request = requestFromUri("http://fonzy.com/r4/Immunization" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> _controller().search(request));
  }

  @Test
  void read() {
    when(ids.register(any())).thenReturn(List.of(registration("im1", "pim1")));
    when(ids.lookup("pim1")).thenReturn(List.of(id("im1")));
    var entity = ImmunizationSamples.Datamart.create().entity("im1", "p1");
    when(repository.findById("im1")).thenReturn(Optional.of(entity));
    assertThat(_controller().read("pim1"))
        .isEqualTo(ImmunizationSamples.R4.create().immunization("pim1", "p1"));
  }

  @Test
  public void readRaw() {
    when(ids.lookup("pim1")).thenReturn(List.of(ImmunizationSamples.id("im1")));
    ImmunizationEntity entity = ImmunizationEntity.builder().icn("p1").payload("payload!").build();
    when(repository.findById("im1")).thenReturn(Optional.of(entity));
    var actual = _controller().readRaw("pim1", mock(HttpServletResponse.class));
    assertThat(actual).isEqualTo("payload!");
  }

  @Test
  void toBundle() {
    when(ids.register(any()))
        .thenReturn(
            List.of(
                registration("im1", "pim1"),
                registration("ai2", "pai2"),
                registration("ai3", "pai3")));
    var bundler = _controller().toBundle();
    var dm = ImmunizationSamples.Datamart.create();
    var vr =
        VulcanResult.<ImmunizationEntity>builder()
            .paging(
                paging(
                    "http://fonzy.com/r4/Immunization?patient=p1&page=%d&_count=%d",
                    1, 4, 5, 6, 9, 15))
            .entities(
                Stream.of(dm.entity("im1", "p1"), dm.entity("ai2", "p1"), dm.entity("ai3", "p1")))
            .build();
    var r4 = ImmunizationSamples.R4.create();
    var expected =
        ImmunizationSamples.R4.asBundle(
            "http://fonzy.com/r4",
            List.of(
                r4.immunization("pim1", "p1"),
                r4.immunization("pai2", "p1"),
                r4.immunization("pai3", "p1")),
            999,
            ImmunizationSamples.R4.link(
                BundleLink.LinkRelation.first,
                "http://fonzy.com/r4/Immunization?patient=p1",
                1,
                15),
            ImmunizationSamples.R4.link(
                BundleLink.LinkRelation.prev, "http://fonzy.com/r4/Immunization?patient=p1", 4, 15),
            ImmunizationSamples.R4.link(
                BundleLink.LinkRelation.self, "http://fonzy.com/r4/Immunization?patient=p1", 5, 15),
            ImmunizationSamples.R4.link(
                BundleLink.LinkRelation.next, "http://fonzy.com/r4/Immunization?patient=p1", 6, 15),
            ImmunizationSamples.R4.link(
                BundleLink.LinkRelation.last,
                "http://fonzy.com/r4/Immunization?patient=p1",
                9,
                15));
    assertThat(bundler.apply(vr)).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=pd1",
        "?identifier=pd1",
        "?patient=p1",
        "?patient=Patient/p1",
        "?patient=http://fonzy.com/r4/Patient/p1",
        "?patient:Patient=p1"
      })
  void validRequests(String query) {
    when(ids.register(any()))
        .thenReturn(
            List.of(
                ImmunizationSamples.registration("im1", "pim1"),
                ImmunizationSamples.registration("ai2", "pai2"),
                ImmunizationSamples.registration("ai3", "pai3")));
    var dm = ImmunizationSamples.Datamart.create();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl(
                    List.of(dm.entity("im1", "p1"), dm.entity("ai2", "p1"), dm.entity("ai3", "p1")),
                    i.getArgument(1, Pageable.class),
                    3));
    var request = requestFromUri("http://fonzy.com/r4/Immunization" + query);
    var actual = _controller().search(request);
    assertThat(actual.entry()).hasSize(3);
  }
}
