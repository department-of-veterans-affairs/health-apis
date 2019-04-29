package gov.va.api.health.dataquery.service.healthcheck;

/**
 * Records successful and failure resource events from source (e.g. Mr. Anderson). A success event
 * resets consecutive failure count and vice versa.
 */
public interface SteelThreadSystemCheckLedger {

  /** How many failures in a row (without any successes) were reported. */
  int getConsecutiveFailureCount();

  /** Register a failure event and return current failure count. */
  int recordFailure();

  /** Register a success event. */
  void recordSuccess();
}
