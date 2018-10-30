package gov.va.api.health.argonaut.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(description = "http://hl7.org/fhir/DSTU2/datatypes.html#Identifier")
public class Identifier implements Element {
  @Pattern(regexp = Fhir.ID)
  String id;

  @Valid List<Extension> extension;

  IdentifierUse use;

  @Valid CodeableConcept type;

  @Pattern(regexp = Fhir.URI)
  String system;

  String value;
  @Valid Period period;
  @Valid Reference assigner;

  public enum IdentifierUse {
    usual,
    official,
    temp,
    secondary
  }
}
