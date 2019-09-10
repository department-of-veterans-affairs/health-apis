package gov.va.api.health.dataquery.tools.minimart;

import com.fasterxml.jackson.core.type.TypeReference;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.ids.api.ResourceIdentity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FhirToDatamartUtils {

  public static String getReferenceIdentifier(String reference) {
    String[] splitRef = reference.split("/");
    return splitRef[splitRef.length - 1];
  }

  @SneakyThrows
  public static String revealSecretIdentity(String villainId) {
    Response response =
        RestAssured.given()
            .headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON)
            .when()
            .get("http://localhost:8089/api/resourceIdentity/{id}", villainId)
            .then()
            .contentType(ContentType.JSON)
            .statusCode(200)
            .extract()
            .response();
    String jsonBody = response.getBody().print();
    List<ResourceIdentity> resourceIdentities =
        JacksonConfig.createMapper()
            .readValue(jsonBody, new TypeReference<List<ResourceIdentity>>() {});
    return resourceIdentities
        .stream()
        .filter(i -> i.system().equalsIgnoreCase("CDW"))
        .map(ResourceIdentity::identifier)
        .findFirst()
        .orElse(null);
  }

  public static Optional<DatamartReference> toDatamartReferenceWithCdwId(Reference reference) {
    if (reference == null) {
      return null;
    }
    if (reference.reference() == null && reference.display() == null) {
      return null;
    }

    if (reference.reference() != null) {
      String[] fhirUrl = reference.reference().split("/");
      String referenceType = fhirUrl[fhirUrl.length - 2];
      String referenceId = fhirUrl[fhirUrl.length - 1];
      String realId = revealSecretIdentity(referenceId);
      return Optional.of(
          DatamartReference.builder()
              .type(Optional.of(referenceType))
              .reference(realId != null ? Optional.of(realId) : null)
              .display(reference.display() != null ? Optional.of(reference.display()) : null)
              .build());
    } else {
      return Optional.of(
          DatamartReference.builder().display(Optional.of(reference.display())).build());
    }
  }
}
