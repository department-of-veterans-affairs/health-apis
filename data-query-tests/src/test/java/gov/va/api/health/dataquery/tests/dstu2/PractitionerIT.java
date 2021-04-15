package gov.va.api.health.dataquery.tests.dstu2;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class PractitionerIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.dstu2();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void advanced() {
    verifier.verifyAll(
        test(200, Practitioner.Bundle.class, "Practitioner?_id={id}", testIds.practitioner()),
        test(404, OperationOutcome.class, "Practitioner?_id={id}", testIds.unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?identifier={id}",
            testIds.practitioner()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Practitioner.class, "Practitioner/{id}", testIds.practitioner()),
        test(404, OperationOutcome.class, "Practitioner/{id}", testIds.unknown()));
  }
}
