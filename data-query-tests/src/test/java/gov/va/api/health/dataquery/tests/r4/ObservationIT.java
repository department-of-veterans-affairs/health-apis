package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class ObservationIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  void read() {
    verifyAll(
        test(200, Observation.class, "Observation/{id}", testIds.observation()),
        test(404, OperationOutcome.class, "Observation/{id}", testIds.unknown()));
  }

  @Test
  void search() {
    verifyAll(
        // ID and Identifier
        test(200, Observation.Bundle.class, "Observation?_id={id}", testIds.observation()),
        test(
            200,
            Observation.Bundle.class,
            r -> r.entry().isEmpty(),
            "Observation?_id={id}",
            testIds.unknown()),
        test(200, Observation.Bundle.class, "Observation?identifier={id}", testIds.observation()),
        // Patient And Category
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=laboratory",
            testIds.patient()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=http://terminology.hl7.org/CodeSystem/observation-category|laboratory&date={date}",
            testIds.patient(),
            testIds.observations().onDate()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=laboratory,vital-signs&date={from}&date={to}",
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
            "Observation?patient={patient}&category=http://terminology.hl7.org/CodeSystem/observation-category|",
            testIds.patient()),
        // Patient And Code
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&code={loinc1}",
            testIds.patient(),
            testIds.observations().loinc1()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&code=http://loinc.org|",
            testIds.patient()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&code={loinc1}&date={date}",
            testIds.patient(),
            testIds.observations().loinc1(),
            testIds.observations().onDate()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&code={loinc1}&date={from}&date={to}",
            testIds.patient(),
            testIds.observations().loinc1(),
            testIds.observations().dateRange().from(),
            testIds.observations().dateRange().to()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&code=http://loinc.org|{loinc1},{loinc2}",
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
        // Patient Icn
        test(200, Observation.Bundle.class, "Observation?patient={patient}", testIds.patient()));
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifyAll(
        test(403, OperationOutcome.class, "Observation?patient={patient}", testIds.unknown()));
  }
}
