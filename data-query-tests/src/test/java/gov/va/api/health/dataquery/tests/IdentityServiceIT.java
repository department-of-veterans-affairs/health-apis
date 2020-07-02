package gov.va.api.health.dataquery.tests;

import static gov.va.api.health.dataquery.tests.EnvironmentAssumptions.assumeNotLocal;

import gov.va.api.health.argonaut.api.resources.Observation;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class IdentityServiceIT {

  @Delegate ResourceVerifier verifier = ResourceVerifier.dstu2();

  @Test
  public void readByUuid() {
    assumeNotLocal();
    verifier.verify(test(200, Observation.class, "Observation/{uuid}", verifier.ids().uuid()));
  }
}
