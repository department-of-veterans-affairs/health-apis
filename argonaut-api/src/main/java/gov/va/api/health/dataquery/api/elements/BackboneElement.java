package gov.va.api.health.dataquery.api.elements;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "http://hl7.org/fhir/DSTU2/backboneelement.html")
public interface BackboneElement extends Element {
  List<Extension> modifierExtension();
}
