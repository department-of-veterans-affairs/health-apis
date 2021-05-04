package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.Location;
import gov.va.api.health.r4.api.resources.Location.Bundle;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import java.util.function.Predicate;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class LocationIT {
  @Delegate private final ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  private Predicate<Bundle> bundleIsNotEmpty() {
    return bundle -> !bundle.entry().isEmpty();
  }

  @Test
  void read() {
    verifyAll(
        // Read
        test(200, Location.class, "Location/{id}", testIds.location()),
        test(404, OperationOutcome.class, "Location/{id}", testIds.unknown()));
  }

  @Test
  void search() {
    verifyAll(
        // Search by _id
        test(200, Location.Bundle.class, "Location?_id={id}", testIds.location()),
        test(
            200,
            Location.Bundle.class,
            bundleIsNotEmpty().negate(),
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
        test(
            200,
            Location.Bundle.class,
            b -> !b.entry().isEmpty(),
            "Location?identifier={identifier}",
            testIds.locations().clinicIdentifier()),
        test(
            200,
            Location.Bundle.class,
            b -> !b.entry().isEmpty(),
            "Location?identifier=https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-clinic-identifier|{identifier}",
            testIds.locations().clinicIdentifier()),
        test(
            200,
            Location.Bundle.class,
            b -> b.entry().isEmpty(),
            "Location?identifier={identifier}",
            testIds.locations().unknownClinicIdentifier()),
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
}
