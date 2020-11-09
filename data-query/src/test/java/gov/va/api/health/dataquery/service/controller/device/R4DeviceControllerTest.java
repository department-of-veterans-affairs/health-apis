package gov.va.api.health.dataquery.service.controller.device;

import static gov.va.api.health.dataquery.service.controller.MockRequests.paging;
import static gov.va.api.health.dataquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.dataquery.service.controller.device.DeviceSamples.id;
import static gov.va.api.health.dataquery.service.controller.device.DeviceSamples.registration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
public class R4DeviceControllerTest {

  @Mock IdentityService ids;

  @Mock DeviceRepository repository;

  R4DeviceController controller() {
    return new R4DeviceController(
        WitnessProtection.builder().identityService(ids).build(),
        repository,
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
        "?nachos=friday",
        "?type=156009",
        "?patient=p1&_id=123",
        "?patient=p1&identifier=123",
        "?_id=123&identifier=456"
      })
  @SneakyThrows
  void invalidRequests(String query) {
    var request = requestFromUri("http://fonzy.com/r4/Device" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> controller().search(request));
  }

  @Test
  void read() {
    when(ids.register(any())).thenReturn(List.of(registration("d1", "pd1")));
    when(ids.lookup("pd1")).thenReturn(List.of(id("d1")));
    DeviceEntity entity = DeviceSamples.Datamart.create().entity("d1", "p1");
    when(repository.findById("d1")).thenReturn(Optional.of(entity));
    assertThat(controller().read("pd1")).isEqualTo(DeviceSamples.R4.create().device("pd1", "p1"));
  }

  @Test
  void readRaw() {
    when(ids.lookup("pd1")).thenReturn(List.of(id("d1")));
    DeviceEntity entity = DeviceEntity.builder().cdwId("d1").icn("p1").payload("payload").build();
    when(repository.findById("d1")).thenReturn(Optional.of(entity));
    assertThat(controller().readRaw("pd1", mock(HttpServletResponse.class))).isEqualTo("payload");
  }

  @Test
  void toBundle() {
    when(ids.register(any()))
        .thenReturn(
            List.of(
                registration("d1", "pd1"), registration("d2", "pd2"), registration("d3", "pd3")));
    var bundler = controller().toBundle();
    DeviceSamples.Datamart dm = DeviceSamples.Datamart.create();
    var vr =
        VulcanResult.<DeviceEntity>builder()
            .paging(
                paging(
                    "http://fonzy.com/r4/Device?patient=p1&page=%d&_count=%d", 1, 4, 5, 6, 9, 15))
            .entities(
                Stream.of(dm.entity("d1", "p1"), dm.entity("d2", "p1"), dm.entity("d3", "p1")))
            .build();
    DeviceSamples.R4 r4 = DeviceSamples.R4.create();
    var expected =
        DeviceSamples.R4.asBundle(
            "http://fonzy.com/r4",
            List.of(r4.device("pd1", "p1"), r4.device("pd2", "p1"), r4.device("pd3", "p1")),
            999,
            DeviceSamples.R4.link(
                BundleLink.LinkRelation.first, "http://fonzy.com/r4/Device?patient=p1", 1, 15),
            DeviceSamples.R4.link(
                BundleLink.LinkRelation.prev, "http://fonzy.com/r4/Device?patient=p1", 4, 15),
            DeviceSamples.R4.link(
                BundleLink.LinkRelation.self, "http://fonzy.com/r4/Device?patient=p1", 5, 15),
            DeviceSamples.R4.link(
                BundleLink.LinkRelation.next, "http://fonzy.com/r4/Device?patient=p1", 6, 15),
            DeviceSamples.R4.link(
                BundleLink.LinkRelation.last, "http://fonzy.com/r4/Device?patient=p1", 9, 15));
    assertThat(bundler.apply(vr)).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(strings = {"?_id=pd1", "?identifier=pd1", "?patient=p1"})
  @SneakyThrows
  void validRequests(String query) {
    when(ids.register(any()))
        .thenReturn(
            List.of(
                registration("d1", "pd1"), registration("d2", "pd2"), registration("d3", "pd3")));
    DeviceSamples.Datamart dm = DeviceSamples.Datamart.create();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl(
                    List.of(dm.entity("d1", "p1"), dm.entity("d2", "p1"), dm.entity("d3", "p1")),
                    i.getArgument(1, Pageable.class),
                    3));
    var request = requestFromUri("http://fonzy.com/r4/Device" + query);
    var actual = controller().search(request);
    assertThat(actual.entry()).hasSize(3);
  }
}
