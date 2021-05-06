package gov.va.api.health.dataquery.service.controller.procedure;

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
  @ValueSource(
      strings = {
        "?_id=321&identifier=123",
        "?_id=678&patient=p1",
        "?identifier=935&patient=p1",
        "?date=2020-1-20T16:35:00Z"
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

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=pr1",
        "?identifier=pr1",
        "?patient=p1",
        "?patient=p1&date=2020-1-20T16:35:00Z",
        "?patient=p1&date=gt2020-1-20",
        "?patient=p1&date=2020-1-20T16:35:00Z&date=2020-2-20T16:35:00Z",
        "?patient=p1&date=gt2020-1-20&date=lt2020-2-20T"
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
