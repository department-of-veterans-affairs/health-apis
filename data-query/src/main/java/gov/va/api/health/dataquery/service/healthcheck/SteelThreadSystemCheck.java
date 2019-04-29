package gov.va.api.health.dataquery.service.healthcheck;

import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient.MrAndersonServiceException;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.dvp.cdw.xsd.model.CdwMedication101Root;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Service
@Slf4j
public class SteelThreadSystemCheck implements HealthIndicator {

  private final MrAndersonClient client;

  private final SteelThreadSystemCheckLedger requestStatusLeger;

  private final String id;

  private final int consecutiveFailureThreshold;

  /** 'By hand' all args constructor is required to inject non-string values from our properties. */
  public SteelThreadSystemCheck(
      @Autowired MrAndersonClient client,
      @Autowired SteelThreadSystemCheckLedger requestStatusLeger,
      @Value("${health-check.medication-id}") String id,
      @Value("${health-check.consecutive-failure-threshold}") int consecutiveFailureThreshold) {
    this.client = client;
    this.requestStatusLeger = requestStatusLeger;
    this.id = id;
    this.consecutiveFailureThreshold = consecutiveFailureThreshold;
  }

  @Override
  @SneakyThrows
  public Health health() {
    if ("skip".equals(id)) {
      return Health.up().withDetail("skipped", true).build();
    }
    // If we've exceeded the sequential failure count - bad.
    // We grab the count into a local because there is another thread writing it and we use below.
    int consecutiveFails = requestStatusLeger.getConsecutiveFailureCount();
    if (consecutiveFails > consecutiveFailureThreshold) {
      return Health.down()
          .withDetail(
              "failures",
              String.format(
                  "%d consecutive failures exceeds threshold of %s.", consecutiveFails, 5))
          .build();
    }
    return Health.up().build();
  }

  private Query<CdwMedication101Root> query() {
    return Query.forType(CdwMedication101Root.class)
        .resource("Medication")
        .profile(Query.Profile.ARGONAUT)
        .version("1.01")
        .parameters(Parameters.forIdentity(id))
        .build();
  }

  /**
   * Asynchronously perform the steel thread read and save the results for health check to use.
   * Frequency is configurable via properties.
   */
  @Scheduled(
    fixedDelayString = "${health-check.read-frequency-ms}",
    initialDelayString = "${health-check.read-frequency-ms}"
  )
  @SneakyThrows
  public void runSteelThreadCheckAsynchronously() {
    log.info("Performing health check.");
    try {
      client.search(query());
      requestStatusLeger.recordSuccess();
    } catch (HttpServerErrorException
        | HttpClientErrorException
        | ResourceAccessException
        | MrAndersonServiceException e) {
      int consecutiveFailures = requestStatusLeger.recordFailure();
      log.error("Failed to complete health check. Failure count is " + consecutiveFailures);
    } catch (Exception e) {
      int consecutiveFailures = requestStatusLeger.recordFailure();
      log.error("Failed to complete health check. Failure count is " + consecutiveFailures, e);
      throw e;
    }
  }
}
