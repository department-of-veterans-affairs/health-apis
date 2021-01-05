package gov.va.api.health.dataquery.service.controller.observation;

import static gov.va.api.health.dataquery.service.controller.MockRequests.paging;
import static gov.va.api.health.dataquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.dataquery.service.controller.practitioner.PractitionerSamples.id;
import static gov.va.api.health.dataquery.service.controller.practitioner.PractitionerSamples.registration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.Observation;
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
public class R4ObservationControllerTest {

  @Mock IdentityService ids = mock(IdentityService.class);

  HttpServletResponse response = mock(HttpServletResponse.class);

  @Mock private ObservationRepository repository;

  R4ObservationController controller() {
    return new R4ObservationController(
        repository,
        WitnessProtection.builder().identityService(ids).build(),
        LinkProperties.builder()
            .publicUrl("http://fonzy.com")
            .publicR4BasePath("r4")
            .maxPageSize(20)
            .defaultPageSize(15)
            .build());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=321&identifier=123",
        "?category=laboratory",
        "?date=2020-1-20T16:35:00Z",
        "?category=laboratory&date=2020-1-20T16:35:00Z",
        "?code=1989-3"
      })
  @SneakyThrows
  void invalidRequest(String query) {
    var r = requestFromUri("http://fonzy.com/r4/Observation" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> controller().search(r));
  }

  @Test
  void read() {
    when(ids.register(any())).thenReturn(List.of(registration("ob1", "pob1")));
    when(ids.lookup("pob1")).thenReturn(List.of(id("ob1")));
    ObservationEntity entity = ObservationSamples.Datamart.create().entity("ob1");
    when(repository.findById("ob1")).thenReturn(Optional.of(entity));
    assertThat(controller().read("pob1"))
        .isEqualTo(ObservationSamples.R4.create().observation("ob1"));
  }

  @Test
  void readRaw() {
    when(ids.lookup("pob1")).thenReturn(List.of(id("ob1")));
    ObservationEntity entity = ObservationEntity.builder().icn("p1").payload("payload!").build();
    when(repository.findById("ob1")).thenReturn(Optional.of(entity));
    var actual = controller().readRaw("pob1", response);
    assertThat(actual).isEqualTo("payload!");
  }

  @Test
  void readRawThrowsNotFoundWhenDataIsMissing() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().readRaw("ob1", response));
  }

  @Test
  void readRawThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().readRaw("ob1", response));
  }

  @Test
  void readThrowsNotFoundWhenDataIsMissing() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("ob1"));
  }

  @Test
  void readThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().readRaw("ob1", response));
  }

  @Test
  void toBundle() {
    when(ids.register(any()))
        .thenReturn(
            List.of(
                registration("ob1", "pob1"),
                registration("ob2", "pob2"),
                registration("ob3", "pob3")));
    var bundler = controller().toBundle();
    ObservationSamples.Datamart datamart = ObservationSamples.Datamart.create();
    var vr =
        VulcanResult.<ObservationEntity>builder()
            .paging(
                paging(
                    "http://fonzy.com/r4/Observation?identifier=o1&page=%d&_count=%d",
                    1, 4, 5, 6, 9, 15))
            .entities(
                Stream.of(datamart.entity("ob1"), datamart.entity("ob2"), datamart.entity("ob3")))
            .build();
    ObservationSamples.R4 r4 = ObservationSamples.R4.create();
    Observation.Bundle expected =
        ObservationSamples.R4.asBundle(
            "http://fonzy.com/r4",
            List.of(r4.observation("pob1"), r4.observation("pob2"), r4.observation("pob3")),
            999,
            ObservationSamples.R4.link(
                BundleLink.LinkRelation.first,
                "http://fonzy.com/r4/Observation?identifier=o1",
                1,
                15),
            ObservationSamples.R4.link(
                BundleLink.LinkRelation.prev,
                "http://fonzy.com/r4/Observation?identifier=o1",
                4,
                15),
            ObservationSamples.R4.link(
                BundleLink.LinkRelation.self,
                "http://fonzy.com/r4/Observation?identifier=o1",
                5,
                15),
            ObservationSamples.R4.link(
                BundleLink.LinkRelation.next,
                "http://fonzy.com/r4/Observation?identifier=o1",
                6,
                15),
            ObservationSamples.R4.link(
                BundleLink.LinkRelation.last,
                "http://fonzy.com/r4/Observation?identifier=o1",
                9,
                15));
    var applied = bundler.apply(vr);
    assertThat(applied).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=ob1",
        "?identifier=p1",
        "?patient=111V111",
        "?patient=111V111&category=laboratory",
        "?patient=111V111&category=laboratory&date=2020-1-20T16:35:00Z",
        "?patient=111V111&code=1989-3"
      })
  @SneakyThrows
  void validRequests(String query) {
    when(ids.register(any())).thenReturn(List.of(registration("ob1", "pob1")));
    ObservationSamples.Datamart dm = ObservationSamples.Datamart.create();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i -> new PageImpl(List.of(dm.entity("ob1")), i.getArgument(1, Pageable.class), 1));
    var r = requestFromUri("http://fonzy.com/r4/Observation" + query);
    var actual = controller().search(r);
    assertThat(actual.entry()).hasSize(1);
  }
}
