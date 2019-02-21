package gov.va.api.health.argonaut.api.resources;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(description = "Well-Known")
public class WellKnown{

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
