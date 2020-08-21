package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.uscorer4.api.resources.Organization;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class OrganizationIT {
  @Delegate private final ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  void advanced() {
    verifyAll(
        // Search by _id
        test(
            200, Organization.Bundle.class, "Organization?_id={id}", verifier.ids().organization()),
        test(404, OperationOutcome.class, "Organization?_id={id}", verifier.ids().unknown()),
        // Search by identifier
        test(
            200,
            Organization.Bundle.class,
            "Organization?identifier={id}",
            verifier.ids().organization()),
        test(404, OperationOutcome.class, "Organization?identifier={id}", verifier.ids().unknown()),
        // Search by name
        test(
            200,
            Organization.Bundle.class,
            "Organization?name={name}",
            verifier.ids().organizations().name()),
        // Search by address
        test(
            200,
            Organization.Bundle.class,
            "Organizations?address={street}",
            verifier.ids().organizations().addressStreet()),
        // Search by address-city
        test(
            200,
            Organization.Bundle.class,
            "Organization?address-city={city}",
            verifier.ids().organizations().addressCity()),
        // Search by address-state
        test(
            200,
            Organization.Bundle.class,
            "Organization?address-state={state}",
            verifier.ids().organizations().addressState()),
        // Search by address-postalcode
        test(
            200,
            Organization.Bundle.class,
            "Organization?address-postalcode={zip}",
            verifier.ids().organizations().addressPostalCode()));
  }

  @Test
  void basic() {
    verifyAll(
        // Read
        test(200, Organization.class, "Organization/{id}", verifier.ids().organization()),
        test(404, OperationOutcome.class, "Organization/{id}", verifier.ids().unknown()));
  }
}
