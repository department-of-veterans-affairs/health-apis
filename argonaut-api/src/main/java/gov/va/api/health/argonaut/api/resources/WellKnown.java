package gov.va.api.health.argonaut.api.resources;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import gov.va.api.health.argonaut.api.Fhir;
import gov.va.api.health.argonaut.api.elements.Meta;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(description = "Well-Known")
public final class WellKnown {

  @NotBlank String authorizationEndpoint;
  @NotBlank String tokenEndpoint;
  String[] tokenEndpointAuthMethodsSupported;
  String registrationEndpoint;
  String[] scopesSupported;
  String[] responseTypeSupported;
  String managementEndpoint;
  String introspectionEndpoint;
  String revocationEndpoint;
  @NotEmpty String[] capabilities;
}
