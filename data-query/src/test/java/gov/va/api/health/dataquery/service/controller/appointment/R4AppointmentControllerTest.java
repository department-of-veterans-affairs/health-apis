package gov.va.api.health.dataquery.service.controller.appointment;

import static gov.va.api.health.dataquery.service.controller.MockRequests.paging;
import static gov.va.api.health.dataquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.dataquery.service.controller.appointment.AppointmentSamples.id;
import static gov.va.api.health.dataquery.service.controller.appointment.AppointmentSamples.registration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.vulcan.VulcanResult;
import java.math.BigInteger;
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
public class R4AppointmentControllerTest {
  @Mock IdentityService ids;

  @Mock AppointmentRepository repository;

  R4AppointmentController controller() {
    return new R4AppointmentController(
        WitnessProtection.builder().identityService(ids).build(),
        repository,
        LinkProperties.builder()
            .publicUrl("http://fonzy.com")
            .publicR4BasePath("r4")
            .maxPageSize(20)
            .defaultPageSize(15)
            .build());
  }

  @Test
  void emptyBundle() {
    var url = "http://fonzy.com/r4/Appointment?nope=unknown";
    var request = requestFromUri(url);
    Appointment.Bundle bundle = controller().search(request);
    assertThat(bundle.total()).isEqualTo(0);
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  void read() {
    when(ids.register(any())).thenReturn(List.of(registration("1:A", "pa1")));
    when(ids.lookup("pa1")).thenReturn(List.of(id("1:A")));
    AppointmentEntity entity = AppointmentSamples.Datamart.create().entity("1", "A", "p1");
    when(repository.findById(CompositeCdwId.fromCdwId("1:A"))).thenReturn(Optional.of(entity));
    assertThat(controller().read("pa1"))
        .isEqualTo(AppointmentSamples.R4.create().appointment("pa1", "p1"));
  }

  @Test
  void readRaw() {
    when(ids.lookup("pa1")).thenReturn(List.of(id("1:A")));
    AppointmentEntity entity =
        AppointmentEntity.builder()
            .cdwIdNumber(new BigInteger("1"))
            .cdwIdResourceCode('A')
            .icn("p1")
            .payload("payload")
            .build();
    when(repository.findById(CompositeCdwId.fromCdwId("1:A"))).thenReturn(Optional.of(entity));
    assertThat(controller().readRaw("pa1", mock(HttpServletResponse.class))).isEqualTo("payload");
  }

  @Test
  void toBundle() {
    when(ids.register(any()))
        .thenReturn(
            List.of(
                registration("1:A", "pa1"),
                registration("2:A", "pa2"),
                registration("3:A", "pa3")));
    var bundler = controller().toBundle();
    AppointmentSamples.Datamart datamart = AppointmentSamples.Datamart.create();
    var vr =
        VulcanResult.<AppointmentEntity>builder()
            .paging(
                paging(
                    "http://fonzy.com/r4/Appointment?patient=p1&page=%d&_count=%d",
                    1, 4, 5, 6, 9, 15))
            .entities(
                Stream.of(
                    datamart.entity("1", "A", "p1"),
                    datamart.entity("2", "A", "p1"),
                    datamart.entity("3", "A", "p1")))
            .build();
    AppointmentSamples.R4 r4 = AppointmentSamples.R4.create();
    Appointment.Bundle expected =
        AppointmentSamples.R4.asBundle(
            "http://fonzy.com/r4",
            List.of(
                r4.appointment("pa1", "p1"),
                r4.appointment("pa2", "p1"),
                r4.appointment("pa3", "p1")),
            999,
            AppointmentSamples.R4.link(
                BundleLink.LinkRelation.first, "http://fonzy.com/r4/Appointment?patient=p1", 1, 15),
            AppointmentSamples.R4.link(
                BundleLink.LinkRelation.prev, "http://fonzy.com/r4/Appointment?patient=p1", 4, 15),
            AppointmentSamples.R4.link(
                BundleLink.LinkRelation.self, "http://fonzy.com/r4/Appointment?patient=p1", 5, 15),
            AppointmentSamples.R4.link(
                BundleLink.LinkRelation.next, "http://fonzy.com/r4/Appointment?patient=p1", 6, 15),
            AppointmentSamples.R4.link(
                BundleLink.LinkRelation.last, "http://fonzy.com/r4/Appointment?patient=p1", 9, 15));
    var applied = bundler.apply(vr);
    assertThat(applied).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=publica1",
        "?patient=111V111",
        "?patient=p1&location=orlando",
        "?patient=p1&_lastUpdated=2020-1-20T16:35:00Z",
        "?location=miami&_lastUpdated=2020-1-20T16:35:00Z",
        "?patient=p1&location=westpalm&_lastUpdated=2020-1-20T16:35:00Z",
      })
  @SneakyThrows
  void validRequests(String query) {
    when(ids.register(any())).thenReturn(List.of(registration("1:A", "pa1")));
    AppointmentSamples.Datamart dm = AppointmentSamples.Datamart.create();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl(
                    List.of(dm.entity("1", "A", "p1")), i.getArgument(1, Pageable.class), 1));
    var r = requestFromUri("http://fonzy.com/r4/Appointment" + query);
    var actual = controller().search(r);
    assertThat(actual.entry()).hasSize(1);
  }
}
