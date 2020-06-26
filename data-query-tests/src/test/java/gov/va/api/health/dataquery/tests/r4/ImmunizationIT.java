package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.uscorer4.api.resources.Immunization;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

import static gov.va.api.health.dataquery.tests.TestAssumptionUtility.assumeAllButLocal;
import static gov.va.api.health.dataquery.tests.TestAssumptionUtility.assumeLocal;

public class ImmunizationIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  public void advanced() {
    assumeLocal();
    verifier.verifyAll(
        test(
            200, Immunization.Bundle.class, "Immunization?_id={id}", verifier.ids().immunization()),
        test(404, OperationOutcome.class, "Immunization?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Immunization.Bundle.class,
            "Immunization?identifier={id}",
            verifier.ids().immunization()));
  }

  @Test
  public void basic() {
    assumeAllButLocal();
    verifier.verifyAll(
        test(200, Immunization.class, "Immunization/{id}", verifier.ids().immunization()),
        test(404, OperationOutcome.class, "Immunization/{id}", verifier.ids().unknown()),
        test(
            200,
            Immunization.Bundle.class,
            "Immunization?patient={patient}",
            verifier.ids().patient()));
  }

  @Test
  public void searchNotMe() {
    assumeAllButLocal();
    verifier.verifyAll(
        test(
            403,
            OperationOutcome.class,
            "Immunization?patient={patient}",
            verifier.ids().unknown()));
  }
}
