package gov.va.api.health.dataquery.tools.minimart;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.ids.client.RestIdentityServiceClientConfig;
import groovy.util.logging.Slf4j;
import lombok.NoArgsConstructor;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
public class RevealSecretIdentity {

  private static RestTemplate restTemplate;

  private static String idsUrl;

  private static IdentityService identityService;

  public RevealSecretIdentity() {
    restTemplate = new RestTemplate();
    idsUrl = "http://localhost:8089";
    identityService =
        new RestIdentityServiceClientConfig(restTemplate, idsUrl).restIdentityServiceClient();
  }

  public static String unmask(String villainId) {
    try {
      return identityService.lookup(villainId).stream()
          .filter(id -> id.system().equalsIgnoreCase("CDW"))
          .map(ResourceIdentity::identifier)
          .findFirst()
          .orElse(null);
    } catch (IdentityService.UnknownIdentity e) {
      // ~~ Just keep swimming ~~
      log.error("Exception thrown while unmasking: {}", villainId);
      return null;
    }
  }

  public static Optional<DatamartReference> toDatamartReference(Reference reference) {
    if (reference == null) {
      return null;
    }
    String[] fhirUrl = reference.reference().split("/");
    String referenceType = fhirUrl[fhirUrl.length - 2];
    String referenceId = fhirUrl[fhirUrl.length - 1];

    return Optional.of(
        DatamartReference.builder()
            .type(Optional.of(referenceType))
            .reference(Optional.of(unmask(referenceId)))
            .display(Optional.of(reference.display()))
            .build());
  }
}
