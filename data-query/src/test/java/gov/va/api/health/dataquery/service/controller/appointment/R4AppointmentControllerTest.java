package gov.va.api.health.dataquery.service.controller.appointment;

import static gov.va.api.health.dataquery.service.controller.condition.ConditionSamples.id;
import static gov.va.api.health.dataquery.service.controller.condition.ConditionSamples.registration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
  void read() {
    when(ids.register(any())).thenReturn(List.of(registration("1:A", "pc1")));
    when(ids.lookup("pc1")).thenReturn(List.of(id("1:A")));
    AppointmentEntity entity = AppointmentSamples.Datamart.create().entity("1", "A", "p1");
    when(repository.findById(CompositeCdwId.fromCdwId("1:A"))).thenReturn(Optional.of(entity));
    assertThat(controller().read("pc1"))
        .isEqualTo(AppointmentSamples.R4.create().appointment("1:A", "p1"));
  }

  @Test
  void readRaw() {
    when(ids.lookup("pc1")).thenReturn(List.of(id("1:A")));
    AppointmentEntity entity =
        AppointmentEntity.builder()
            .cdwIdNumber(new BigInteger("1"))
            .cdwIdResourceCode('A')
            .icn("p1")
            .payload("payload")
            .build();
    when(repository.findById(CompositeCdwId.fromCdwId("1:A"))).thenReturn(Optional.of(entity));
    assertThat(controller().readRaw("pc1", mock(HttpServletResponse.class))).isEqualTo("payload");
  }
}
