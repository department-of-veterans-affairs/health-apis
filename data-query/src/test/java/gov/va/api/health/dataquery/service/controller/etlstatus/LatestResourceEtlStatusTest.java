package gov.va.api.health.dataquery.service.controller.etlstatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class LatestResourceEtlStatusTest {
  @Mock LatestResourceEtlStatusRepository repository;

  private LatestResourceEtlStatusController _controller() {
    return new LatestResourceEtlStatusController(repository);
  }

  private LatestResourceEtlStatusEntity _makeEntity(String resource, Instant time) {
    return LatestResourceEtlStatusEntity.builder().resourceName(resource).endDateTime(time).build();
  }

  @Test
  void allHealthyIsOverallHealthy() {
    when(repository.findAll())
        .thenReturn(
            List.of(
                _makeEntity("AllergyIntolerance", Instant.now()),
                _makeEntity("Condition", Instant.now())));
    ResponseEntity<Health> actual = _controller().resourceStatusHealth();
    assertThat(actual.getBody().getStatus()).isEqualTo(Status.UP);
  }

  @Test
  void allUnhealthyIsOverallUnhealthy() {
    when(repository.findAll())
        .thenReturn(
            List.of(
                _makeEntity("AllergyIntolerance", Instant.now().minus(2, ChronoUnit.DAYS)),
                _makeEntity("Condition", Instant.now().minus(2, ChronoUnit.DAYS))));
    ResponseEntity<Health> actual = _controller().resourceStatusHealth();
    assertThat(actual.getBody().getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  void clearCache() {
    _controller().clearResourceStatusScheduler();
  }

  @Test
  void healthyAndUnhealthyIsOverallUnhealthy() {
    when(repository.findAll())
        .thenReturn(
            List.of(
                _makeEntity("AllergyIntolerance", Instant.now()),
                _makeEntity("Condition", Instant.now().minus(2, ChronoUnit.DAYS))));
    ResponseEntity<Health> actual = _controller().resourceStatusHealth();
    assertThat(actual.getBody().getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  void nullIsOverallUnhealthy() {
    when(repository.findAll()).thenReturn(List.of());
    ResponseEntity<Health> actual = _controller().resourceStatusHealth();
    assertThat(actual.getBody().getStatus()).isEqualTo(Status.DOWN);
  }
}
