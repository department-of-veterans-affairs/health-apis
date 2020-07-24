package gov.va.api.health.dataquery.tests;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class IdentityServiceIT {

  @Delegate ResourceVerifier verifier = ResourceVerifier.dstu2();

  @Test
  public void readByUuid() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verify(test(200, Observation.class, "Observation/{uuid}", verifier.ids().uuid()));
  }
}
