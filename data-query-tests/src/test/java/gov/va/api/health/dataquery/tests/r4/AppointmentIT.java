package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.Environment.LAB;
import static gov.va.api.health.sentinel.Environment.LOCAL;
import static gov.va.api.health.sentinel.Environment.STAGING_LAB;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;
import static org.junit.jupiter.api.Assertions.fail;

import gov.va.api.health.dataquery.tests.Oauth;
import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dataquery.tests.SystemDefinitions;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import java.time.Year;
import java.util.List;

import gov.va.api.health.sentinel.*;
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

  /**
   * Test searching that would typically requires a token. We don't have a kong locally here, so we
   * can ignore the token.
   */
  @Test
  void systemScopesLocal() {
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

  /**
   * Test that the oauth flow returns a system scope, and that the scope passes kong's validation.
   */
  @Test
  void oauthFlow() {
    assumeEnvironmentIn(Environment.STAGING_LAB, Environment.LAB);
    String token = Oauth.exchangeToken(List.of("system/Appointment.read"));
    var sd = SystemDefinitions.systemDefinition().r4DataQuery();
    // Valid Token
    Oauth.test(
        sd,
        200,
        Appointment.Bundle.class,
        token,
        "Appointment?patient={icn}",
        verifier.ids().appointments().oauthPatient());
    // Invalid Token
    Oauth.test(
        sd,
        401,
        OperationOutcome.class,
        "NOPE",
        "Appointment?patient={icn}",
        verifier.ids().appointments().oauthPatient());
    // Invalid Resource for Scopes
    Oauth.test(
        sd,
        403,
        OperationOutcome.class,
        token,
        "Observation?patient={icn}",
        verifier.ids().appointments().oauthPatient());
  }
}
