package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.health.api.sentinel.categories.NotInLab;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ProcedureIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            Procedure.Bundle.class,
            "api/Procedure?patient={patient}&date={onDate}",
            verifier.ids().patient(),
            verifier.ids().procedures().onDate()),
        test(
            200,
            Procedure.Bundle.class,
            "api/Procedure?patient={patient}&date={fromDate}&date={toDate}",
            verifier.ids().patient(),
            verifier.ids().procedures().fromDate(),
            verifier.ids().procedures().toDate()),
        test(200, Procedure.Bundle.class, "/api/Procedure?_id={id}", verifier.ids().procedure()),
        test(404, OperationOutcome.class, "/api/Procedure?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Procedure.Bundle.class,
            "/api/Procedure?identifier={id}",
            verifier.ids().procedure()));
  }

  @Test
  @Category({NotInLab.class})
  public void basic() {
    verifier.verifyAll(
        test(200, Procedure.class, "/api/Procedure/{id}", verifier.ids().procedure()),
        test(404, OperationOutcome.class, "/api/Procedure/{id}", verifier.ids().unknown()),
        test(
            200,
            Procedure.Bundle.class,
            "/api/Procedure?patient={patient}",
            verifier.ids().patient()));
  }
}
