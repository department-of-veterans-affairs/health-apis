package gov.va.api.health.dataquery.tools.minimart;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.io.FileInputStream;
import java.util.Optional;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RevealSecretIdentity {

  private final Properties props;

  @SneakyThrows
  RevealSecretIdentity(String configFile) {
    this.props = new Properties(System.getProperties());
    try (FileInputStream inputStream = new FileInputStream(configFile)) {
      props.load(inputStream);
    }
  }

  public Optional<DatamartReference> toDatamartReferenceWithCdwId(Reference reference) {
    if (reference == null) {
      return null;
    }
    String[] fhirUrl = reference.reference().split("/");
    // Fhir Resource
    String referenceType = fhirUrl[fhirUrl.length - 2];
    // Public Id
    String referenceId = fhirUrl[fhirUrl.length - 1];
    String realId = unmask(referenceType, referenceId);
    return Optional.of(
        DatamartReference.builder()
            .type(Optional.of(referenceType))
            .reference(realId != null ? Optional.of(realId) : null)
            .display(reference.display() != null ? Optional.of(reference.display()) : null)
            .build());
  }

  public String unmask(String resourceName, String publicId) {
    String idsPropertyName = resourceName.toUpperCase() + "+" + publicId;
    String cdwId = props.getProperty(idsPropertyName, "");
    if (cdwId.isBlank()) {
      throw new RuntimeException("Ids value not found for property: " + idsPropertyName);
    }
    return cdwId;
  }
}
