package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.categories.Local;
import lombok.experimental.Delegate;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ProcedureIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  @Ignore
  @Category(Local.class)
  public void advanced() {
    // TODO: Uncomment line(s) below once R4 Procedure exists

    //        verifier.verifyAll(
    //                test(200, Procedure.Bundle.class, "Procedure?_id={id}",
    // verifier.ids().procedure()),
    //                test(404, OperationOutcome.class, "Procedure?_id={id}",
    // verifier.ids().unknown()),
    //                test(200, Procedure.Bundle.class, "Procedure?identifier={id}",
    // verifier.ids().procedure()));
  }

  @Test
  @Ignore
  @Category({
    Local.class,
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void basic() {
    // TODO: Uncomment line(s) below once R4 Procedure exists
    //        verifier.verifyAll(
    //                test(
    //                        200,
    //                        Procedure.Bundle.class,
    //                        "Procedure?patient={patient}&date={onDate}",
    //                        verifier.ids().patient(),
    //                        verifier.ids().procedures().onDate()),
    //                test(
    //                        200,
    //                        Procedure.Bundle.class,
    //                        "Procedure?patient={patient}&date={fromDate}&date={toDate}",
    //                        verifier.ids().patient(),
    //                        verifier.ids().procedures().fromDate(),
    //                        verifier.ids().procedures().toDate()),
    //                test(200, Procedure.class, "Procedure/{id}", verifier.ids().procedure()),
    //                test(404, OperationOutcome.class, "Procedure/{id}", verifier.ids().unknown()),
    //                test(200, Procedure.Bundle.class, "Procedure?patient={patient}",
    // verifier.ids().patient()));
  }

  @Test
  @Ignore
  @Category({
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void searchNotMe() {
    verifier.verifyAll(
        test(403, OperationOutcome.class, "Procedure?patient={patient}", verifier.ids().unknown()));
  }
}
