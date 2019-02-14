package gov.va.health.api.sentinel;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import lombok.AllArgsConstructor;
import lombok.Value;

/** The set of different test clients used to interact with different services within a system. */
@Value
@AllArgsConstructor(staticName = "of")
public class TestClients {
  SystemDefinition systemDefinition;

  TestClient argonaut() {
    return FhirTestClient.builder()
        .service(systemDefinition.argonaut())
        .mapper(JacksonConfig::createMapper)
        .build();
  }

  TestClient ids() {
    return BasicTestClient.builder()
        .service(systemDefinition.ids())
        .contentType("application/json")
        .mapper(JacksonConfig::createMapper)
        .build();
  }

  TestClient mrAnderson() {
    return BasicTestClient.builder()
        .service(systemDefinition.mrAnderson())
        .contentType("application/xml")
        .mapper(JacksonConfig::createMapper)
        .build();
  }
}
