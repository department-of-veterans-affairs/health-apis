package gov.va.api.health.dataquery.tests.r4;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.categories.Local;
import gov.va.api.health.uscorer4.api.resources.Procedure;
import java.util.Map;
import lombok.experimental.Delegate;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ProcedureIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  @Category(Local.class)
  public void advanced() {
    verifier.verifyAll(
        test(200, Procedure.Bundle.class, "Procedure?_id={id}", verifier.ids().procedure()),
        test(404, OperationOutcome.class, "Procedure?_id={id}", verifier.ids().unknown()),
        test(200, Procedure.Bundle.class, "Procedure?identifier={id}", verifier.ids().procedure()));
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
  @Category({LabDataQueryPatient.class, ProdDataQueryPatient.class})
  public void postSearch() {
    verifier.verifyAll(
        test(
            200,
            Procedure.Bundle.class,
            "Procedure/_search",
            Map.of("Content-Type", "application/x-www-form-urlencoded"),
            "patient={patient}",
            verifier.ids().patient()),
        test(
            200,
            Procedure.Bundle.class,
            "Procedure/_search",
            Map.of("Content-Type", "application/x-www-form-urlencoded"),
            "patient={patient}&date={onDate}",
            verifier.ids().patient(),
            verifier.ids().procedures().onDate()),
        test(
            200,
            Procedure.Bundle.class,
            "Procedure/_search",
            Map.of("Content-Type", "application/x-www-form-urlencoded"),
            "patient={patient}&date={fromDate}&date={toDate}",
            verifier.ids().patient(),
            verifier.ids().procedures().fromDate(),
            verifier.ids().procedures().toDate()));
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
        test(403, OperationOutcome.class, "Procedure?patient={patient}", verifier.ids().unknown()));
  }
}
