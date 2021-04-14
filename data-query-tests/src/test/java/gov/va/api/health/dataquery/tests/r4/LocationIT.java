package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.Location;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class LocationIT {
  @Delegate private final ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  void advanced() {
    verifyAll(
        // Search by _id
        test(200, Location.Bundle.class, "Location?_id={id}", testIds.location()),
        test(
            200,
            Location.Bundle.class,
            b -> b.entry().isEmpty(),
            "Location?_id={id}",
            testIds.unknown()),
        // Search by identifier
        test(200, Location.Bundle.class, "Location?identifier={id}", testIds.location()),
        test(
            200,
            Location.Bundle.class,
            b -> b.entry().isEmpty(),
            "Location?identifier={id}",
            testIds.unknown()),
        // Search by name
        test(200, Location.Bundle.class, "Location?name={name}", testIds.locations().name()),
        // Search by address
        test(
            200,
            Location.Bundle.class,
            "Location?address={street}",
            testIds.locations().addressStreet()),
        // Search by address-city
        test(
            200,
            Location.Bundle.class,
            "Location?address-city={city}",
            testIds.locations().addressCity()),
        // Search by address-state
        test(
            200,
            Location.Bundle.class,
            "Location?address-state={state}",
            testIds.locations().addressState()),
        // Search by address-postalcode
        test(
            200,
            Location.Bundle.class,
            "Location?address-postalcode={zip}",
            testIds.locations().addressPostalCode()));
  }

  @Test
  void basic() {
    verifyAll(
        // Read
        test(200, Location.class, "Location/{id}", testIds.location()),
        test(404, OperationOutcome.class, "Location/{id}", testIds.unknown()));
  }
}
