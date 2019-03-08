package gov.va.api.health.dataquery.api.resources;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gov.va.api.health.dataquery.api.Fhir;
import gov.va.api.health.dataquery.api.bundle.AbstractBundle;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.datatypes.Annotation;
import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Identifier;
import gov.va.api.health.dataquery.api.datatypes.Signature;
import gov.va.api.health.dataquery.api.datatypes.SimpleResource;
import gov.va.api.health.dataquery.api.elements.BackboneElement;
import gov.va.api.health.dataquery.api.elements.Extension;
import gov.va.api.health.dataquery.api.elements.Meta;
import gov.va.api.health.dataquery.api.elements.Narrative;
import gov.va.api.health.dataquery.api.elements.Reference;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonAutoDetect(
  fieldVisibility = JsonAutoDetect.Visibility.ANY,
  isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
@Schema(
  description =
      "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-allergyintolerance.html",
  example = "SWAGGER_EXAMPLE_ALLERGY_INTOLERANCE"
)
public class AllergyIntolerance implements Resource {
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

  @Pattern(regexp = Fhir.DATETIME)
  String onset;

  @Pattern(regexp = Fhir.DATETIME)
  String recordedDate;

  @Valid Reference recorder;
  @NotNull @Valid Reference patient;
  @Valid Reference reporter;
  @NotNull @Valid CodeableConcept substance;
  @NotNull Status status;
  Criticality criticality;
  Type type;
  Category category;

  @Pattern(regexp = Fhir.DATETIME)
  String lastOccurence;

  @Valid Annotation note;
  @Valid List<Reaction> reaction;

  @SuppressWarnings("unused")
  public enum Status {
    active,
    unconfirmed,
    confirmed,
    inactive,
    resolved,
    refuted,
    @JsonProperty("entered-in-error")
    entered_in_error
  }

  @SuppressWarnings("unused")
  public enum Criticality {
    CRITL,
    CRITH,
    CRITU
  }

  @SuppressWarnings("unused")
  public enum Type {
    allergy,
    intolerance
  }

  @SuppressWarnings("unused")
  public enum Category {
    food,
    medication,
    environment,
    other
  }

  @SuppressWarnings("unused")
  public enum Certainty {
    unlikely,
    likely,
    confirmed
  }

  @SuppressWarnings("unused")
  public enum Severity {
    mild,
    moderate,
    severe
  }

  @Data
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonDeserialize(builder = AllergyIntolerance.Bundle.BundleBuilder.class)
  @Schema(name = "AllergyIntoleranceBundle", example = "SWAGGER_EXAMPLE_ALLERGY_INTOLERANCE_BUNDLE")
  public static class Bundle extends AbstractBundle<AllergyIntolerance.Entry> {
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
        @Valid List<AllergyIntolerance.Entry> entry,
        @Valid Signature signature) {
      super(resourceType, id, meta, implicitRules, language, type, total, link, entry, signature);
    }
  }

  @Data
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonDeserialize(builder = AllergyIntolerance.Entry.EntryBuilder.class)
  @Schema(name = "AllergyIntoleranceEntry")
  public static class Entry extends AbstractEntry<AllergyIntolerance> {
    @Builder
    public Entry(
        @Pattern(regexp = Fhir.ID) String id,
        @Valid List<Extension> extension,
        @Valid List<Extension> modifierExtension,
        @Valid List<BundleLink> link,
        @Pattern(regexp = Fhir.URI) String fullUrl,
        @Valid AllergyIntolerance resource,
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
  @Schema(name = "AllergyIntoleranceReaction")
  public static class Reaction implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    @Valid CodeableConcept substance;
    Certainty certainty;
    @NotEmpty @Valid List<CodeableConcept> manifestation;
    String description;

    @Pattern(regexp = Fhir.DATETIME)
    String onset;

    Severity severity;
    @Valid CodeableConcept exposureRoute;
    @Valid Annotation note;
  }
}
