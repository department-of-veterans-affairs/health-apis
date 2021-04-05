package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Organization;
import java.util.function.Predicate;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class OrganizationIT {
  @Delegate private final ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  void advanced() {
    verifyAll(
        test(
            200,
            Organization.Bundle.class,
            bundleIsNotEmpty(),
            "Organization?_id={id}",
            testIds.organization()),
        test(
            200,
            Organization.Bundle.class,
            bundleIsNotEmpty().negate(),
            "Organization?_id={id}",
            testIds.unknown()),
        test(
            200,
            Organization.Bundle.class,
            bundleIsNotEmpty(),
            "Organization?identifier={id}",
            testIds.organization()),
        test(
            200,
            Organization.Bundle.class,
            bundleIsNotEmpty(),
            "Organization?name={name}",
            testIds.organizations().name()),
        test(
            200,
            Organization.Bundle.class,
            bundleIsNotEmpty(),
            "Organization?address={street}",
            testIds.organizations().addressStreet()),
        test(
            200,
            Organization.Bundle.class,
            bundleIsNotEmpty(),
            "Organization?address-city={city}",
            testIds.organizations().addressCity()),
        test(
            200,
            Organization.Bundle.class,
            bundleIsNotEmpty(),
            "Organization?address-state={state}",
            testIds.organizations().addressState()),
        test(
            200,
            Organization.Bundle.class,
            bundleIsNotEmpty(),
            "Organization?address-postalcode={zip}",
            testIds.organizations().addressPostalCode()));
  }

  @Test
  void basic() {
    verifyAll(
        test(200, Organization.class, "Organization/{id}", testIds.organization()),
        test(404, OperationOutcome.class, "Organization/{id}", testIds.unknown()));
  }

  private Predicate<Organization.Bundle> bundleIsNotEmpty() {
    return bundle -> !bundle.entry().isEmpty();
  }
}
