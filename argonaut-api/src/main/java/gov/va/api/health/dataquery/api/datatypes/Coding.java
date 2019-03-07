package gov.va.api.health.dataquery.api.datatypes;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import gov.va.api.health.dataquery.api.Fhir;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(description = "http://hl7.org/fhir/DSTU2/datatypes.html#Coding")
public class Coding {
  @Pattern(regexp = Fhir.URI)
  String system;

  String version;

  @Pattern(regexp = Fhir.CODE)
  String code;

  String display;

  Boolean userSelected;

  /** All-args builder constructor. */
  @Builder
  public Coding(String system, String version, String code, String display, Boolean userSelected) {
    this.system = defaultIfBlank(system, null);
    this.version = defaultIfBlank(version, null);
    this.code = defaultIfBlank(code, null);
    this.display = defaultIfBlank(display, null);
    this.userSelected = userSelected;
  }
}
