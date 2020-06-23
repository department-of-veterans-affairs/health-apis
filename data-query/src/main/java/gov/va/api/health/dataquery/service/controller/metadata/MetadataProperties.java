package gov.va.api.health.dataquery.service.controller.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("DefaultAnnotationParam")
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("metadata")
@Data
@Accessors(fluent = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataProperties {
  private String id;
  private String version;
  private String dstu2Name;
  private String r4Name;
  private String publisher;
  private StatementType statementType;
  private ContactProperties contact;
  private String publicationDate;
  private String description;
  private String softwareName;
  private boolean productionUse;
  /**
   * This is specific to DSTU2, STU3, or R4 and not used across versions. It is also specific to the
   * implementation itself and hard coded.
   */
  @Deprecated private String fhirVersion;

  private SecurityProperties security;
  private String resourceDocumentation;

  enum StatementType {
    CLINICIAN,
    PATIENT
  }

  @Data
  @Accessors(fluent = false)
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ContactProperties {
    private String name;
    private String email;
  }

  @Data
  @Accessors(fluent = false)
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SecurityProperties {
    private String tokenEndpoint;
    private String authorizeEndpoint;
    private String manageEndpoint;
    private String revocationEndpoint;
    private String description;
  }
}
