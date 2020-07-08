package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.dataquery.tests.EnvironmentAssumptions.assumeLocal;
import static gov.va.api.health.dataquery.tests.EnvironmentAssumptions.assumeNotLocal;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.uscorer4.api.resources.Observation;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class ObservationIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  public void advanced() {
    assumeLocal();
    verifier.verifyAll(
        test(200, Observation.Bundle.class, "Observation?_id={id}", verifier.ids().observation()),
        test(404, OperationOutcome.class, "Observation?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?identifier={id}",
            verifier.ids().observation()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        // Patient And Category
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=laboratory",
            verifier.ids().patient()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=http://terminology.hl7.org/CodeSystem/observation-category|laboratory&date={date}",
            verifier.ids().patient(),
            verifier.ids().observations().onDate()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=laboratory,vital-signs&date={from}&date={to}",
            verifier.ids().patient(),
            verifier.ids().observations().dateRange().from(),
            verifier.ids().observations().dateRange().to()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=vital-signs",
            verifier.ids().patient()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=http://terminology.hl7.org/CodeSystem/observation-category|",
            verifier.ids().patient()),
        // Patient And Code
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&code={loinc1}",
            verifier.ids().patient(),
            verifier.ids().observations().loinc1()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&code={loinc1}&date={date}",
            verifier.ids().patient(),
            verifier.ids().observations().loinc1(),
            verifier.ids().observations().onDate()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&code={loinc1}&date={from}&date={to}",
            verifier.ids().patient(),
            verifier.ids().observations().loinc1(),
            verifier.ids().observations().dateRange().from(),
            verifier.ids().observations().dateRange().to()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&code={loinc1},{loinc2}",
            verifier.ids().patient(),
            verifier.ids().observations().loinc1(),
            verifier.ids().observations().loinc2()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&code={loinc1},{badLoinc}",
            verifier.ids().patient(),
            verifier.ids().observations().loinc1(),
            verifier.ids().observations().badLoinc()),
        // Observation Public Id
        test(200, Observation.class, "Observation/{id}", verifier.ids().observation()),
        test(404, OperationOutcome.class, "Observation/{id}", verifier.ids().unknown()),
        // Patient Icn
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}",
            verifier.ids().patient()));
  }

  @Test
  public void searchNotMe() {
    assumeNotLocal();
    verifier.verifyAll(
        test(
            403,
            OperationOutcome.class,
            "Observation?patient={patient}",
            verifier.ids().unknown()));
  }
}
