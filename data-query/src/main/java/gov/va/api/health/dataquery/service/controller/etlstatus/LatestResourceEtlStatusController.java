package gov.va.api.health.dataquery.service.controller.etlstatus;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.persistence.Cacheable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(
        value = {"/latestresourceetlstatus"},
        produces = {"application/json"})
public class LatestResourceEtlStatusController {

    private LatestResourceEtlStatusRepository repository;

    private final AtomicBoolean hasCachedCollectionStatus = new AtomicBoolean(false);

    private final static String[] RESOURCE_NAMES = {
            "AllergyIntolerance",
            "Condition",
            "DiagnosticReport",
            "FallRisk",
            "Location",
            "Medication",
            "MedicationOrder",
            "MedicationStatement",
            "Observation",
            "Immunization",
            "Patient",
            "Procedure",
            "Organization",
            "Practitioner"
    };

    private Health toHealth (LatestResourceEtlStatusEntity entity){

        return Health.status(new Status(entity == null ? "DOWN" : "UP"))
                .withDetail("name", entity.getResourceName())
                .withDetail("lastUpdated", entity.getEndDateTime())
                .withDetail("time", Instant.now())
                .build();

    }

    @Scheduled(cron = "0 */5 * * * *")
    @CacheEvict(value = "collection-status")
    public void clearCollectionStatusScheduler() {
        if (hasCachedCollectionStatus.getAndSet(false)) {
            // reduce log spam by only reporting cleared if we've actually cached something
            log.info("Clearing etl-collection-status cache");
        }
    }

    @Cacheable("collection-status")
    @GetMapping("/")
    public ResponseEntity<Health> collectionStatusHealth() {
        hasCachedCollectionStatus.set(true);

        Instant tooLongAgo = Instant.now().minus(1, ChronoUnit.DAYS);
        AtomicBoolean overall = new AtomicBoolean(false);
        List<Health> details =
            StreamSupport.stream(repository.findAll().spliterator(), true)
                .map(
                    e -> {
                        if (e.getEndDateTime().isBefore(tooLongAgo)) {
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
        return ResponseEntity.ok(overallHealth)
    }



}
