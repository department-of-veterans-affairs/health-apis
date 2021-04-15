package gov.va.api.health.dataquery.tests.stu3;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import gov.va.api.health.stu3.api.resources.Organization;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class OrganizationIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.stu3();

  @Test
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            Organization.Bundle.class,
            "Organization?_id={id}",
            DataQueryResourceVerifier.ids().organization()),
        test(
            404,
            OperationOutcome.class,
            "Organization?_id={id}",
            DataQueryResourceVerifier.ids().unknown()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?identifier={npi}",
            DataQueryResourceVerifier.ids().organizations().npi()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?name={name}",
            DataQueryResourceVerifier.ids().organizations().name()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?address={street}",
            DataQueryResourceVerifier.ids().organizations().addressStreet()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?address-city={city}",
            DataQueryResourceVerifier.ids().organizations().addressCity()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?address-state={state}",
            DataQueryResourceVerifier.ids().organizations().addressState()),
        test(
            200,
            Organization.Bundle.class,
            "Organization?address-postalcode={zip}",
            DataQueryResourceVerifier.ids().organizations().addressPostalCode()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            Organization.class,
            "Organization/{id}",
            DataQueryResourceVerifier.ids().organization()),
        test(
            404,
            OperationOutcome.class,
            "Organization/{id}",
            DataQueryResourceVerifier.ids().unknown()));
  }
}
