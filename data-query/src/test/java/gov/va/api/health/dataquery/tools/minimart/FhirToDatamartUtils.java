package gov.va.api.health.dataquery.tools.minimart;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.io.FileInputStream;
import java.util.Optional;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FhirToDatamartUtils {

  private final Properties props;

  @SneakyThrows
  FhirToDatamartUtils(String configFile) {
    this.props = new Properties(System.getProperties());
    try (FileInputStream inputStream = new FileInputStream(configFile)) {
      props.load(inputStream);
    }
  }

  public static String getReferenceIdentifier(String reference) {
    String[] splitRef = reference.split("/");
    return splitRef[splitRef.length - 1];
  }

  public static String getReferenceType(String reference) {
    String[] splitRef = reference.split("/");
    return splitRef[splitRef.length - 2];
  }

  public Optional<DatamartReference> toDatamartReferenceWithCdwId(Reference reference) {
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
      String realId = unmask(referenceType, referenceId);
      return Optional.of(
          DatamartReference.builder()
              .type(Optional.of(referenceType))
              .display(Optional.ofNullable(reference.display()))
              .reference(Optional.ofNullable(realId))
              .build());
    }
    return Optional.of(
        DatamartReference.builder().display(Optional.of(reference.display())).build());
  }

  public String unmask(String resourceName, String publicId) {
    String idsPropertyName = resourceName.toUpperCase() + "+" + publicId;
    String cdwId = props.getProperty(idsPropertyName, "");
    if (cdwId.isBlank()) {
      throw new RuntimeException("Ids value not found for property: " + idsPropertyName);
    }
    log.info("{}:{} - cdwId {}", resourceName, publicId, cdwId);
    return cdwId;
  }

  public String unmaskByReference(String reference) {
    return unmask(getReferenceType(reference), getReferenceIdentifier(reference));
  }
}
