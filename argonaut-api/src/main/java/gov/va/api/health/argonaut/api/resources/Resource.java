package gov.va.api.health.argonaut.api.resources;

import gov.va.api.health.argonaut.api.elements.Meta;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "https://www.hl7.org/fhir/resource.html")
public interface Resource {

  String id();

  String implicitRules();

  String language();

  Meta meta();
}
