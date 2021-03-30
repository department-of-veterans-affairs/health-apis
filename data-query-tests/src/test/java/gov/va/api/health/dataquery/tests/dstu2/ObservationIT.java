package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.dstu2.api.resources.Observation;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class ObservationIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.dstu2();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void advanced() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(200, Observation.Bundle.class, "Observation?_id={id}", testIds.observation()),
        test(404, OperationOutcome.class, "Observation?_id={id}", testIds.unknown()),
        test(200, Observation.Bundle.class, "Observation?identifier={id}", testIds.observation()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=laboratory",
            testIds.patient()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=laboratory&date={date}",
            testIds.patient(),
            testIds.observations().onDate()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=laboratory&date={from}&date={to}",
            testIds.patient(),
            testIds.observations().dateRange().from(),
            testIds.observations().dateRange().to()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=vital-signs",
            testIds.patient()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=laboratory,vital-signs",
            testIds.patient()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&code={loinc1}",
            testIds.patient(),
            testIds.observations().loinc1()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&code={loinc1},{loinc2}",
            testIds.patient(),
            testIds.observations().loinc1(),
            testIds.observations().loinc2()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&code={loinc1},{badLoinc}",
            testIds.patient(),
            testIds.observations().loinc1(),
            testIds.observations().badLoinc()),
        test(200, Observation.class, "Observation/{id}", testIds.observation()),
        test(404, OperationOutcome.class, "Observation/{id}", testIds.unknown()),
        test(200, Observation.Bundle.class, "Observation?patient={patient}", testIds.patient()));
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(403, OperationOutcome.class, "Observation?patient={patient}", testIds.unknown()));
  }
}
