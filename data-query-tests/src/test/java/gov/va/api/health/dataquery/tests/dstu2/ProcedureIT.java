package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.dstu2.api.resources.Procedure;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class ProcedureIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.dstu2();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void advanced() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(200, Procedure.Bundle.class, "Procedure?_id={id}", testIds.procedure()),
        test(404, OperationOutcome.class, "Procedure?_id={id}", testIds.unknown()),
        test(200, Procedure.Bundle.class, "Procedure?identifier={id}", testIds.procedure()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            Procedure.Bundle.class,
            "Procedure?patient={patient}&date={onDate}",
            testIds.patient(),
            testIds.procedures().onDate()),
        test(
            200,
            Procedure.Bundle.class,
            "Procedure?patient={patient}&date={fromDate}&date={toDate}",
            testIds.patient(),
            testIds.procedures().fromDate(),
            testIds.procedures().toDate()),
        test(200, Procedure.class, "Procedure/{id}", testIds.procedure()),
        test(404, OperationOutcome.class, "Procedure/{id}", testIds.unknown()),
        test(200, Procedure.Bundle.class, "Procedure?patient={patient}", testIds.patient()));
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(403, OperationOutcome.class, "Procedure?patient={patient}", testIds.unknown()));
  }
}
