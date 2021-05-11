package gov.va.api.health.dataquery.service.controller.procedure;

import static gov.va.api.health.dataquery.service.controller.MockRequests.paging;
import static gov.va.api.health.dataquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.dataquery.service.controller.procedure.ProcedureSamples.id;
import static gov.va.api.health.dataquery.service.controller.procedure.ProcedureSamples.registration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.Procedure;
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
public class R4ProcedureControllerTest {

  @Mock IdentityService ids;

  @Mock private ProcedureRepository repository;

  R4ProcedureController controller() {
    return new R4ProcedureController(
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
  @ValueSource(strings = {"?nachos=friday", "?patient:identifier=p1"})
  @SneakyThrows
  void emptyBundle(String query) {
    var url = "http://fonzy.com/r4/Procedure" + query;
    var request = requestFromUri(url);
    var bundle = controller().search(request);
    assertThat(bundle.total()).isEqualTo(0);
    assertThat(bundle.entry()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=321&identifier=123",
        "?_id=678&patient=p1",
        "?identifier=935&patient=p1",
        "?date=gt2020",
        "?patient=p1&date=nope",
        "?patient:RelatedPerson=p1",
        "?patient=RelatedPerson/p1",
        "?patient=http://fonzy.com/r4/RelatedPerson/p1"
      })
  @SneakyThrows
  void invalidRequest(String query) {
    var r = requestFromUri("http://fonzy.com/r4/Procedure" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> controller().search(r));
  }

  @Test
  public void read() {
    when(ids.register(any())).thenReturn(List.of(registration("pr1", "ppr1")));
    when(ids.lookup("ppr1")).thenReturn(List.of(id("pr1")));
    ProcedureEntity entity = ProcedureSamples.Datamart.create().entity("pr1", "p1");
    when(repository.findById("pr1")).thenReturn(Optional.of(entity));
    assertThat(controller().read("ppr1"))
        .isEqualTo(ProcedureSamples.R4.create().procedure("ppr1", "p1", "2008-01-02T06:00:00Z"));
  }

  @Test
  public void readRaw() {
    when(ids.lookup("ppr1")).thenReturn(List.of(id("pr1")));
    ProcedureEntity entity = ProcedureEntity.builder().icn("p1").payload("payload!").build();
    when(repository.findById("pr1")).thenReturn(Optional.of(entity));
    var actual = controller().readRaw("ppr1", mock(HttpServletResponse.class));
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
    var datamart = ProcedureSamples.Datamart.create();
    var vr =
        VulcanResult.<ProcedureEntity>builder()
            .paging(
                paging(
                    "http://fonzy.com/r4/Procedure?patient=p1&page=%d&_count=%d",
                    1, 4, 5, 6, 9, 15))
            .entities(
                Stream.of(
                    datamart.entity("pr1", "p1"),
                    datamart.entity("pr2", "p1"),
                    datamart.entity("pr3", "p1")))
            .build();
    ProcedureSamples.R4 r4 = ProcedureSamples.R4.create();
    Procedure.Bundle expected =
        r4.asBundle(
            "http://fonzy.com/r4",
            List.of(
                r4.procedure("ppr1", "p1", "2008-01-02T06:00:00Z"),
                r4.procedure("ppr2", "p1", "2008-01-02T06:00:00Z"),
                r4.procedure("ppr3", "p1", "2008-01-02T06:00:00Z")),
            999,
            r4.link(
                BundleLink.LinkRelation.first, "http://fonzy.com/r4/Procedure?patient=p1", 1, 15),
            r4.link(
                BundleLink.LinkRelation.prev, "http://fonzy.com/r4/Procedure?patient=p1", 4, 15),
            r4.link(
                BundleLink.LinkRelation.self, "http://fonzy.com/r4/Procedure?patient=p1", 5, 15),
            r4.link(
                BundleLink.LinkRelation.next, "http://fonzy.com/r4/Procedure?patient=p1", 6, 15),
            r4.link(
                BundleLink.LinkRelation.last, "http://fonzy.com/r4/Procedure?patient=p1", 9, 15));
    var applied = bundler.apply(vr);
    assertThat(applied).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=pr1",
        "?identifier=pr1",
        "?patient=p1",
        "?patient=Patient/p1",
        "?patient=http://fonzy.com/r4/Patient/p1",
        "?patient:Patient=p1",
        "?patient=Patient/p1&date=le2009",
        "?patient=p1&date=2009",
        "?patient=p1&date=gt2020",
        "?patient=p1&date=2003&date=2007",
        "?patient=p1&date=gt2004&date=lt2006"
      })
  @SneakyThrows
  void validRequests(String query) {
    when(ids.register(any())).thenReturn(List.of(registration("pr1", "ppr1")));
    ProcedureSamples.Datamart dm = ProcedureSamples.Datamart.create();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl(List.of(dm.entity("pr1", "p1")), i.getArgument(1, Pageable.class), 1));
    var r = requestFromUri("http://fonzy.com/r4/Procedure" + query);
    var actual = controller().search(r);
    assertThat(actual.entry()).hasSize(1);
  }
}
