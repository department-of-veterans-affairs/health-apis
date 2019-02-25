package gov.va.api.health.argonaut.service.controller.wellknown;

import gov.va.api.health.argonaut.api.information.WellKnown;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
  value = {".well-known/smart-configuration"},
  produces = {"application/json", "application/fhir+json", "application/json+fhir"}
)
@AllArgsConstructor(onConstructor = @__({@Autowired}))
class WellKnownController {

  private final WellKnownProperties properties;

  @GetMapping
  WellKnown read() {
    return WellKnown.builder()
        .authorizationEndpoint(properties.getSecurity().getAuthorizeEndpoint())
        .tokenEndpoint(properties.getSecurity().getTokenEndpoint())
        .capabilities(properties.getCapabilities())
        .responseTypeSupported(properties.getResponseTypeSupported())
        .scopesSupported(properties.getScopesSupported())
        .build();
  }
}
