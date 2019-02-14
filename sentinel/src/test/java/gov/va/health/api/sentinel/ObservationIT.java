package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.LabArgo;
import gov.va.health.api.sentinel.categories.LabCargo;
import gov.va.health.api.sentinel.categories.Local;
import gov.va.health.api.sentinel.categories.ProdArgo;
import gov.va.health.api.sentinel.categories.ProdCargo;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ObservationIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({Local.class, LabCargo.class, ProdCargo.class})
  public void advanced() {
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
  @Category({Local.class, LabArgo.class, LabCargo.class, ProdArgo.class, ProdCargo.class})
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=laboratory",
            verifier.ids().patient()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=laboratory&date={date}",
            verifier.ids().patient(),
            verifier.ids().observations().onDate()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&category=laboratory&date={from}&date={to}",
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
            "Observation?patient={patient}&category=laboratory,vital-signs",
            verifier.ids().patient()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}&code={loinc1}",
            verifier.ids().patient(),
            verifier.ids().observations().loinc1()),
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
        test(200, Observation.class, "Observation/{id}", verifier.ids().observation()),
        test(404, OperationOutcome.class, "Observation/{id}", verifier.ids().unknown()),
        test(
            200,
            Observation.Bundle.class,
            "Observation?patient={patient}",
            verifier.ids().patient()));
  }

  @Test
  @Category({LabArgo.class, LabCargo.class, ProdArgo.class, ProdCargo.class})
  public void searchNotMe() {
    verifier.verifyAll(
        test(
            403,
            OperationOutcome.class,
            "Observation?patient={patient}",
            verifier.ids().unknown()));
  }
}
