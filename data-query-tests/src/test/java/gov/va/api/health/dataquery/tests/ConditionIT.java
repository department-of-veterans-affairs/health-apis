package gov.va.api.health.dataquery.tests;

import gov.va.api.health.dataquery.api.resources.Condition;
import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.categories.Local;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ConditionIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({Local.class, LabDataQueryClinician.class, ProdDataQueryClinician.class})
  public void advanced() {
    verifier.verifyAll(
        ResourceVerifier.test(
            200, Condition.Bundle.class, "Condition?_id={id}", verifier.ids().condition()),
        ResourceVerifier.test(
            404, OperationOutcome.class, "Condition?_id={id}", verifier.ids().unknown()),
        ResourceVerifier.test(
            200, Condition.Bundle.class, "Condition?identifier={id}", verifier.ids().condition()));
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
        ResourceVerifier.test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&category=problem",
            verifier.ids().patient()),
        ResourceVerifier.test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&category=health-concern",
            verifier.ids().patient()),
        ResourceVerifier.test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&clinicalstatus=active",
            verifier.ids().patient()),
        ResourceVerifier.test(
            200,
            Condition.Bundle.class,
            "Condition?patient={patient}&clinicalstatus=active,resolved",
            verifier.ids().patient()),
        ResourceVerifier.test(200, Condition.class, "Condition/{id}", verifier.ids().condition()),
        ResourceVerifier.test(
            404, OperationOutcome.class, "Condition/{id}", verifier.ids().unknown()),
        ResourceVerifier.test(
            200, Condition.Bundle.class, "Condition?patient={patient}", verifier.ids().patient()));
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
        ResourceVerifier.test(
            403, OperationOutcome.class, "Condition?patient={patient}", verifier.ids().unknown()));
  }
}
