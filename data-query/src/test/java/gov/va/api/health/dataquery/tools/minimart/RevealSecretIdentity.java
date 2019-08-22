package gov.va.api.health.dataquery.tools.minimart;

import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.client.RestIdentityServiceClientConfig;
import groovy.util.logging.Slf4j;
import java.util.Optional;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class RevealSecretIdentity {

  private WitnessProtection witnessProtection;

  public RevealSecretIdentity() {
    String idsUrl = "http://localhost:8089";
    RestTemplate restTemplate = new RestTemplate();
    IdentityService identityService =
        new RestIdentityServiceClientConfig(restTemplate, idsUrl).restIdentityServiceClient();
    this.witnessProtection = new WitnessProtection(identityService);
  }

  public Optional<DatamartReference> toDatamartReferenceWithCdwId(Reference reference) {
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

  public String unmask(String villainId) {
    return witnessProtection.toCdwId(villainId);
  }
}
