package gov.va.api.health.dataquery.service.healthcheck;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/** Implements the recorder. */
@Component
public class SteelThreadSystemCheckLedgerImpl implements SteelThreadSystemCheckLedger {

  /** A count of consecutive failures, will be reset to 0 on success. */
  private AtomicInteger consecutiveFailureCount = new AtomicInteger(0);

  /** How many failures in a row (without any successes) were reported. */
  @Override
  public int getConsecutiveFailureCount() {
    return consecutiveFailureCount.get();
  }

  /** Register a failure event. */
  @Override
  public int recordFailure() {
    return consecutiveFailureCount.incrementAndGet();
  }

  /** Register a success event. */
  @Override
  public void recordSuccess() {
    consecutiveFailureCount.set(0);
  }
}
