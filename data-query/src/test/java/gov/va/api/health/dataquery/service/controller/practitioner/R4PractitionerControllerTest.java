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
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.vulcan.InvalidRequest;
import gov.va.api.lighthouse.vulcan.VulcanResult;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public class R4PractitionerControllerTest {
  IdentityService ids = mock(IdentityService.class);

  PractitionerRepository repository = mock(PractitionerRepository.class);

  R4PractitionerController _controller() {
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
  @ValueSource(
      strings = {
        "",
        "?_id=foo&identifier=bar",
        "?invalid=request",
        "?name=harry&family=potter",
        "?name=harry&given=harry"
      })
  void invalidRequest(String query) {
    var r = requestFromUri("http://fonzy.com/r4/Practitioner" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> _controller().search(r));
  }

  @Test
  void read() {
    when(ids.register(any())).thenReturn(List.of(registration("111:S", "I2-111")));
    when(ids.lookup("I2-111")).thenReturn(List.of(id("111:S")));
    PractitionerEntity entity =
        PractitionerSamples.Datamart.create().entity("111:S", "222:I", "333:L");
    when(repository.findById(CompositeCdwId.fromCdwId("111:S"))).thenReturn(Optional.of(entity));
    assertThat(_controller().read("I2-111"))
        .isEqualTo(PractitionerSamples.R4.create().practitioner("I2-111"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"npi-1234567890", "NPI-1234567890"})
  void readByNpi(String npi) {
    when(ids.register(any())).thenReturn(List.of(registration("111:S", "I2-111")));
    PractitionerEntity entity =
        PractitionerSamples.Datamart.create().entity("111:S", "222:I", "333:L");
    when(repository.findByNpi("1234567890", Pageable.unpaged()))
        .thenReturn(new PageImpl<PractitionerEntity>(List.of(entity)));
    assertThat(_controller().read(npi))
        .isEqualTo(PractitionerSamples.R4.create().practitioner("I2-111"));
  }

  @Test
  void readRaw() {
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(ids.lookup("I2-111")).thenReturn(List.of(id("111:S")));
    PractitionerEntity entity = PractitionerEntity.builder().npi("12345").payload("{}").build();
    when(repository.findById(CompositeCdwId.fromCdwId("111:S"))).thenReturn(Optional.of(entity));
    assertThat(_controller().readRaw("I2-111", response)).isEqualTo("{}");
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
    PractitionerSamples.Datamart datamart = PractitionerSamples.Datamart.create();
    var vr =
        VulcanResult.<PractitionerEntity>builder()
            .paging(
                paging(
                    "http://fonzy.com/r4/Practitioner?identifier=p1&page=%d&_count=%d",
                    1, 4, 5, 6, 9, 15))
            .entities(
                Stream.of(
                    datamart.entity("111:S", "111:I", "111:L"),
                    datamart.entity("222:S", "222:I", "222:L"),
                    datamart.entity("333:S", "333:I", "333:L")))
            .build();
    PractitionerSamples.R4 r4 = PractitionerSamples.R4.create();
    Practitioner.Bundle expected =
        PractitionerSamples.R4.asBundle(
            "http://fonzy.com/r4",
            List.of(
                r4.practitioner("I2-111"), r4.practitioner("I2-222"), r4.practitioner("I2-333")),
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
  @SuppressWarnings("unchecked")
  @ValueSource(
      strings = {
        "?_id=111:S",
        "?identifier=111:S",
        "?identifier=http://hl7.org/fhir/sid/us-npi|",
        "?identifier=http://hl7.org/fhir/sid/us-npi|123",
        "?family=potter",
        "?given=harry",
        "?name=harry",
        "?name=potter"
      })
  void validRequest(String query) {
    when(ids.register(any())).thenReturn(List.of(registration("111:S", "I2-111")));
    PractitionerSamples.Datamart dm = PractitionerSamples.Datamart.create();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl<PractitionerEntity>(
                    List.of(dm.entity("111:S", "222:I", "333:L")),
                    i.getArgument(1, Pageable.class),
                    1));
    var r = requestFromUri("http://fonzy.com/r4/Practitioner" + query);
    var actual = _controller().search(r);
    assertThat(actual.entry()).hasSize(1);
  }
}
