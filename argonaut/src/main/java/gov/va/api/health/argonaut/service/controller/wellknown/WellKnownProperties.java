package gov.va.api.health.argonaut.service.controller.wellknown;

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
@ConfigurationProperties("well-known")
@Data
@Accessors(fluent = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WellKnownProperties {
  private String capabilities;
  private SecurityProperties security;

  @Data
  @Accessors(fluent = false)
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SecurityProperties {
    private String tokenEndpoint;
    private String authorizeEndpoint;
  }
}
