package gov.va.api.health.dataquery.api.datatypes;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import gov.va.api.health.dataquery.api.Fhir;
import gov.va.api.health.dataquery.api.elements.Element;
import gov.va.api.health.dataquery.api.elements.Extension;
import gov.va.api.health.dataquery.api.validation.ZeroOrOneOf;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
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
@Schema(description = "http://hl7.org/fhir/DSTU2/datatypes.html#Timing")
public class Timing implements Element {
  @Pattern(regexp = Fhir.ID)
  String id;

  @Valid List<Extension> extension;

  List<@Pattern(regexp = Fhir.DATETIME) String> event;

  @Valid CodeableConcept code;
  @Valid Repeat repeat;

  @SuppressWarnings("unused")
  public enum EventTiming {
    HS,
    WAKE,
    C,
    CM,
    CD,
    CV,
    AC,
    ACM,
    ACD,
    ACV,
    PC,
    PCM,
    PCD,
    PCV
  }

  @SuppressWarnings("unused")
  public enum UnitsOfTime {
    s,
    min,
    h,
    d,
    wk,
    mo,
    a
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @ZeroOrOneOf(fields = {"boundsQuantity", "boundsRange", "boundsPeriod"})
  public static class Repeat implements Element {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid Duration boundsQuantity;
    @Valid Range boundsRange;
    @Valid Period boundsPeriod;
    @Valid List<Extension> extension;
    Integer count;
    Double duration;
    Double durationMax;
    @Valid UnitsOfTime durationUnits;
    Integer frequency;
    Integer frequencyMax;
    Double period;
    Double periodMax;
    @Valid UnitsOfTime periodUnits;
    EventTiming when;
  }
}
