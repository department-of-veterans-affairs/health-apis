package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.LabArgo;
import gov.va.health.api.sentinel.categories.LabCargo;
import gov.va.health.api.sentinel.categories.Local;
import gov.va.health.api.sentinel.categories.ProdArgo;
import gov.va.health.api.sentinel.categories.ProdCargo;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ImmunizationIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({Local.class, LabCargo.class, ProdCargo.class})
  public void advanced() {
    verifier.verifyAll(
        test(
            200, Immunization.Bundle.class, "Immunization?_id={id}", verifier.ids().immunization()),
        test(404, OperationOutcome.class, "Immunization?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Immunization.Bundle.class,
            "Immunization?identifier={id}",
            verifier.ids().immunization()));
  }

  @Test
  @Category({Local.class, LabArgo.class, LabCargo.class, ProdArgo.class, ProdCargo.class})
  public void basic() {
    verifier.verifyAll(
        test(200, Immunization.class, "Immunization/{id}", verifier.ids().immunization()),
        test(404, OperationOutcome.class, "Immunization/{id}", verifier.ids().unknown()),
        test(
            200,
            Immunization.Bundle.class,
            "Immunization?patient={patient}",
            verifier.ids().patient()));
  }

  @Test
  @Category({LabArgo.class, LabCargo.class, ProdArgo.class, ProdCargo.class})
  public void searchNotMe() {
    verifier.verifyAll(
        test(
            403,
            OperationOutcome.class,
            "Immunization?patient={patient}",
            verifier.ids().unknown()));
  }
}
