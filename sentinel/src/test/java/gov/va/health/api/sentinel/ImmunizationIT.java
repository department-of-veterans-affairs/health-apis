package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.NotInProd;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ImmunizationIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category(NotInProd.class)
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            Immunization.Bundle.class,
            "/api/Immunization?_id={id}",
            verifier.ids().immunization()),
        test(404, OperationOutcome.class, "/api/Immunization?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Immunization.Bundle.class,
            "/api/Immunization?identifier={id}",
            verifier.ids().immunization()));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Immunization.class, "/api/Immunization/{id}", verifier.ids().immunization()),
        test(404, OperationOutcome.class, "/api/Immunization/{id}", verifier.ids().unknown()),
        test(
            200,
            Immunization.Bundle.class,
            "/api/Immunization?patient={patient}",
            verifier.ids().patient()));
  }
}
