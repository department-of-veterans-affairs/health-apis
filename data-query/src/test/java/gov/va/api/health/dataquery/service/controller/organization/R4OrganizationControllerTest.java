package gov.va.api.health.dataquery.service.controller.organization;

import static gov.va.api.health.dataquery.service.controller.MockRequests.paging;
import static gov.va.api.health.dataquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.dataquery.service.controller.organization.OrganizationSamples.id;
import static gov.va.api.health.dataquery.service.controller.organization.OrganizationSamples.json;
import static gov.va.api.health.dataquery.service.controller.organization.OrganizationSamples.registration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.Organization;
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
public class R4OrganizationControllerTest {
  @Mock IdentityService ids;

  @Mock OrganizationRepository repository;

  R4OrganizationController controller() {
    return new R4OrganizationController(
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
  @ValueSource(strings = {"?identifier=|vha_123"})
  void emptyBundle(String query) {
    var url = "http://fonzy.com/r4/Organization" + query;
    var request = requestFromUri(url);
    var bundle = controller().search(request);
    assertThat(bundle.total()).isEqualTo(0);
    assertThat(bundle.entry()).isEmpty();
  }

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = {"?_id=321&identifier=123"})
  void invalidRequest(String query) {
    var r = requestFromUri("http://fonzy.com/r4/Organization" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> controller().search(r));
  }

  @Test
  void read() {
    when(ids.register(any())).thenReturn(List.of(registration("or1", "por1")));
    when(ids.lookup("por1")).thenReturn(List.of(id("or1")));
    var entity = OrganizationSamples.Datamart.create().entity("or1");
    when(repository.findById("or1")).thenReturn(Optional.of(entity));
    assertThat(json(controller().read("por1")))
        .isEqualTo(json(OrganizationSamples.R4.create().organization("por1")));
  }

  @Test
  void readRaw() {
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(ids.lookup("por1")).thenReturn(List.of(id("or1")));
    var entity = OrganizationEntity.builder().cdwId("or1").payload("payload!").build();
    when(repository.findById("or1")).thenReturn(Optional.of(entity));
    var actual = controller().readRaw("por1", response);
    assertThat(actual).isEqualTo("payload!");
  }

  @Test
  void toBundle() {
    when(ids.register(any()))
        .thenReturn(
            List.of(
                registration("or1", "por1"),
                registration("or2", "por2"),
                registration("or3", "por3")));
    var bundler = controller().toBundle();
    var datamart = OrganizationSamples.Datamart.create();
    var vr =
        VulcanResult.<OrganizationEntity>builder()
            .paging(
                paging(
                    "http://fonzy.com/r4/Organization?identifier=o1&page=%d&_count=%d",
                    1, 4, 5, 6, 9, 15))
            .entities(
                Stream.of(datamart.entity("or1"), datamart.entity("or2"), datamart.entity("or3")))
            .build();
    var r4 = OrganizationSamples.R4.create();
    Organization.Bundle expected =
        r4.asBundle(
            "http://fonzy.com/r4",
            List.of(r4.organization("por1"), r4.organization("por2"), r4.organization("por3")),
            999,
            r4.link(
                BundleLink.LinkRelation.first,
                "http://fonzy.com/r4/Organization?identifier=o1",
                1,
                15),
            r4.link(
                BundleLink.LinkRelation.prev,
                "http://fonzy.com/r4/Organization?identifier=o1",
                4,
                15),
            r4.link(
                BundleLink.LinkRelation.self,
                "http://fonzy.com/r4/Organization?identifier=o1",
                5,
                15),
            r4.link(
                BundleLink.LinkRelation.next,
                "http://fonzy.com/r4/Organization?identifier=o1",
                6,
                15),
            r4.link(
                BundleLink.LinkRelation.last,
                "http://fonzy.com/r4/Organization?identifier=o1",
                9,
                15));
    var applied = bundler.apply(vr);
    assertThat(applied).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=or1",
        "?identifier=or1",
        "?identifier=vha_123",
        "?identifier=https://api.va.gov/services/"
            + "fhir/v0/r4/NamingSystem/va-facility-identifier|vha_123",
        "?identifier=https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-facility-identifier|",
        "?name=NEW+AMSTERDAM+CBOC",
        "?address=10+MONROE+AVE,+SUITE+6B+PO+BOX+4160",
        "?address-city=NEW+AMSTERDAM",
        "?address-state=OH",
        "?address-postalcode=44444-4160",
        "?address=10+MONROE+AVE,+SUITE+6B+PO+BOX+4160&address-city=NEW+AMSTERDAM",
        "?address=10+MONROE+AVE,+SUITE+6B+PO+BOX+4160&address-city=NEW+AMSTERDAM&address-state=OH",
        "?address=10+MONROE+AVE,+SUITE+6B+PO+BOX+4160&address-city=NEW+AMSTERDAM&"
            + "address-state=OH&address-postalcode=44444-4160"
      })
  @SneakyThrows
  void validRequest(String query) {
    when(ids.register(any())).thenReturn(List.of(OrganizationSamples.registration("or1", "por1")));
    var dm = OrganizationSamples.Datamart.create();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i -> new PageImpl(List.of(dm.entity("or1")), i.getArgument(1, Pageable.class), 1));
    var request = requestFromUri("http://fonzy.com/r4/Organization" + query);
    var actual = controller().search(request);
    assertThat(actual.entry()).hasSize(1);
  }
}
