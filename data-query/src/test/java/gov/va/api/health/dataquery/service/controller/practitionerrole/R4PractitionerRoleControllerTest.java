package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static gov.va.api.health.dataquery.service.controller.MockRequests.paging;
import static gov.va.api.health.dataquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.dataquery.service.controller.practitionerrole.PractitionerRoleSamples.id;
import static gov.va.api.health.dataquery.service.controller.practitionerrole.PractitionerRoleSamples.registration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerEntity;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerRepository;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.PractitionerRole;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.vulcan.InvalidRequest;
import gov.va.api.lighthouse.vulcan.VulcanResult;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
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
public class R4PractitionerRoleControllerTest {
  @Mock IdentityService ids;

  @Mock PractitionerRepository repository;

  R4PractitionerRoleController _controller() {
    return new R4PractitionerRoleController(
        WitnessProtection.builder().identityService(ids).build(),
        LinkProperties.builder()
            .publicUrl("http://fonzy.com")
            .publicR4BasePath("r4")
            .publicStu3BasePath("stu3")
            .publicDstu2BasePath("dstu2")
            .maxPageSize(20)
            .defaultPageSize(15)
            .build(),
        repository);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "?unknownparam=123"})
  void invalidRequests(String query) {
    var r = requestFromUri("http://fonzy.com/r4/PractitionerRole" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> _controller().search(r));
  }

  @Test
  void read() {
    when(ids.register(any())).thenReturn(List.of(registration("111:S", "I2-111")));
    when(ids.lookup("I2-111")).thenReturn(List.of(id("111:S")));
    PractitionerEntity entity =
        PractitionerRoleSamples.Datamart.create().entity("111:S", "loc1", "org1");
    when(repository.findById(CompositeCdwId.fromCdwId("111:S"))).thenReturn(Optional.of(entity));
    assertThat(_controller().read("I2-111"))
        .isEqualTo(PractitionerRoleSamples.R4.create().practitionerRole("I2-111", "org1", "loc1"));
  }

  @Test
  void readRaw() {
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(ids.lookup("I2-111")).thenReturn(List.of(id("111:S")));
    PractitionerEntity entity =
        PractitionerEntity.builder().npi("12345").payload("payload!").build();
    when(repository.findById(CompositeCdwId.fromCdwId("111:S"))).thenReturn(Optional.of(entity));
    assertThat(_controller().readRaw("I2-111", response)).isEqualTo("payload!");
  }

  @Test
  void toBundle() {
    when(ids.register(any()))
        .thenReturn(
            List.of(
                registration("111:S", "I2-111"),
                registration("222:S", "I2-222"),
                registration("333:S", "I2-333")));
    var bundler = _controller().toBundle();
    PractitionerRoleSamples.Datamart datamart = PractitionerRoleSamples.Datamart.create();
    var vr =
        VulcanResult.<PractitionerEntity>builder()
            .paging(
                paging(
                    "http://fonzy.com/r4/PractitionerRole?practitioner.identifier=pr1&page=%d&_count=%d",
                    1, 4, 5, 6, 9, 15))
            .entities(
                Stream.of(
                    datamart.entity("111:S", "loc1", "org1"),
                    datamart.entity("222:S", "loc2", "org2"),
                    datamart.entity("333:S", "loc3", "org3")))
            .build();
    PractitionerRoleSamples.R4 r4 = PractitionerRoleSamples.R4.create();
    PractitionerRole.Bundle expected =
        PractitionerRoleSamples.R4.asBundle(
            "http://fonzy.com/r4",
            List.of(
                r4.practitionerRole("I2-111", "org1", "loc1"),
                r4.practitionerRole("I2-222", "org2", "loc2"),
                r4.practitionerRole("I2-333", "org3", "loc3")),
            999,
            PractitionerRoleSamples.R4.link(
                BundleLink.LinkRelation.first,
                "http://fonzy.com/r4/PractitionerRole?practitioner.identifier=pr1",
                1,
                15),
            PractitionerRoleSamples.R4.link(
                BundleLink.LinkRelation.prev,
                "http://fonzy.com/r4/PractitionerRole?practitioner.identifier=pr1",
                4,
                15),
            PractitionerRoleSamples.R4.link(
                BundleLink.LinkRelation.self,
                "http://fonzy.com/r4/PractitionerRole?practitioner.identifier=pr1",
                5,
                15),
            PractitionerRoleSamples.R4.link(
                BundleLink.LinkRelation.next,
                "http://fonzy.com/r4/PractitionerRole?practitioner.identifier=pr1",
                6,
                15),
            PractitionerRoleSamples.R4.link(
                BundleLink.LinkRelation.last,
                "http://fonzy.com/r4/PractitionerRole?practitioner.identifier=pr1",
                9,
                15));
    var applied = bundler.apply(vr);
    assertThat(applied).isEqualTo(expected);
  }

  @ParameterizedTest
  @SuppressWarnings("unchecked")
  @ValueSource(strings = {"?_id=111:S"})
  void validRequests(String query) {
    when(ids.register(any())).thenReturn(List.of(registration("111:S", "I2-111")));
    PractitionerRoleSamples.Datamart dm = PractitionerRoleSamples.Datamart.create();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl<PractitionerEntity>(
                    List.of(dm.entity("111:S", "loc1", "org1")),
                    i.getArgument(1, Pageable.class),
                    1));
    var r = requestFromUri("http://fonzy.com/r4/PractitionerRole" + query);
    var actual = _controller().search(r);
    assertThat(actual.entry()).hasSize(1);
  }
}
