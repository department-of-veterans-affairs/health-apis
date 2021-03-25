package gov.va.api.health.dataquery.tests.dstu2;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.dstu2.api.resources.Location;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class LocationIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.dstu2();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void advanced() {
    verifier.verifyAll(
        test(200, Location.Bundle.class, "Location?_id={id}", testIds.location()),
        test(404, OperationOutcome.class, "Location?_id={id}", testIds.unknown()),
        test(200, Location.Bundle.class, "Location?identifier={id}", testIds.location()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Location.class, "Location/{id}", testIds.location()),
        test(404, OperationOutcome.class, "Location/{id}", testIds.unknown()));
  }
}
