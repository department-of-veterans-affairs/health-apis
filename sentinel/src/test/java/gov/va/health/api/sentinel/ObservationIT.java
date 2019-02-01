package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.NotInProd;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ObservationIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category(NotInProd.class)
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            Observation.Bundle.class,
            "/api/Observation?patient={patient}&category=laboratory",
            verifier.ids().patient()),
        test(
            200,
            Observation.Bundle.class,
            "/api/Observation?patient={patient}&category=laboratory&date={date}",
            verifier.ids().patient(),
            verifier.ids().observations().onDate()),
        test(
            200,
            Observation.Bundle.class,
            "/api/Observation?patient={patient}&category=laboratory&date={from}&date={to}",
            verifier.ids().patient(),
            verifier.ids().observations().dateRange().from(),
            verifier.ids().observations().dateRange().to()),
        test(
            200,
            Observation.Bundle.class,
            "/api/Observation?patient={patient}&category=vital-signs",
            verifier.ids().patient()),
        test(
            200,
            Observation.Bundle.class,
            "/api/Observation?patient={patient}&category=laboratory,vital-signs",
            verifier.ids().patient()),
        test(
            200,
            Observation.Bundle.class,
            "/api/Observation?patient={patient}&code={loinc1}",
            verifier.ids().patient(),
            verifier.ids().observations().loinc1()),
        test(
            200,
            Observation.Bundle.class,
            "/api/Observation?patient={patient}&code={loinc1},{loinc2}",
            verifier.ids().patient(),
            verifier.ids().observations().loinc1(),
            verifier.ids().observations().loinc2()),
        test(
            200,
            Observation.Bundle.class,
            "/api/Observation?_id={id}",
            verifier.ids().observation()),
        test(404, OperationOutcome.class, "/api/Observation?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Observation.Bundle.class,
            "/api/Observation?identifier={id}",
            verifier.ids().observation()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Observation.class, "/api/Observation/{id}", verifier.ids().observation()),
        test(404, OperationOutcome.class, "/api/Observation/{id}", verifier.ids().unknown()),
        test(
            200,
            Observation.Bundle.class,
            "/api/Observation?patient={patient}",
            verifier.ids().patient()));
  }
}
