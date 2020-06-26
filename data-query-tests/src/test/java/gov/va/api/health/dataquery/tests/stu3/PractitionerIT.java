package gov.va.api.health.dataquery.tests.stu3;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import gov.va.api.health.stu3.api.resources.Practitioner;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static gov.va.api.health.dataquery.tests.TestAssumptionUtility.assumeAllButLocal;
import static gov.va.api.health.dataquery.tests.TestAssumptionUtility.assumeLocal;

public class PractitionerIT {
  @Delegate private final ResourceVerifier verifier = ResourceVerifier.stu3();

  @Test
  public void advanced() {
    assumeLocal();
    verifyAll(
        test(
            200, Practitioner.Bundle.class, "Practitioner?_id={id}", verifier.ids().practitioner()),
        test(404, OperationOutcome.class, "Practitioner?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?family={family}&given={given}",
            verifier.ids().practitioners().family(),
            verifier.ids().practitioners().given()),
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?identifier={npi}",
            verifier.ids().practitioners().npi()));
  }

  @Test
  public void basic() {
    assumeAllButLocal();
    verifier.verifyAll(
        test(200, Practitioner.class, "Practitioner/{id}", verifier.ids().practitioner()),
        test(404, OperationOutcome.class, "Practitioner/{id}", verifier.ids().unknown()));
  }
}
