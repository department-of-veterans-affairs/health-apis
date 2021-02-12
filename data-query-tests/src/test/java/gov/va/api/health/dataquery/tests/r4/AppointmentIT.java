package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.Environment.LAB;
import static gov.va.api.health.sentinel.Environment.LOCAL;
import static gov.va.api.health.sentinel.Environment.STAGING_LAB;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import java.time.Year;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class AppointmentIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  public void advanced() {
    assumeEnvironmentIn(STAGING_LAB, LAB, LOCAL);
    verifier.verifyAll(
        test(200, Appointment.Bundle.class, "Appointment?_id={id}", verifier.ids().appointment()),
        test(
            200,
            Appointment.Bundle.class,
            r -> r.entry().isEmpty(),
            "Appointment?_id={id}",
            verifier.ids().unknown()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?identifier={id}",
            verifier.ids().appointment()));
  }

  @Test
  public void basic() {
    assumeEnvironmentIn(LOCAL, STAGING_LAB, LAB);
    verifier.verifyAll(
        test(200, Appointment.class, "Appointment/{id}", verifier.ids().appointment()),
        test(404, OperationOutcome.class, "Appointment/{id}", verifier.ids().unknown()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?patient={patient}",
            verifier.ids().patient()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?patient={patient}&location={location}",
            verifier.ids().patient(),
            verifier.ids().appointments().location()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?patient={patient}&location={location}&_lastUpdated={lastUpdated}",
            verifier.ids().patient(),
            verifier.ids().appointments().location(),
            verifier.ids().appointments().lastUpdated()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?patient={patient}&_lastUpdated={lastUpdated}",
            verifier.ids().patient(),
            verifier.ids().appointments().lastUpdated()));
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(LOCAL);
    verifier.verifyAll(
        test(
            403,
            OperationOutcome.class,
            "Appointment?patient={patient}",
            verifier.ids().unknown()));
  }

  @Test
  void systemScopes() {
    assumeEnvironmentIn(LOCAL);
    verifier.verifyAll(
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?location={location}",
            verifier.ids().appointments().location()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?_lastUpdated={lastUpdated}",
            verifier.ids().appointments().lastUpdated()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?location={location}&_lastUpdated={lastUpdated}",
            verifier.ids().appointments().location(),
            verifier.ids().appointments().lastUpdated()),
        test(
            200,
            Appointment.Bundle.class,
            r -> r.entry().isEmpty(),
            "Appointment?_lastUpdated=gt" + Year.now().plusYears(1).toString()));
  }
}
