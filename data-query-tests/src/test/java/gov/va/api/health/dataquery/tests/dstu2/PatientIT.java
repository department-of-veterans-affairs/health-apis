package gov.va.api.health.dataquery.tests.dstu2;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dataquery.tests.TestClients;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.categories.Local;
import gov.va.api.health.sentinel.categories.Smoke;
import io.restassured.http.Header;
import io.restassured.http.Method;
import lombok.experimental.Delegate;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class PatientIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.dstu2();

  @Test
  @Category({Local.class, LabDataQueryClinician.class, ProdDataQueryClinician.class})
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            Patient.Bundle.class,
            "Patient?family={family}&gender={gender}",
            verifier.ids().pii().family(),
            verifier.ids().pii().gender()),
        test(
            200,
            Patient.Bundle.class,
            "Patient?given={given}&gender={gender}",
            verifier.ids().pii().given(),
            verifier.ids().pii().gender()),
        test(
            200,
            Patient.Bundle.class,
            "Patient?name={name}&birthdate={birthdate}",
            verifier.ids().pii().name(),
            verifier.ids().pii().birthdate()),
        test(
            200,
            Patient.Bundle.class,
            "Patient?name={name}&gender={gender}",
            verifier.ids().pii().name(),
            verifier.ids().pii().gender()),
        /*
         * These are tests for the UnsatisfiedServletRequestParameterException mapping to bad
         * request.
         */
        test(400, OperationOutcome.class, "Patient?given={given}", verifier.ids().pii().given()),
        test(400, OperationOutcome.class, "Patient/"));
  }

  @Test
  @Category({
    Local.class,
    Smoke.class,
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void basic() {
    verifier.verifyAll(
        test(200, Patient.class, "Patient/{id}", verifier.ids().patient()),
        test(200, Patient.Bundle.class, "Patient?_id={id}", verifier.ids().patient()));
  }

  /**
   * The CDW database has disabled patient searching by identifier for both PROD/QA. We will test
   * this only in LOCAL mode against the sandbox db.
   */
  @Test
  @Category(Local.class)
  public void patientIdentifierSearching() {
    verifier.verify(
        test(200, Patient.Bundle.class, "Patient?identifier={id}", verifier.ids().patient()));
  }

  /**
   * In the PROD/QA environments, patient reading is restricted to your unique access-token. Any IDs
   * but your own are revoked with a 403 Forbidden. In environments where this restriction is
   * lifted, the result of an unknown ID should be 404 Not Found.
   */
  @Test
  @Category({
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void patientMatching() {
    int status = (Environment.get() == Environment.LOCAL) ? 404 : 403;
    verifier.verifyAll(
        test(status, OperationOutcome.class, "Patient/{id}", verifier.ids().unknown()),
        test(status, OperationOutcome.class, "Patient?_id={id}", verifier.ids().unknown()));
  }

  /**
   * Temporary equality validation while we support backwards compatibility of patient v1 and v2.
   */
  @Test
  @Category({Local.class, LabDataQueryClinician.class, ProdDataQueryClinician.class})
  public void patientV1EqualsV2Read() {
    var patientV1 =
        TestClients.dstu2DataQuery()
            .service()
            .requestSpecification()
            .header(new Header("patientV2", "false"))
            .request(
                Method.GET,
                TestClients.dstu2DataQuery().service().urlWithApiPath()
                    + "Patient/"
                    + verifier.ids().patient())
            .getBody()
            .asString();
    var patientV2 =
        TestClients.dstu2DataQuery()
            .service()
            .requestSpecification()
            .header(new Header("patientV2", "true"))
            .request(
                Method.GET,
                TestClients.dstu2DataQuery().service().urlWithApiPath()
                    + "Patient/"
                    + verifier.ids().patient())
            .getBody()
            .asString();
    assertThat(patientV1).isEqualTo(patientV2);
  }

  /**
   * Temporary equality validation while we support backwards compatibility of patient v1 and v2.
   */
  @Test
  @Category({
    Local.class,
    Smoke.class,
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void patientV1EqualsV2SearchByNameAndGender() {
    var patientV1 =
        TestClients.dstu2DataQuery()
            .service()
            .requestSpecification()
            .header(new Header("patientV2", "false"))
            .request(
                Method.GET,
                TestClients.dstu2DataQuery().service().urlWithApiPath()
                    + "Patient?given="
                    + verifier.ids().pii().given()
                    + "&gender="
                    + verifier.ids().pii().gender())
            .getBody()
            .asString();
    var patientV2 =
        TestClients.dstu2DataQuery()
            .service()
            .requestSpecification()
            .header(new Header("patientV2", "true"))
            .request(
                Method.GET,
                TestClients.dstu2DataQuery().service().urlWithApiPath()
                    + "Patient?given="
                    + verifier.ids().pii().given()
                    + "&gender="
                    + verifier.ids().pii().gender())
            .getBody()
            .asString();
    assertThat(patientV1).isEqualTo(patientV2);
  }
}
