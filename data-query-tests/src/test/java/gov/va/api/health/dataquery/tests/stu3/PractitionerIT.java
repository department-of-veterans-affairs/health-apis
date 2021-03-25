package gov.va.api.health.dataquery.tests.stu3;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import gov.va.api.health.stu3.api.resources.Practitioner;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class PractitionerIT {
  @Delegate private final ResourceVerifier verifier = DataQueryResourceVerifier.stu3();

  @Test
  public void advanced() {
    verifyAll(
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?_id={id}",
            DataQueryResourceVerifier.ids().practitioner()),
        test(
            404,
            OperationOutcome.class,
            "Practitioner?_id={id}",
            DataQueryResourceVerifier.ids().unknown()),
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?family={family}&given={given}",
            DataQueryResourceVerifier.ids().practitioners().family(),
            DataQueryResourceVerifier.ids().practitioners().given()),
        test(
            200,
            Practitioner.Bundle.class,
            "Practitioner?identifier={npi}",
            DataQueryResourceVerifier.ids().practitioners().npi()));
  }

  @Test
  public void basic() {
    verifyAll(
        test(
            200,
            Practitioner.class,
            "Practitioner/{id}",
            DataQueryResourceVerifier.ids().practitioner()),
        test(
            404,
            OperationOutcome.class,
            "Practitioner/{id}",
            DataQueryResourceVerifier.ids().unknown()));
  }
}
