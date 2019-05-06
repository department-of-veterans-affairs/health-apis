package gov.va.api.health.sentinel;

import static gov.va.api.health.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.categories.LabDataQueryClinician;
import gov.va.api.health.sentinel.categories.LabDataQueryPatient;
import gov.va.api.health.sentinel.categories.Local;
import gov.va.api.health.sentinel.categories.ProdDataQueryClinician;
import gov.va.api.health.sentinel.categories.ProdDataQueryPatient;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ConditionIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({Local.class, LabDataQueryClinician.class, ProdDataQueryClinician.class})
  public void advanced() {
    verifier.verifyAll(
        test(200, Condition.Bundle.class, "Condition?_id={id}", verifier.ids().condition()),
        test(404, OperationOutcome.class, "Condition?_id={id}", verifier.ids().unknown()),
        test(200, Condition.Bundle.class, "Condition?identifier={id}", verifier.ids().condition()));
  }

  @Test
  @Category({
    Local.class,
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&category=problem",
            verifier.ids().patient()),
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&category=health-concern",
            verifier.ids().patient()),
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&clinicalstatus=active",
            verifier.ids().patient()),
        test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&clinicalstatus=active,resolved",
            verifier.ids().patient()),
        test(200, Condition.class, "Condition/{id}", verifier.ids().condition()),
        test(404, OperationOutcome.class, "Condition/{id}", verifier.ids().unknown()),
        test(200, Condition.Bundle.class, "Condition?patient={patient}", verifier.ids().patient()));
  }

  @Test
  @Category({
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void searchNotMe() {
    verifier.verifyAll(
        test(403, OperationOutcome.class, "Condition?patient={patient}", verifier.ids().unknown()));
  }
}
