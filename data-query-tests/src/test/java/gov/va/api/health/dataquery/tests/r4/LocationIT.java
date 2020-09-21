package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.r4.api.resources.Location;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class LocationIT {
  @Delegate private final ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  void advanced() {
    verifyAll(
        // Search by _id
        test(200, Location.Bundle.class, "Location?_id={id}", verifier.ids().location()),
        test(404, OperationOutcome.class, "Location?_id={id}", verifier.ids().unknown()),
        // Search by identifier
        test(200, Location.Bundle.class, "Location?identifier={id}", verifier.ids().location()),
        test(404, OperationOutcome.class, "Location?identifier={id}", verifier.ids().unknown()),
        // Search by name
        test(200, Location.Bundle.class, "Location?name={name}", verifier.ids().locations().name()),
        // Search by address
        test(
            200,
            Location.Bundle.class,
            "Location?address={street}",
            verifier.ids().locations().addressStreet()),
        // Search by address-city
        test(
            200,
            Location.Bundle.class,
            "Location?address-city={city}",
            verifier.ids().locations().addressCity()),
        // Search by address-state
        test(
            200,
            Location.Bundle.class,
            "Location?address-state={state}",
            verifier.ids().locations().addressState()),
        // Search by address-postalcode
        test(
            200,
            Location.Bundle.class,
            "Location?address-postalcode={zip}",
            verifier.ids().locations().addressPostalCode()));
  }

  @Test
  void basic() {
    verifyAll(
        // Read
        test(200, Location.class, "Location/{id}", verifier.ids().location()),
        test(404, OperationOutcome.class, "Location/{id}", verifier.ids().unknown()));
  }
}
