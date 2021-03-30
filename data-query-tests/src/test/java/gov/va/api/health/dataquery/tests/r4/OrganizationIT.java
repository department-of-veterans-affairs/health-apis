package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Organization;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class OrganizationIT {
  @Delegate private final ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  void advanced() {
    verifyAll( // Search by _id
        test(200, Organization.Bundle.class, "Organization?_id={id}", testIds.organization()),
        test(404, OperationOutcome.class, "Organization?_id={id}", testIds.unknown()),
        test(
            200, Organization.Bundle.class, "Organization?identifier={id}", testIds.organization()),
        test(404, OperationOutcome.class, "Organization?identifier={id}", testIds.unknown()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?name={name}",
            testIds.organizations().name()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?address={street}",
            testIds.organizations().addressStreet()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?address-city={city}",
            testIds.organizations().addressCity()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?address-state={state}",
            testIds.organizations().addressState()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?address-postalcode={zip}",
            testIds.organizations().addressPostalCode()));
  }

  @Test
  void basic() {
    verifyAll( // Read
        test(200, Organization.class, "Organization/{id}", testIds.organization()),
        test(404, OperationOutcome.class, "Organization/{id}", testIds.unknown()));
  }
}
