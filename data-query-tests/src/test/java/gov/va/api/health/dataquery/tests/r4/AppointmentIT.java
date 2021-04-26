package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.Environment.LAB;
import static gov.va.api.health.sentinel.Environment.LOCAL;
import static gov.va.api.health.sentinel.Environment.STAGING_LAB;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.Oauth;
import gov.va.api.health.dataquery.tests.SystemDefinitions;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.AccessTokens;
import gov.va.api.health.sentinel.Environment;
import java.time.Year;
import java.util.List;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class AppointmentIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void advanced() {
    assumeEnvironmentIn(STAGING_LAB, LAB, LOCAL);
    verifier.verifyAll(
        test(200, Appointment.Bundle.class, "Appointment?_id={id}", testIds.appointment()),
        test(
            200,
            Appointment.Bundle.class,
            r -> r.entry().isEmpty(),
            "Appointment?_id={id}",
            testIds.unknown()),
        test(200, Appointment.Bundle.class, "Appointment?identifier={id}", testIds.appointment()));
  }

  @Test
  public void basic() {
    assumeEnvironmentIn(LOCAL, STAGING_LAB, LAB);
    verifier.verifyAll(
        test(200, Appointment.class, "Appointment/{id}", testIds.appointment()),
        test(404, OperationOutcome.class, "Appointment/{id}", testIds.unknown()),
        test(200, Appointment.Bundle.class, "Appointment?patient={patient}", testIds.patient()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?patient={patient}&location={location}",
            testIds.patient(),
            testIds.appointments().location()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?patient={patient}&location={location}&_lastUpdated={lastUpdated}",
            testIds.patient(),
            testIds.appointments().location(),
            testIds.appointments().lastUpdated()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?patient={patient}&_lastUpdated={lastUpdated}",
            testIds.patient(),
            testIds.appointments().lastUpdated()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?patient={patient}&date={date}",
            testIds.patient(),
            testIds.appointments().date()));
  }

  /**
   * Searching that would typically requires a clinician-scoped token. We don't have a kong local,
   * so we can ignore the token.
   */
  @Test
  void clinicianSearching() {
    assumeEnvironmentIn(LOCAL);
    verifier.verifyAll(
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?location={location}",
            testIds.appointments().location()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?_lastUpdated={lastUpdated}",
            testIds.appointments().lastUpdated()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?location={location}&_lastUpdated={lastUpdated}",
            testIds.appointments().location(),
            testIds.appointments().lastUpdated()),
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
    String token = AccessTokens.get().forSystemScopes(List.of("system/Appointment.read"));
    var sd = SystemDefinitions.systemDefinition().r4DataQuery();
    // Valid Token
    Oauth.test(
        sd,
        200,
        Appointment.Bundle.class,
        token,
        "Appointment?patient={icn}",
        testIds.appointments().oauthPatient());
    // Invalid Token
    Oauth.test(
        sd,
        401,
        OperationOutcome.class,
        "NOPE",
        "Appointment?patient={icn}",
        testIds.appointments().oauthPatient());
    // Invalid Resource for Scopes
    Oauth.test(
        sd,
        403,
        OperationOutcome.class,
        token,
        "Observation?patient={icn}",
        testIds.appointments().oauthPatient());
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(LOCAL);
    verifier.verifyAll(
        test(403, OperationOutcome.class, "Appointment?patient={patient}", testIds.unknown()));
  }
}
