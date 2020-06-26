package gov.va.api.health.dataquery.tests.stu3;

import static gov.va.api.health.dataquery.tests.TestAssumptionUtility.assumeAllButLocal;
import static gov.va.api.health.dataquery.tests.TestAssumptionUtility.assumeLocal;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import gov.va.api.health.stu3.api.resources.Organization;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class OrganizationIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.stu3();

  @Test
  public void advanced() {
    assumeLocal();
    verifier.verifyAll(
        test(
            200, Organization.Bundle.class, "Organization?_id={id}", verifier.ids().organization()),
        test(404, OperationOutcome.class, "Organization?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?identifier={npi}",
            verifier.ids().organizations().npi()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?name={name}",
            verifier.ids().organizations().name()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?address={street}",
            verifier.ids().organizations().addressStreet()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?address-city={city}",
            verifier.ids().organizations().addressCity()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?address-state={state}",
            verifier.ids().organizations().addressState()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?address-postalcode={zip}",
            verifier.ids().organizations().addressPostalCode()));
  }

  @Test
  public void basic() {
    assumeAllButLocal();
    verifier.verifyAll(
        test(200, Organization.class, "Organization/{id}", verifier.ids().organization()),
        test(404, OperationOutcome.class, "Organization/{id}", verifier.ids().unknown()));
  }
}
