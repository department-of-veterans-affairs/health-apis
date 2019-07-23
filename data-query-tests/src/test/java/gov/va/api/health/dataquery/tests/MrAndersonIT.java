package gov.va.api.health.dataquery.tests;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.TestClient;
import gov.va.api.health.sentinel.categories.Local;
import io.restassured.http.Method;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MrAndersonIT {
  private final IdRegistrar registrar = IdRegistrar.of(SystemDefinitions.systemDefinition());

  private String apiPath() {
    return TestClients.mrAnderson().service().apiPath();
  }

  private TestIds ids() {
    return registrar.registeredIds();
  }

  @Category(Local.class)
  @Test
  public void invalidCountSpecifiedReturns400() {
    mrAnderson()
        .get(
            apiPath() + "v1/resources/argonaut/Patient/1.03?identifier={id}&_count=-1",
            ids().patient())
        .expect(400);
  }

  @Category(Local.class)
  @Test
  public void invalidPageSpecifiedReturns400() {
    mrAnderson()
        .get(
            apiPath() + "v1/resources/argonaut/Patient/1.03?identifier={id}&page=-1",
            ids().patient())
        .expect(400);
  }

  @Category(Local.class)
  @Test
  public void invalidQueryParamsReturns400() {
    mrAnderson().get(apiPath() + "v1/resources/argonaut/Patient/1.03?stuff=missing").expect(400);
  }

  private TestClient mrAnderson() {
    return TestClients.mrAnderson();
  }

  @Category(Local.class)
  @Test
  public void noResultsReturns200WithEmptyResults() {
    String id = registrar.register("DIAGNOSTIC_REPORT", "5555555555555-mra-it");
    String records =
        mrAnderson()
            .get(apiPath() + "v1/resources/argonaut/DiagnosticReport/1.02?identifier={id}", id)
            .expect(200)
            .response()
            .path("root.recordCount");
    assertThat(records).isEqualTo("0");
  }

  @Category(Local.class)
  @Test
  public void pageAndCountCanBeOmittedAndDefaultToOneAnd15() {
    mrAnderson()
        .get(
            apiPath() + "v1/resources/argonaut/DiagnosticReport/1.02?patient={id}", ids().patient())
        .expect(200);
  }

  @Category(Local.class)
  @Test
  public void patientCanBeReadAfterIdHasBeenRegistered() {
    mrAnderson()
        .get(apiPath() + "v1/resources/argonaut/Patient/1.03?identifier={id}", ids().patient())
        .expect(200);
  }

  @Category(Local.class)
  @Test
  public void rawResponseDoesNotReplaceReferences() {
    String reference =
        ExpectedResponse.of(
                mrAnderson()
                    .service()
                    .requestSpecification()
                    .header("Mr-Anderson-Raw", "true")
                    .contentType("application/xml")
                    .request()
                    .request(
                        Method.GET,
                        apiPath() + "v1/resources/argonaut/DiagnosticReport/1.02?patient={id}",
                        ids().patient()))
            .expect(200)
            .response()
            .path("root.diagnosticReports.diagnosticReport[0].encounter.reference");
    assertThat(reference).isEqualTo("Encounter/1000511190181");
  }

  @Category(Local.class)
  @Test
  public void recordsCanBeSearchedByPatientThenReadById() {
    String id =
        mrAnderson()
            .get(
                apiPath()
                    + "v1/resources/argonaut/DiagnosticReport/1.02?patient={id}&page=1&_count=15",
                ids().patient())
            .expect(200)
            .response()
            .path("root.diagnosticReports.diagnosticReport[0].cdwId");
    String id2 =
        mrAnderson()
            .get(apiPath() + "v1/resources/argonaut/DiagnosticReport/1.02?identifier={id}", id)
            .expect(200)
            .response()
            .path("root.diagnosticReports.diagnosticReport[0].cdwId");
    assertThat(id2).isEqualTo(id);
  }

  @Category(Local.class)
  @Test
  public void unknownResourceReturns404() {
    mrAnderson()
        .get(apiPath() + "v1/resources/argonaut/Patient/9.99?identifier=" + ids().patient())
        .expect(404);
  }

  @Category(Local.class)
  @Test
  public void unregisteredIdReturns404() {
    mrAnderson()
        .get(apiPath() + "v1/resources/argonaut/Patient/1.03?identifier=" + ids().unknown())
        .expect(404);
  }
}
