package gov.va.api.health.argonaut.api.information;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
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
@Schema(
  description = "http://hl7.org/fhir/smart-app-launch/conformance/index.html#using-well-known"
)
public final class WellKnown {

  @NotBlank String authorizationEndpoint;
  @NotBlank String tokenEndpoint;
  List<String> tokenEndpointAuthMethodsSupported;
  String registrationEndpoint;
  List<String> scopesSupported;
  List<String> responseTypeSupported;
  String managementEndpoint;
  String introspectionEndpoint;
  String revocationEndpoint;
  @NotEmpty List<String> capabilities;
}
