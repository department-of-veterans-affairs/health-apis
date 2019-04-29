package gov.va.api.health.dataquery.service.healthcheck;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SteelThreadSystemCheckLedgerImplTest {

  @Test
  public void addingFailureEventIncrementsFailureCount() {
    SteelThreadSystemCheckLedgerImpl recorder = new SteelThreadSystemCheckLedgerImpl();
    recorder.recordFailure();
    assertThat(recorder.getConsecutiveFailureCount()).isOne();
  }

  @Test
  public void addingSuccessEventClearsFailureCount() {
    SteelThreadSystemCheckLedgerImpl recorder = new SteelThreadSystemCheckLedgerImpl();
    // Kick the failure count up for this test to make sure it resets.
    recorder.recordFailure();
    assertThat(recorder.getConsecutiveFailureCount()).isOne();
    // Add a success and verify it's set and failure is cleared.
    recorder.recordSuccess();
    assertThat(recorder.getConsecutiveFailureCount()).isZero();
  }
}
