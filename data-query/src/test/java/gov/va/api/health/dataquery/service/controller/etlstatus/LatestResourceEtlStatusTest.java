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

  private Health _convertHealth(LatestResourceEtlStatusEntity entity, String expectedStatus) {
    return Health.status(new Status(expectedStatus, entity.resourceName()))
        .withDetail("endDateTime", entity.endDateTime())
        .build();
  }

  private LatestResourceEtlStatusEntity _makeEntity(String resource, Instant time) {
    return LatestResourceEtlStatusEntity.builder().resourceName(resource).endDateTime(time).build();
  }

  @Test
  void allHealthyIsOverallHealthy() {
    Instant now = Instant.now();
    LatestResourceEtlStatusEntity firstEntity = _makeEntity("AllergyIntolerance", now);
    LatestResourceEtlStatusEntity secondEntity = _makeEntity("Condition", now);
    when(repository.findAll()).thenReturn(List.of(firstEntity, secondEntity));
    ResponseEntity<Health> actual = _controller().resourceStatusHealth(now);
    Health firstHealth = _convertHealth(firstEntity, "UP");
    Health secondHealth = _convertHealth(secondEntity, "UP");
    assertThat(actual.getBody())
        .isEqualTo(
            Health.status(new Status("UP", "Resource ETL Statuses"))
                .withDetail("time", now)
                .withDetail("statuses", List.of(firstHealth, secondHealth))
                .build());
  }

  @Test
  void allUnhealthyIsOverallUnhealthy() {
    Instant now = Instant.now();
    LatestResourceEtlStatusEntity firstEntity =
        _makeEntity("AllergyIntolerance", now.minus(2, ChronoUnit.DAYS));
    LatestResourceEtlStatusEntity secondEntity =
        _makeEntity("Condition", now.minus(2, ChronoUnit.DAYS));
    when(repository.findAll()).thenReturn(List.of(firstEntity, secondEntity));
    ResponseEntity<Health> actual = _controller().resourceStatusHealth(now);
    Health firstHealth = _convertHealth(firstEntity, "DOWN");
    Health secondHealth = _convertHealth(secondEntity, "DOWN");
    assertThat(actual.getBody())
        .isEqualTo(
            Health.status(new Status("DOWN", "Resource ETL Statuses"))
                .withDetail("time", now)
                .withDetail("statuses", List.of(firstHealth, secondHealth))
                .build());
  }

  @Test
  void clearCache() {
    _controller().clearResourceStatusScheduler();
  }

  @Test
  void healthyAndUnhealthyIsOverallUnhealthy() {
    Instant now = Instant.now();
    LatestResourceEtlStatusEntity firstEntity = _makeEntity("AllergyIntolerance", now);
    LatestResourceEtlStatusEntity secondEntity =
        _makeEntity("Condition", now.minus(2, ChronoUnit.DAYS));
    when(repository.findAll()).thenReturn(List.of(firstEntity, secondEntity));
    ResponseEntity<Health> actual = _controller().resourceStatusHealth(now);
    Health firstHealth = _convertHealth(firstEntity, "UP");
    Health secondHealth = _convertHealth(secondEntity, "DOWN");
    assertThat(actual.getBody())
        .isEqualTo(
            Health.status(new Status("DOWN", "Resource ETL Statuses"))
                .withDetail("time", now)
                .withDetail("statuses", List.of(firstHealth, secondHealth))
                .build());
  }

  @Test
  void nullIsOverallUnhealthy() {
    when(repository.findAll()).thenReturn(List.of());
    Instant now = Instant.now();
    ResponseEntity<Health> actual = _controller().resourceStatusHealth(now);
    assertThat(actual.getBody())
        .isEqualTo(
            Health.status(new Status("DOWN", "Resource ETL Statuses"))
                .withDetail("time", now)
                .withDetail("statuses", List.of())
                .build());
  }
}
