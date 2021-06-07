package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Practitioner;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class PractitionerIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

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
            p -> p.entry().isEmpty(),
            "Practitioner?_id={id}",
            testIds.unknown()),
        // any system with valid id
        test(
            200, Practitioner.Bundle.class, "Practitioner?identifier={id}", testIds.practitioner()),
        // any system with valid npi
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?identifier={npi}",
            testIds.practitioners().npi()),
        // any system with unknown id
        test(
            200,
            Practitioner.Bundle.class,
            p -> p.entry().isEmpty(),
            "Practitioner?identifier={id}",
            testIds.unknown()),
        // npi system with valid npi
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?identifier=http://hl7.org/fhir/sid/us-npi|{npi}",
            testIds.practitioners().npi()),
        // npi system with valid I2
        test(
            200,
            Practitioner.Bundle.class,
            p -> p.entry().isEmpty(),
            "Practitioner?identifier=http://hl7.org/fhir/sid/us-npi|{id}",
            testIds.practitioner()),
        // npi system with unknown value
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?identifier=http://hl7.org/fhir/sid/us-npi|{npi}",
            testIds.unknown()),
        // npi system with any code
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?identifier=http://hl7.org/fhir/sid/us-npi|"),
        // empty system with valid npi
        test(
            200,
            Practitioner.Bundle.class,
            p -> p.entry().isEmpty(),
            "Practitioner?identifier=|{npi}",
            testIds.unknown()));
  }
}
