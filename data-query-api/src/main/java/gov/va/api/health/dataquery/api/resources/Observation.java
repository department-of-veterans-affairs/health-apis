package gov.va.api.health.dataquery.api.resources;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gov.va.api.health.dataquery.api.Fhir;
import gov.va.api.health.dataquery.api.bundle.AbstractBundle;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.datatypes.Attachment;
import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Identifier;
import gov.va.api.health.dataquery.api.datatypes.Period;
import gov.va.api.health.dataquery.api.datatypes.Quantity;
import gov.va.api.health.dataquery.api.datatypes.Range;
import gov.va.api.health.dataquery.api.datatypes.Ratio;
import gov.va.api.health.dataquery.api.datatypes.SampledData;
import gov.va.api.health.dataquery.api.datatypes.Signature;
import gov.va.api.health.dataquery.api.datatypes.SimpleQuantity;
import gov.va.api.health.dataquery.api.datatypes.SimpleResource;
import gov.va.api.health.dataquery.api.elements.BackboneElement;
import gov.va.api.health.dataquery.api.elements.Extension;
import gov.va.api.health.dataquery.api.elements.Meta;
import gov.va.api.health.dataquery.api.elements.Narrative;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.validation.ZeroOrOneOf;
import gov.va.api.health.dataquery.api.validation.ZeroOrOneOfs;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonAutoDetect(
  fieldVisibility = JsonAutoDetect.Visibility.ANY,
  isGetterVisibility = Visibility.NONE
)
@Schema(
  description =
      "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-observationresults.html",
  example = "SWAGGER_EXAMPLE_OBSERVATION"
)
@ZeroOrOneOfs({
  @ZeroOrOneOf(
    fields = {"effectiveDateTime", "effectivePeriod"},
    message = "Only one effective field may be specified"
  ),
  @ZeroOrOneOf(
    fields = {
      "valueAttachment",
      "valueCodeableConcept",
      "valueDateTime",
      "valuePeriod",
      "valueQuantity",
      "valueRange",
      "valueRatio",
      "valueSampledData",
      "valueString",
      "valueTime"
    },
    message = "Only one value field may be specified"
  )
})
public class Observation implements Resource {

  @NotBlank String resourceType;

  @Pattern(regexp = Fhir.ID)
  String id;

  @Valid Meta meta;

  @Pattern(regexp = Fhir.URI)
  String implicitRules;

  @Pattern(regexp = Fhir.CODE)
  String language;

  @Valid Narrative text;
  @Valid List<SimpleResource> contained;
  @Valid List<Extension> extension;
  @Valid List<Extension> modifierExtension;
  @Valid List<Identifier> identifier;
  @NotNull Observation.Status status;
  @Valid @NotNull CodeableConcept category;
  @Valid @NotNull CodeableConcept code;
  @Valid @NotNull Reference subject;
  @Valid Reference encounter;
  @Valid Period effectivePeriod;

  @Pattern(regexp = Fhir.DATETIME)
  String effectiveDateTime;

  @Pattern(regexp = Fhir.INSTANT)
  String issued;

  @Valid List<Reference> performer;
  @Valid Quantity valueQuantity;
  @Valid CodeableConcept valueCodeableConcept;
  String valueString;
  @Valid Range valueRange;
  @Valid Ratio valueRatio;
  @Valid SampledData valueSampledData;
  @Valid Attachment valueAttachment;

  @Pattern(regexp = Fhir.TIME)
  String valueTime;

  @Pattern(regexp = Fhir.DATETIME)
  String valueDateTime;

  @Valid Period valuePeriod;
  @Valid CodeableConcept dataAbsentReason;
  @Valid CodeableConcept interpretation;
  String comments;
  @Valid CodeableConcept bodySite;
  @Valid CodeableConcept method;
  @Valid Reference specimen;
  @Valid Reference device;
  @Valid List<ObservationReferenceRange> referenceRange;
  @Valid List<ObservationRelated> related;
  @Valid List<ObservationComponent> component;

  @JsonIgnore
  @AssertTrue(message = "Category system should be http://hl7.org/fhir/observation-category.")
  private boolean isValidCategory() {
    if (category == null) {
      return true;
    }
    return StringUtils.equals(
        "http://hl7.org/fhir/observation-category", (category.coding().get(0).system()));
  }

  @SuppressWarnings("unused")
  public enum Status {
    registered,
    preliminary,
    amended,
    cancelled,
    unknown,
    @JsonProperty("final")
    _final,
    @JsonProperty("entered-in-error")
    entered_in_error
  }

  @SuppressWarnings("unused")
  public enum Type {
    @JsonProperty("has-member")
    has_member,
    @JsonProperty("derived-from")
    derived_from,
    @JsonProperty("sequel-to")
    sequel_to,
    replaces,
    @JsonProperty("qualified-by")
    qualified_by,
    @JsonProperty("interfered-by")
    interfered_by
  }

  @Data
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonDeserialize(builder = Observation.Bundle.BundleBuilder.class)
  @Schema(name = "ObservationBundle", example = "SWAGGER_EXAMPLE_OBSERVATION_BUNDLE")
  public static class Bundle extends AbstractBundle<Entry> {

    @Builder
    public Bundle(
        @NotBlank String resourceType,
        @Pattern(regexp = Fhir.ID) String id,
        @Valid Meta meta,
        @Pattern(regexp = Fhir.URI) String implicitRules,
        @Pattern(regexp = Fhir.CODE) String language,
        @NotNull BundleType type,
        @Min(0) Integer total,
        @Valid List<BundleLink> link,
        @Valid List<Entry> entry,
        @Valid Signature signature) {
      super(resourceType, id, meta, implicitRules, language, type, total, link, entry, signature);
    }
  }

  @Data
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonDeserialize(builder = Observation.Entry.EntryBuilder.class)
  @Schema(name = "ObservationEntry")
  public static class Entry extends AbstractEntry<Observation> {

    @Builder
    public Entry(
        @Pattern(regexp = Fhir.ID) String id,
        @Valid List<Extension> extension,
        @Valid List<Extension> modifierExtension,
        @Valid List<BundleLink> link,
        @Pattern(regexp = Fhir.URI) String fullUrl,
        @Valid Observation resource,
        @Valid Search search,
        @Valid Request request,
        @Valid Response response) {
      super(id, extension, modifierExtension, link, fullUrl, resource, search, request, response);
    }
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @ZeroOrOneOf(
    fields = {
      "valueAttachment",
      "valueCodeableConcept",
      "valueDateTime",
      "valuePeriod",
      "valueQuantity",
      "valueRange",
      "valueRatio",
      "valueSampledData",
      "valueString",
      "valueTime"
    },
    message = "Only one value value may be specified"
  )
  public static class ObservationComponent implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;

    @Valid @NotNull CodeableConcept code;

    @Valid Quantity valueQuantity;
    @Valid CodeableConcept valueCodeableConcept;
    String valueString;
    @Valid Range valueRange;
    @Valid Ratio valueRatio;
    @Valid SampledData valueSampledData;
    @Valid Attachment valueAttachment;

    @Pattern(regexp = Fhir.TIME)
    String valueTime;

    @Pattern(regexp = Fhir.DATETIME)
    String valueDateTime;

    @Valid Period valuePeriod;

    @Valid CodeableConcept dataAbsentReason;

    @Valid List<ObservationReferenceRange> referenceRange;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class ObservationReferenceRange implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;

    @Valid SimpleQuantity low;
    @Valid SimpleQuantity high;
    @Valid CodeableConcept meaning;
    @Valid Range age;
    String text;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class ObservationRelated implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;

    Type type;

    @Valid @NotNull Reference target;
  }
}
