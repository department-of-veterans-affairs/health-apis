package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Practitioner;
import gov.va.api.health.sentinel.Environment;
import java.util.function.Predicate;
import lombok.experimental.Delegate;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class PractitionerIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  private Predicate<Practitioner.Bundle> bundleIsEmpty() {
    return bundle -> bundle.entry().isEmpty();
  }

  private Predicate<Practitioner.Bundle> bundleIsNotEmpty() {
    return bundle -> !bundle.entry().isEmpty();
  }

  @Test
  public void read() {
    verifyAll(
        test(200, Practitioner.class, "Practitioner/{id}", testIds.practitioner()),
        test(200, Practitioner.class, "Practitioner/npi-{npi}", testIds.practitioners().npi()),
        test(404, OperationOutcome.class, "Practitioner/{id}", testIds.unknown()));
  }

  @Test
  public void search() {
    verifyAll(
        test(200, Practitioner.Bundle.class, "Practitioner?_id={id}", testIds.practitioner()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsEmpty(),
            "Practitioner?_id={id}",
            testIds.unknown()));
  }

  @Test
  public void searchByIdentifier() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifyAll(
        test(
            200, Practitioner.Bundle.class, "Practitioner?identifier={id}", testIds.practitioner()),
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?identifier={npi}",
            testIds.practitioners().npi()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsEmpty(),
            "Practitioner?identifier={id}",
            testIds.unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?identifier=http://hl7.org/fhir/sid/us-npi|{npi}",
            testIds.practitioners().npi()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsEmpty(),
            "Practitioner?identifier=http://hl7.org/fhir/sid/us-npi|{id}",
            testIds.practitioner()),
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?identifier=http://hl7.org/fhir/sid/us-npi|{npi}",
            testIds.unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?identifier=http://hl7.org/fhir/sid/us-npi|"),
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsEmpty(),
            "Practitioner?identifier=|{npi}",
            testIds.unknown()));
  }

  @Test
  public void searchByName() {
    // Grab subset of family/given names for startsWith tests
    String startFamily = StringUtils.substring(testIds.practitioners().family(), 0, 3);
    String startGiven = StringUtils.substring(testIds.practitioners().given(), 0, 3);
    verifyAll(
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsNotEmpty(),
            "Practitioner?given={given}",
            testIds.practitioners().given()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsNotEmpty(),
            "Practitioner?family={family}",
            testIds.practitioners().family()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsNotEmpty(),
            "Practitioner?name={given}",
            testIds.practitioners().given()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsNotEmpty(),
            "Practitioner?name={family}",
            testIds.practitioners().family()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsNotEmpty(),
            "Practitioner?given={given}",
            startGiven),
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsNotEmpty(),
            "Practitioner?family={family}",
            startFamily),
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsNotEmpty(),
            "Practitioner?name={given}",
            startGiven),
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsNotEmpty(),
            "Practitioner?name={family}",
            startFamily),
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsEmpty(),
            "Practitioner?given={given}",
            testIds.unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsEmpty(),
            "Practitioner?family={family}",
            testIds.unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsEmpty(),
            "Practitioner?name={given}",
            testIds.unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            bundleIsEmpty(),
            "Practitioner?name={family}",
            testIds.unknown()));
  }
}
