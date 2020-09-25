package gov.va.api.health.dataquery.service.controller.etlstatus;

import com.google.common.collect.Streams;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(
    value = {"/etl-status"},
    produces = {"application/json"})
public class LatestResourceEtlStatusController {
  private final AtomicBoolean hasCachedResourceStatus = new AtomicBoolean(false);

  private LatestResourceEtlStatusRepository repository;

  /**
   * Compares Latest Resource ETL Status Entities to Health
   *
   * <p>Health is compared to 36 hours before it got called for validity.
   */
  private static Health toHealth(LatestResourceEtlStatusEntity entity, Instant tooLongAgo) {

    return Health.status(
            new Status(
                entity.endDateTime().isBefore(tooLongAgo) ? "DOWN" : "UP", entity.resourceName()))
        .withDetail("endDateTime", entity.endDateTime())
        .build();
  }

  /** Clears cache every 5 minutes. */
  @Scheduled(cron = "0 */5 * * * *")
  @CacheEvict(value = "resource-status")
  public void clearResourceStatusScheduler() {
    if (hasCachedResourceStatus.getAndSet(false)) {
      // reduce log spam by only reporting cleared if we've actually cached something
      log.info("Clearing resource-status cache");
    }
  }

  ResponseEntity<Health> resourceStatusHealth(Instant now) {
    hasCachedResourceStatus.set(true);
    Instant tooLongAgo = now.minus(36, ChronoUnit.HOURS);
    Iterable<LatestResourceEtlStatusEntity> entities = repository.findAll();
    List<Health> statusDetails =
        Streams.stream(entities).map(e -> toHealth(e, tooLongAgo)).collect(Collectors.toList());
    boolean isDown =
        statusDetails.isEmpty()
            || statusDetails.stream().anyMatch(h -> h.getStatus().equals(Status.DOWN));
    Health overallHealth =
        Health.status(new Status(isDown ? "DOWN" : "UP", "Resource ETL Statuses"))
            .withDetail("time", now)
            .withDetail("statuses", statusDetails)
            .build();
    if (!overallHealth.getStatus().equals(Status.UP)) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(overallHealth);
    }
    return ResponseEntity.ok(overallHealth);
  }

  /**
   * Gets health of etl resources.
   *
   * <p>Results are cached in resource-status to limit the amount of queries to once every 5
   * minutes.
   *
   * <p>Every 5 minutes, the cache is invalidated.
   */
  @Cacheable("resource-status")
  @GetMapping
  public ResponseEntity<Health> resourceStatusHealth() {
    return resourceStatusHealth(Instant.now());
  }
}
