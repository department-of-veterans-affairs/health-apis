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
  private String version;
  private String publisher;
  private StatementType statementType;
  private ContactProperties contact;
  private String publicationDate;
  private String description;
  private String softwareName;
  private boolean productionUse;
  private VersionSpecificProperties r4;
  private VersionSpecificProperties dstu2;

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
    private String managementEndpoint;
    private String revocationEndpoint;
    private String description;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Accessors(fluent = false)
  public static class VersionSpecificProperties {
    private String id;
    private String name;
    private String resourceDocumentation;
  }
}
