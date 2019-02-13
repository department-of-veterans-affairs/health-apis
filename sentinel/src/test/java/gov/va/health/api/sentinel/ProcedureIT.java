package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.health.api.sentinel.categories.LabArgo;
import gov.va.health.api.sentinel.categories.LabCargo;
import gov.va.health.api.sentinel.categories.Local;
import gov.va.health.api.sentinel.categories.ProdArgo;
import gov.va.health.api.sentinel.categories.ProdCargo;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ProcedureIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category(Local.class)
  public void advanced() {
    verifier.verifyAll(
        test(200, Procedure.Bundle.class, "Procedure?_id={id}", verifier.ids().procedure()),
        test(404, OperationOutcome.class, "Procedure?_id={id}", verifier.ids().unknown()),
        test(200, Procedure.Bundle.class, "Procedure?identifier={id}", verifier.ids().procedure()));
  }

  @Test
  @Category({Local.class, LabArgo.class, LabCargo.class, ProdArgo.class, ProdCargo.class})
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            Procedure.Bundle.class,
            "Procedure?patient={patient}&date={onDate}",
            verifier.ids().patient(),
            verifier.ids().procedures().onDate()),
        test(
            200,
            Procedure.Bundle.class,
            "Procedure?patient={patient}&date={fromDate}&date={toDate}",
            verifier.ids().patient(),
            verifier.ids().procedures().fromDate(),
            verifier.ids().procedures().toDate()),
        test(200, Procedure.class, "Procedure/{id}", verifier.ids().procedure()),
        test(404, OperationOutcome.class, "Procedure/{id}", verifier.ids().unknown()),
        test(200, Procedure.Bundle.class, "Procedure?patient={patient}", verifier.ids().patient()));
  }

  @Test
  @Category({LabArgo.class, LabCargo.class, ProdArgo.class, ProdCargo.class})
  public void searchNotMe() {
    verifier.verifyAll(
        test(403, OperationOutcome.class, "Procedure?patient={patient}", verifier.ids().unknown()));
  }
}
