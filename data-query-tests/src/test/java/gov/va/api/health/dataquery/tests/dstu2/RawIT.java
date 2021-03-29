package gov.va.api.health.dataquery.tests.dstu2;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import io.restassured.RestAssured;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * This class is only meant to test the "raw" functionality of data-query Regular reads and searches
 * are performed in each individual resource's IT tests
 */
@Slf4j
public class RawIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.dstu2();

  TestIds testIds = DataQueryResourceVerifier.ids();

  RequestSpecification raw =
      RestAssured.given()
          .spec(verifier.testClient().service().requestSpecification())
          .headers(ImmutableMap.of("raw", System.getProperty("raw-token", "true")));

  @Test
  public void allergyIntoleranceRaw() {
    assertFhirObject("AllergyIntolerance", testIds.allergyIntolerance());
  }

  @SneakyThrows
  public void assertFhirObject(String resourceName, String publicId) {
    // Verify it is a raw response from the correct resource
    String fhirObjectType =
        readRaw(resourceName, publicId)
            .jsonPath()
            .using(JsonPathConfig.jsonPathConfig().charset("UTF-8"))
            .get("objectType")
            .toString();
    assertThat(fhirObjectType).isEqualTo(resourceName);
  }

  @Test
  public void conditionRaw() {
    assertFhirObject("Condition", testIds.condition());
  }

  @Test
  @SneakyThrows
  public void diagnosticReportRaw() {
    // objectType is not returned in a raw diagnosticReport read, so we'll make sure it has an
    // identifier instead
    Response response = readRaw("DiagnosticReport", testIds.diagnosticReport());
    String resourceIdentifier =
        response
            .jsonPath()
            .using(JsonPathConfig.jsonPathConfig().charset("UTF-8"))
            .get("cdwId")
            .toString();
    assertThat(resourceIdentifier).isNotBlank();
  }

  @Test
  public void immunizationRaw() {
    assertFhirObject("Immunization", testIds.immunization());
  }

  @Test
  public void locationRaw() {
    assertFhirObject("Location", testIds.location());
  }

  @Test
  public void medicationOrderRaw() {
    assertFhirObject("MedicationOrder", testIds.medicationOrder());
  }

  @Test
  public void medicationRaw() {
    assertFhirObject("Medication", testIds.medication());
  }

  @Test
  public void medicationStatementRaw() {
    assertFhirObject("MedicationStatement", testIds.medicationStatement());
  }

  @Test
  public void observationRaw() {
    assertFhirObject("Observation", testIds.observation());
  }

  @Test
  public void organizationRaw() {
    assertFhirObject("Organization", testIds.organization());
  }

  @Test
  public void patientRaw() {
    assertFhirObject("Patient", testIds.patient());
  }

  @Test
  public void practitionerRaw() {
    assertFhirObject("Practitioner", testIds.practitioner());
  }

  @Test
  public void procedureRaw() {
    assertFhirObject("Procedure", testIds.procedure());
  }

  @SneakyThrows
  public Response readRaw(String resourceName, String publicId) {
    String path = verifier.testClient().service().apiPath() + resourceName + "/" + publicId;
    log.info("Verify raw response for {}, with [{}]", path, publicId);
    Response response = raw.get(path);
    assertThat(response.getStatusCode()).isEqualTo(200);
    return response;
  }
}
