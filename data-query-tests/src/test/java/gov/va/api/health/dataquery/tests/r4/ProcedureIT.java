package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Procedure;
import gov.va.api.health.sentinel.Environment;
import java.util.function.Predicate;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class ProcedureIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  private Predicate<Procedure.Bundle> bundleHasResults() {
    return bundle -> !bundle.entry().isEmpty();
  }

  @Test
  public void read() {
    verifyAll(
        test(200, Procedure.class, "Procedure/{id}", testIds.procedure()),
        test(404, OperationOutcome.class, "Procedure/{id}", testIds.unknown()));
  }

  @Test
  public void search() {
    verifyAll(
        test(200, Procedure.Bundle.class, "Procedure?_id={id}", testIds.procedure()),
        test(
            200,
            Procedure.Bundle.class,
            bundleHasResults().negate(),
            "Procedure?_id={id}",
            testIds.unknown()),
        test(
            200,
            Procedure.Bundle.class,
            bundleHasResults(),
            "Procedure?identifier={id}",
            testIds.procedure()),
        test(
            200,
            Procedure.Bundle.class,
            bundleHasResults(),
            "Procedure?patient={patient}&date={onDate}",
            testIds.patient(),
            testIds.procedures().onDate()),
        test(
            200,
            Procedure.Bundle.class,
            bundleHasResults(),
            "Procedure?patient={patient}&date={fromDate}&date={toDate}",
            testIds.patient(),
            testIds.procedures().fromDate(),
            testIds.procedures().toDate()),
        test(
            200,
            Procedure.Bundle.class,
            bundleHasResults(),
            "Procedure?patient={patient}",
            testIds.patient()));
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(403, OperationOutcome.class, "Procedure?patient={patient}", testIds.unknown()));
  }
}
