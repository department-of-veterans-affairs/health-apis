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

  private final TestIds testIds = DataQueryResourceVerifier.ids();

  private static Predicate<Bundle> bundleIsEmpty() {
    return bundle -> bundle.entry().isEmpty();
  }

  private static Predicate<Bundle> bundleIsNotEmpty() {
    return bundleIsEmpty().negate();
  }

  @Test
  void read() {
    verifyAll(
        test(200, Location.class, "Location/{id}", testIds.location()),
        test(404, OperationOutcome.class, "Location/{id}", testIds.unknown()));
  }

  @Test
  void search() {
    verifyAll(
        test(200, Location.Bundle.class, "Location?_id={id}", testIds.location()),
        test(
            200,
            Location.Bundle.class,
            bundleIsEmpty(),
            "Location?_id={unknown}",
            testIds.unknown()),
        test(
            200,
            Location.Bundle.class,
            bundleIsNotEmpty(),
            "Location?organization={organization}",
            testIds.locations().organization()),
        test(
            200,
            Location.Bundle.class,
            bundleIsEmpty(),
            "Location?organization={unknown}",
            testIds.unknown()),
        test(200, Location.Bundle.class, "Location?identifier={id}", testIds.location()),
        test(
            200,
            Location.Bundle.class,
            bundleIsEmpty(),
            "Location?identifier={unknown}",
            testIds.unknown()),
        test(
            200,
            Location.Bundle.class,
            bundleIsNotEmpty(),
            "Location?identifier={clinicId}",
            testIds.locations().clinicIdentifier()),
        test(
            200,
            Location.Bundle.class,
            bundleIsNotEmpty(),
            "Location?identifier=https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-clinic-identifier|{clinicId}",
            testIds.locations().clinicIdentifier()),
        test(
            200,
            Location.Bundle.class,
            bundleIsEmpty(),
            "Location?identifier={unknown}",
            testIds.locations().unknownClinicIdentifier()),
        test(200, Location.Bundle.class, "Location?name={name}", testIds.locations().name()),
        test(
            200,
            Location.Bundle.class,
            "Location?address={street}",
            testIds.locations().addressStreet()),
        test(
            200,
            Location.Bundle.class,
            "Location?address-city={city}",
            testIds.locations().addressCity()),
        test(
            200,
            Location.Bundle.class,
            "Location?address-state={state}",
            testIds.locations().addressState()),
        test(
            200,
            Location.Bundle.class,
            "Location?address-postalcode={zip}",
            testIds.locations().addressPostalCode()));
  }
}
