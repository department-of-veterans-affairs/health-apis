package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.r4.api.resources.Device;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class DeviceIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Device.class, "Device/{id}", verifier.ids().device()),
        test(404, OperationOutcome.class, "Device/{id}", verifier.ids().unknown()),
        test(200, Device.Bundle.class, "Device?patient={patientIcn}", verifier.ids().patient()));
  }

  @Test
  void searchByIdentifier() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(200, Device.Bundle.class, "Device?_id={id}", verifier.ids().device()),
        test(200, Device.Bundle.class, "Device?_id={id}", verifier.ids().unknown()),
        test(200, Device.Bundle.class, "Device?identifier={id}", verifier.ids().device()),
        test(200, Device.Bundle.class, "Device?identifier={id}", verifier.ids().unknown()));
  }

  @Test
  void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(403, OperationOutcome.class, "Device?patient={patient}", verifier.ids().unknown()));
  }
}
