package gov.va.api.health.dataquery.service.controller.etlstatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.validation.constraints.NotNull;
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
@RequestMapping(
    value = {"/latestresourceetlstatus"},
    produces = {"application/json"})
public class LatestResourceEtlStatusController {
  private final AtomicBoolean hasCachedCollectionStatus = new AtomicBoolean(false);

  private LatestResourceEtlStatusRepository repository;

  /** All args constructor. */
  public LatestResourceEtlStatusController(
      @Autowired LatestResourceEtlStatusRepository repository) {
    this.repository = repository;
  }

  /** Clears cache every 5 minutes. */
  @Scheduled(cron = "0 */5 * * * *")
  @CacheEvict(value = "collection-status")
  public void clearCollectionStatusScheduler() {
    if (hasCachedCollectionStatus.getAndSet(false)) {
      // reduce log spam by only reporting cleared if we've actually cached something
      log.info("Clearing etl-collection-status cache");
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
  @Cacheable("collection-status")
  @GetMapping(value = "/general-status")
  public ResponseEntity<Health> collectionStatusHealth() {
    hasCachedCollectionStatus.set(true);
    Instant tooLongAgo = Instant.now().minus(1, ChronoUnit.DAYS);
    AtomicBoolean overall = new AtomicBoolean(false);
    List<Health> details =
        StreamSupport.stream(repository.findAll().spliterator(), true)
            .map(
                e -> {
                  if (e.endDateTime().isBefore(tooLongAgo)) {
                    overall.set(false);
                  }
                  return toHealth(e);
                })
            .collect(Collectors.toList());
    Health overallHealth =
        Health.status(
                new Status(
                    details.stream().anyMatch(h -> h.getStatus().equals(Status.DOWN))
                        ? "DOWN"
                        : "UP",
                    "Downstream services"))
            .withDetail("name", "All downstream services")
            .withDetail("downstreamServices", details)
            .withDetail("time", Instant.now())
            .build();
    if (!overallHealth.getStatus().equals(Status.UP)) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(overallHealth);
    }
    return ResponseEntity.ok(overallHealth);
  }

  private Health toHealth(@NotNull LatestResourceEtlStatusEntity entity) {
    Instant tooLongAgo = Instant.now().minus(1, ChronoUnit.DAYS);
    return Health.status(new Status(entity.endDateTime().isBefore(tooLongAgo) ? "DOWN" : "UP"))
        .withDetail("name", entity.resourceName())
        .withDetail("lastUpdated", entity.endDateTime())
        .withDetail("time", Instant.now())
        .build();
  }
}
