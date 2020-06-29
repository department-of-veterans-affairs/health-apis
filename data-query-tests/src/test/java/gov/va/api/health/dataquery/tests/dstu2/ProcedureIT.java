package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.dataquery.tests.EnvironmentAssumptions.assumeLocal;
import static gov.va.api.health.dataquery.tests.EnvironmentAssumptions.assumeNotLocal;

import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class ProcedureIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.dstu2();

  @Test
  public void advanced() {
    assumeLocal();
    verifier.verifyAll(
        test(200, Procedure.Bundle.class, "Procedure?_id={id}", verifier.ids().procedure()),
        test(404, OperationOutcome.class, "Procedure?_id={id}", verifier.ids().unknown()),
        test(200, Procedure.Bundle.class, "Procedure?identifier={id}", verifier.ids().procedure()));
  }

  @Test
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
  public void searchNotMe() {
    assumeNotLocal();
    verifier.verifyAll(
        test(403, OperationOutcome.class, "Procedure?patient={patient}", verifier.ids().unknown()));
  }
}
