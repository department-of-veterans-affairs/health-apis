package gov.va.api.health.dataquery.api.elements;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import gov.va.api.health.dataquery.api.Fhir;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
@Schema(description = "http://hl7.org/fhir/DSTU2/narrative.html")
public class Narrative implements Element {
  @Pattern(regexp = Fhir.ID)
  String id;

  @Valid List<Extension> extension;

  @NotNull NarrativeStatus status;

  @NotBlank
  @Pattern(regexp = Fhir.XHTML)
  String div;

  @SuppressWarnings("unused")
  public enum NarrativeStatus {
    generated,
    extensions,
    additional,
    empty
  }
}
