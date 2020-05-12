package gov.va.api.health.dataquery.service.controller.metadata;

import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.service.config.ReferenceSerializerProperties;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.r4.api.resources.CapabilityStatement;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Implementation;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Kind;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Status;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = {"/r4/metadata"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
class R4MetadataController {

  private final ConfigurableBaseUrlPageLinks pageLinks;

  private final MetadataProperties properties;

  private final ReferenceSerializerProperties referenceSerializerProperties;

  private Implementation implementation() {
    return Implementation.builder()
        .description(properties.getName())
        .url(pageLinks.r4Url())
        .build();
  }

  @GetMapping
  CapabilityStatement read() {
    // TODO REMOVE
    referenceSerializerProperties.toString();
    return CapabilityStatement.builder()
        .resourceType("CapabilityStatement")
        .id(properties.getId())
        .version(properties.getVersion())
        .name(properties.getName())
        .title(properties.getName())
        .publisher(properties.getPublisher())
        .status(Status.active)
        .implementation(implementation())
        // .experimental()
        // .contact(contact())
        .date(properties.getPublicationDate())
        .description(properties.getDescription())
        .kind(Kind.capability)
        //  .software(software())
        .fhirVersion("4.0.1")
        .format(asList("application/json", "application/fhir+json"))
        // .rest(rest())
        .build();
  }
}
