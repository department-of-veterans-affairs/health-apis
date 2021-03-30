package gov.va.api.health.dataquery.tests.stu3;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.stu3.api.resources.Location;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class LocationIT {
  @Delegate private final ResourceVerifier verifier = DataQueryResourceVerifier.stu3();

  @Test
  public void advanced() {
    verifyAll(
        // Search By _id
        test(
            200,
            Location.Bundle.class,
            "Location?_id={id}",
            DataQueryResourceVerifier.ids().location()),
        test(
            404,
            OperationOutcome.class,
            "Location?_id={id}",
            DataQueryResourceVerifier.ids().unknown()),
        // Search By identifier
        test(
            200,
            Location.Bundle.class,
            "Location?identifier={id}",
            DataQueryResourceVerifier.ids().location()),
        test(
            404,
            OperationOutcome.class,
            "Location?identifier={id}",
            DataQueryResourceVerifier.ids().unknown()),
        // Search By Location Name
        test(
            200,
            Location.Bundle.class,
            "Location?name={name}",
            DataQueryResourceVerifier.ids().locations().name()),
        // Search By Location Street
        test(
            200,
            Location.Bundle.class,
            "Location?address={street}",
            DataQueryResourceVerifier.ids().locations().addressStreet()),
        // Search By City
        test(
            200,
            Location.Bundle.class,
            "Location?address-city={city}",
            DataQueryResourceVerifier.ids().locations().addressCity()),
        // Search By State
        test(
            200,
            Location.Bundle.class,
            "Location?address-state={state}",
            DataQueryResourceVerifier.ids().locations().addressState()),
        // Search By Postal Code
        test(
            200,
            Location.Bundle.class,
            "Location?address-postalcode={zip}",
            DataQueryResourceVerifier.ids().locations().addressPostalCode()));
  }

  @Test
  public void basic() {
    verifyAll(
        test(200, Location.class, "Location/{id}", DataQueryResourceVerifier.ids().location()),
        test(
            404,
            OperationOutcome.class,
            "Location/{id}",
            DataQueryResourceVerifier.ids().unknown()));
  }
}
