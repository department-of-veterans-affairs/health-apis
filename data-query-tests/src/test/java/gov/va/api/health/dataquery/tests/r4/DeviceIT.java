package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.Device;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class DeviceIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Device.class, "Device/{id}", testIds.device()),
        test(404, OperationOutcome.class, "Device/{id}", testIds.unknown()),
        test(200, Device.Bundle.class, "Device?patient={patientIcn}", testIds.patient()),
        test(
            200,
            Device.Bundle.class,
            "Device?patient={patientIcn}&type=http://snomed.info/sct|53350007",
            testIds.patient()));
  }

  @Test
  void searchByIdentifier() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(200, Device.Bundle.class, "Device?_id={id}", testIds.device()),
        test(
            200,
            Device.Bundle.class,
            d -> d.entry().isEmpty(),
            "Device?_id={id}",
            testIds.unknown()),
        test(200, Device.Bundle.class, "Device?identifier={id}", testIds.device()),
        test(
            200,
            Device.Bundle.class,
            d -> d.entry().isEmpty(),
            "Device?identifier={id}",
            testIds.unknown()));
  }

  @Test
  void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(403, OperationOutcome.class, "Device?patient={patient}", testIds.unknown()));
  }
}
