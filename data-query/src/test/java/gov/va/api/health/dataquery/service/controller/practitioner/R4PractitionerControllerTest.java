package gov.va.api.health.dataquery.service.controller.practitioner;

import static gov.va.api.health.dataquery.service.controller.MockRequests.paging;
import static gov.va.api.health.dataquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.dataquery.service.controller.practitioner.PractitionerSamples.id;
import static gov.va.api.health.dataquery.service.controller.practitioner.PractitionerSamples.registration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.Practitioner;
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
public class R4PractitionerControllerTest {
  @Mock IdentityService ids;

  @Mock PractitionerRepository repository;

  R4PractitionerController controller() {
    return new R4PractitionerController(
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

  @ParameterizedTest
  @ValueSource(strings = {"", "?identifier=123", "?invalid=request"})
  @SneakyThrows
  void invalidRequest(String query) {
    var r = requestFromUri("http://fonzy.com/r4/Practitioner" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> controller().search(r));
  }

  @Test
  void read() {
    when(ids.register(any())).thenReturn(List.of(registration("pr1", "ppr1")));
    when(ids.lookup("ppr1")).thenReturn(List.of(id("pr1")));
    PractitionerEntity entity = PractitionerSamples.Datamart.create().entity("pr1", "loc1", "org1");
    when(repository.findById("pr1")).thenReturn(Optional.of(entity));
    assertThat(controller().read("ppr1"))
        .isEqualTo(PractitionerSamples.R4.create().practitioner("ppr1"));
  }

  @Test
  void readRaw() {
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(ids.lookup("ppr1")).thenReturn(List.of(id("pr1")));
    PractitionerEntity entity =
        PractitionerEntity.builder().npi("12345").payload("payload!").build();
    when(repository.findById("pr1")).thenReturn(Optional.of(entity));
    var actual = controller().readRaw("ppr1", response);
    assertThat(actual).isEqualTo("payload!");
  }

  @Test
  void toBundle() {
    when(ids.register(any()))
        .thenReturn(
            List.of(
                registration("pr1", "ppr1"),
                registration("pr2", "ppr2"),
                registration("pr3", "ppr3")));
    var bundler = controller().toBundle();
    PractitionerSamples.Datamart datamart = PractitionerSamples.Datamart.create();
    var vr =
        VulcanResult.<PractitionerEntity>builder()
            .paging(
                paging(
                    "http://fonzy.com/r4/Practitioner?identifier=p1&page=%d&_count=%d",
                    1, 4, 5, 6, 9, 15))
            .entities(
                Stream.of(
                    datamart.entity("pr1", "loc1", "org1"),
                    datamart.entity("pr2", "loc2", "org2"),
                    datamart.entity("pr3", "loc3", "org3")))
            .build();
    PractitionerSamples.R4 r4 = PractitionerSamples.R4.create();
    Practitioner.Bundle expected =
        r4.asBundle(
            "http://fonzy.com/r4",
            List.of(r4.practitioner("ppr1"), r4.practitioner("ppr2"), r4.practitioner("ppr3")),
            999,
            PractitionerSamples.R4.link(
                BundleLink.LinkRelation.first,
                "http://fonzy.com/r4/Practitioner?identifier=p1",
                1,
                15),
            PractitionerSamples.R4.link(
                BundleLink.LinkRelation.prev,
                "http://fonzy.com/r4/Practitioner?identifier=p1",
                4,
                15),
            PractitionerSamples.R4.link(
                BundleLink.LinkRelation.self,
                "http://fonzy.com/r4/Practitioner?identifier=p1",
                5,
                15),
            PractitionerSamples.R4.link(
                BundleLink.LinkRelation.next,
                "http://fonzy.com/r4/Practitioner?identifier=p1",
                6,
                15),
            PractitionerSamples.R4.link(
                BundleLink.LinkRelation.last,
                "http://fonzy.com/r4/Practitioner?identifier=p1",
                9,
                15));
    var applied = bundler.apply(vr);
    assertThat(applied).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(strings = {"?_id=pr1"})
  @SneakyThrows
  void validRequest(String query) {
    when(ids.register(any())).thenReturn(List.of(registration("pr1", "ppr1")));
    PractitionerSamples.Datamart dm = PractitionerSamples.Datamart.create();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl(
                    List.of(dm.entity("pr1", "loc1", "org1")),
                    i.getArgument(1, Pageable.class),
                    1));
    var r = requestFromUri("http://fonzy.com/r4/Practitioner" + query);
    var actual = controller().search(r);
    assertThat(actual.entry()).hasSize(1);
  }
}
