package gov.va.api.health.argonaut.service.controller.wellknown;

import java.util.List;
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
@ConfigurationProperties("conformance")
@Data
@Accessors(fluent = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WellKnownProperties {
  private List<String> capabilities;
  private SecurityProperties security;
  private List<String> responseTypeSupported;
  private List<String> scopesSupported;

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
