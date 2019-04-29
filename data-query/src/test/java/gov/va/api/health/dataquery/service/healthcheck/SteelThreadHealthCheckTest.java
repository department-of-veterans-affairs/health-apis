package gov.va.api.health.dataquery.service.healthcheck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.api.resources.Medication;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.dvp.cdw.xsd.model.CdwMedication101Root;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Status;

public class SteelThreadHealthCheckTest {

  private final int failureThresholdForTests = 5;

  @Mock MrAndersonClient client;

  @Mock SteelThreadSystemCheckLedger ledger;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void healthCheckHappyPath() {
    SteelThreadSystemCheck test =
        new SteelThreadSystemCheck(client, ledger, "123", failureThresholdForTests);
    // We'll return exactly the threshold to check the boundary case.
    when(ledger.getConsecutiveFailureCount()).thenReturn(failureThresholdForTests - 1);
    assertThat(test.health().getStatus()).isEqualTo(Status.UP);
  }

  @Test
  public void healthCheckSadPathWhenFailureThresholdExceeded() {
    SteelThreadSystemCheck test =
        new SteelThreadSystemCheck(client, ledger, "123", failureThresholdForTests);
    when(ledger.getConsecutiveFailureCount()).thenReturn(failureThresholdForTests);
    assertThat(test.health().getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void healthCheckSkip() {
    SteelThreadSystemCheck test =
        new SteelThreadSystemCheck(client, ledger, "skip", failureThresholdForTests);
    // Exceed threshold to make sure we're actually skipping.
    when(ledger.getConsecutiveFailureCount()).thenReturn(failureThresholdForTests + 100);
    assertThat(test.health().getStatus()).isEqualTo(Status.UP);
  }

  /**
   * Make sure that when the reads are not working, the failure event is getting kicked in ledger.
   */
  @Test
  public void runSteelThreadExceptionPath() {
    SteelThreadSystemCheck test =
        new SteelThreadSystemCheck(client, ledger, "123", failureThresholdForTests);
    // Just need to thrown any unchecked exception to make sure that we hit the failure reporting.
    when(client.search(Mockito.any())).thenThrow(new IllegalArgumentException("foo"));
    when(ledger.recordFailure()).thenReturn(failureThresholdForTests);

    try {
      test.runSteelThreadCheckAsynchronously();
    } catch (Exception e) {
      // Do nothing. Want to make sure that the failure is recorded and we'll check that below.
    }
    verify(ledger, times(1)).recordFailure();
  }

  /** Make sure that when the reads are working, the happy event is getting kicked in ledger. */
  @Test
  public void runSteelThreadHappyPath() {
    CdwMedication101Root root = new CdwMedication101Root();
    root.setMedications(new CdwMedication101Root.CdwMedications());
    CdwMedication101Root.CdwMedications.CdwMedication xmlMedication =
        new CdwMedication101Root.CdwMedications.CdwMedication();
    root.getMedications().getMedication().add(xmlMedication);
    SteelThreadSystemCheck test =
        new SteelThreadSystemCheck(client, ledger, "123", failureThresholdForTests);
    when(client.search(Mockito.any())).thenReturn(root);
    test.runSteelThreadCheckAsynchronously();
    verify(ledger, times(1)).recordSuccess();
  }

  /**
   * Make sure that when the reads are not working, the failure event is getting kicked in ledger.
   */
  @Test
  public void runSteelThreadSadPath() {
    SteelThreadSystemCheck test =
        new SteelThreadSystemCheck(client, ledger, "123", failureThresholdForTests);
    when(client.search(Mockito.any()))
        .thenThrow(
            new MrAndersonClient.SearchFailed(
                Query.forType(Medication.class)
                    .parameters(Parameters.forIdentity("x"))
                    .profile(Query.Profile.ARGONAUT)
                    .resource("Medication")
                    .version("999")
                    .build()));
    when(ledger.recordFailure()).thenReturn(failureThresholdForTests);
    test.runSteelThreadCheckAsynchronously();
    verify(ledger, times(1)).recordFailure();
  }
}
