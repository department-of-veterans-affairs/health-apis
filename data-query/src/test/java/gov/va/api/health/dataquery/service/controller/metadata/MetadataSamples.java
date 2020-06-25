package gov.va.api.health.dataquery.service.controller.metadata;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.config.ReferenceSerializerProperties;
import gov.va.api.health.dataquery.service.controller.metadata.MetadataProperties.ContactProperties;
import gov.va.api.health.dataquery.service.controller.metadata.MetadataProperties.SecurityProperties;
import lombok.SneakyThrows;

class MetadataSamples {
  @SuppressWarnings("deprecation")
  static MetadataProperties conformanceStatementProperties() {
    return MetadataProperties.builder()
        .id("lighthouse-va-fhir-conformance")
        .version("1.4.0")
        .dstu2Name("VA Lighthouse FHIR")
        .r4Name("VA Lighthouse FHIR R4")
        .productionUse(true)
        .publisher("Lighthouse Team")
        .contact(
            ContactProperties.builder()
                .name("Drew Myklegard")
                .email("david.myklegard@va.gov")
                .build())
        .publicationDate("2018-09-27T19:30:00-05:00")
        .description(
            "This is the base conformance statement for FHIR."
                + " It represents a server that provides the full"
                + " set of functionality defined by FHIR."
                + " It is provided to use as a template for system designers to"
                + " build their own conformance statements from.")
        .softwareName("VA Lighthouse")
        .fhirVersion("1.0.2")
        .security(
            SecurityProperties.builder()
                .tokenEndpoint("https://argonaut.lighthouse.va.gov/token")
                .authorizeEndpoint("https://argonaut.lighthouse.va.gov/authorize")
                .managementEndpoint("https://argonaut.lighthouse.va.gov/manage")
                .revocationEndpoint("https://argonaut.lighthouse.va.gov/revoke")
                .description(
                    "This is the conformance statement to declare that the server"
                        + " supports SMART-on-FHIR. See the SMART-on-FHIR docs for the"
                        + " extension that would go with such a server.")
                .build())
        .resourceDocumentation("Implemented per the specification")
        .build();
  }

  @SneakyThrows
  static String pretty(Object object) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
  }

  static ReferenceSerializerProperties referenceSerializerProperties(boolean isEnabled) {
    return ReferenceSerializerProperties.builder()
        .location(isEnabled)
        .organization(isEnabled)
        .practitioner(isEnabled)
        .build();
  }
}
