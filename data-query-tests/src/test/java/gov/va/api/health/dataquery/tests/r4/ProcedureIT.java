package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.uscorer4.api.resources.Procedure;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class ProcedureIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  public void advanced() {
    assumeEnvironmentIn(Environment.LOCAL);
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
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(403, OperationOutcome.class, "Procedure?patient={patient}", verifier.ids().unknown()));
  }
}
