package gov.va.api.health.dataquery.service.controller.etlstatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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

  /** Clears cache every 5 minutes. */
  @Scheduled(cron = "0 */5 * * * *")
  @CacheEvict(value = "resource-status")
  public void clearResourceStatusScheduler() {
    if (hasCachedResourceStatus.getAndSet(false)) {
      // reduce log spam by only reporting cleared if we've actually cached something
      log.info("Clearing etl-status cache");
    }
  }

  /**
   * Gets health of etl resources.
   *
   * <p>Results are cached in collection-status to limit the amount of queries to once every 5
   * minutes.
   *
   * <p>Every 5 minutes, the cache is invalidated.
   */
  @Cacheable("resource-status")
  @GetMapping(value = "/")
  public ResponseEntity<Health> resourceStatusHealth() {
    hasCachedResourceStatus.set(true);
    Instant tooLongAgo = Instant.now().minus(1, ChronoUnit.DAYS);
    AtomicBoolean overall = new AtomicBoolean(true);
    Iterable<LatestResourceEtlStatusEntity> receivedData = repository.findAll();
    if (StreamSupport.stream(receivedData.spliterator(), false).count() == 0) {
      overall.set(false);
    }
    List<Health> details =
        StreamSupport.stream(receivedData.spliterator(), true)
            .map(
                e -> {
                  if (e.endDateTime().isBefore(tooLongAgo)) {
                    overall.set(false);
                  }
                  return toHealth(e, tooLongAgo);
                })
            .collect(Collectors.toList());
    Health overallHealth =
        Health.status(new Status(overall.get() == false ? "DOWN" : "UP", "Downstream services"))
            .withDetail("name", "All downstream services")
            .withDetail("downstreamServices", details)
            .withDetail("time", Instant.now())
            .build();
    if (!overallHealth.getStatus().equals(Status.UP)) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(overallHealth);
    }
    return ResponseEntity.ok(overallHealth);
  }

  private Health toHealth(LatestResourceEtlStatusEntity entity, Instant tooLongAgo) {

    return Health.status(
            new Status(
                entity.endDateTime().isBefore(tooLongAgo) ? "DOWN" : "UP", entity.resourceName()))
        .withDetail("endDateTime", entity.endDateTime())
        .build();
  }
}
