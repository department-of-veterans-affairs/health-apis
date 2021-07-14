package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Practitioner;
import java.util.function.Predicate;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class PractitionerIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  static Predicate<Practitioner.Bundle> bundleHasResults() {
    return bundle -> !bundle.entry().isEmpty();
  }

  @Test
  void read() {
    verifyAll(
        test(200, Practitioner.class, "Practitioner/{id}", testIds.practitioner()),
        test(200, Practitioner.class, "Practitioner/npi-{npi}", testIds.practitioners().npi()),
        test(404, OperationOutcome.class, "Practitioner/{id}", testIds.unknown()));
  }

  @Test
  void search() {
    verifyAll(
        test(200, Practitioner.Bundle.class, "Practitioner?_id={id}", testIds.practitioner()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults().negate(),
            "Practitioner?_id={id}",
            testIds.unknown()));
  }

  @Test
  void searchByIdentifier() {
    verifyAll(
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults(),
            "Practitioner?identifier={id}",
            testIds.practitioner()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults(),
            "Practitioner?identifier={npi}",
            testIds.practitioners().npi()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults().negate(),
            "Practitioner?identifier={id}",
            testIds.unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults(),
            "Practitioner?identifier=http://hl7.org/fhir/sid/us-npi|{npi}",
            testIds.practitioners().npi()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults().negate(),
            "Practitioner?identifier=http://hl7.org/fhir/sid/us-npi|{id}",
            testIds.practitioner()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults().negate(),
            "Practitioner?identifier=http://hl7.org/fhir/sid/us-npi|{npi}",
            testIds.unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults().negate(),
            "Practitioner?identifier=|{npi}",
            testIds.unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults().negate(),
            "Practitioner?identifier=|{npi}",
            testIds.practitioners().npi()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults().negate(),
            "Practitioner?identifier=http://hl7.org/fhir/sid/us-npi|"));
  }

  @Test
  void searchByName() {
    // Grab subset of family/given names for startsWith tests
    String startFamily = testIds.practitioners().family().substring(0, 3);
    String startGiven = testIds.practitioners().given().substring(0, 3);
    verifyAll(
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults(),
            "Practitioner?given={given}",
            testIds.practitioners().given()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults(),
            "Practitioner?family={family}",
            testIds.practitioners().family()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults(),
            "Practitioner?name={given}",
            testIds.practitioners().given()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults(),
            "Practitioner?name={family}",
            testIds.practitioners().family()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults(),
            "Practitioner?given={given}",
            startGiven),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults(),
            "Practitioner?family={family}",
            startFamily),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults(),
            "Practitioner?name={given}",
            startGiven),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults(),
            "Practitioner?name={family}",
            startFamily),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults().negate(),
            "Practitioner?given={given}",
            testIds.unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults().negate(),
            "Practitioner?family={family}",
            testIds.unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults().negate(),
            "Practitioner?name={given}",
            testIds.unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleHasResults().negate(),
            "Practitioner?name={family}",
            testIds.unknown()));
  }
}
