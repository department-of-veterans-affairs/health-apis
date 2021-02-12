package gov.va.api.health.dataquery.tests.r4;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import gov.va.api.health.dataquery.tests.ResourceVerifier;
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
  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  RequestSpecification raw =
      RestAssured.given()
          .spec(verifier.dataQuery().service().requestSpecification())
          .headers(ImmutableMap.of("raw", System.getProperty("raw-token", "true")));

  @Test
  public void allergyIntoleranceRaw() {
    assertFhirObject("AllergyIntolerance", verifier.ids().allergyIntolerance());
  }

  @Test
  public void appointmentRaw() {
    assertFhirObject("Appointment", verifier.ids().appointment());
  }

  @SneakyThrows
  public void assertFhirObject(String resourceName, String publicId) {
    // Verify it is a raw response from the correct resource
    assertFhirObject(resourceName, resourceName, publicId);
  }

  @SneakyThrows
  public void assertFhirObject(String oldResourceName, String r4ResourceName, String publicId) {
    // Verify it is a raw response as an old resource type from an r4 resource
    String fhirObjectType =
        readRaw(r4ResourceName, publicId)
            .jsonPath()
            .using(JsonPathConfig.jsonPathConfig().charset("UTF-8"))
            .get("objectType")
            .toString();
    assertThat(fhirObjectType).isEqualTo(oldResourceName);
  }

  @Test
  public void conditionRaw() {
    assertFhirObject("Condition", verifier.ids().condition());
  }

  @Test
  public void deviceRaw() {
    assertFhirObject("Device", verifier.ids().device());
  }

  @Test
  @SneakyThrows
  public void diagnosticReportRaw() {
    assertFhirObject("DiagnosticReport", verifier.ids().diagnosticReport());
  }

  @Test
  public void immunizationRaw() {
    assertFhirObject("Immunization", verifier.ids().immunization());
  }

  @Test
  public void locationRaw() {
    assertFhirObject("Location", verifier.ids().location());
  }

  @Test
  public void medicationRaw() {
    assertFhirObject("Medication", verifier.ids().medication());
  }

  @Test
  public void medicationRequestOrderRaw() {
    assertFhirObject("MedicationOrder", "MedicationRequest", verifier.ids().medicationOrder());
  }

  @Test
  public void medicationRequestStatementRaw() {
    assertFhirObject(
        "MedicationStatement", "MedicationRequest", verifier.ids().medicationStatement());
  }

  @Test
  public void observationRaw() {
    assertFhirObject("Observation", verifier.ids().observation());
  }

  @Test
  public void organizationRaw() {
    assertFhirObject("Organization", verifier.ids().organization());
  }

  @Test
  public void patientRaw() {
    assertFhirObject("Patient", verifier.ids().patient());
  }

  @Test
  public void practitionerRaw() {
    assertFhirObject("Practitioner", verifier.ids().practitioner());
  }

  @Test
  public void practitionerRoleRaw() {
    assertFhirObject("Practitioner", "PractitionerRole", verifier.ids().practitioner());
  }

  @Test
  public void procedureRaw() {
    assertFhirObject("Procedure", verifier.ids().procedure());
  }

  @SneakyThrows
  public Response readRaw(String resourceName, String publicId) {
    String path = verifier.dataQuery().service().apiPath() + resourceName + "/" + publicId;
    log.info("Verify raw response for {}, with [{}]", path, publicId);
    Response response = raw.get(path);
    assertThat(response.getStatusCode()).isEqualTo(200);
    return response;
  }
}
