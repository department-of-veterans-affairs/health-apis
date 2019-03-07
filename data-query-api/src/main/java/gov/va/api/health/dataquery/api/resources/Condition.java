package gov.va.api.health.dataquery.api.resources;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gov.va.api.health.dataquery.api.Fhir;
import gov.va.api.health.dataquery.api.bundle.AbstractBundle;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.datatypes.Age;
import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Identifier;
import gov.va.api.health.dataquery.api.datatypes.Period;
import gov.va.api.health.dataquery.api.datatypes.Range;
import gov.va.api.health.dataquery.api.datatypes.Signature;
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

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(
  description = "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-condition.html",
  example = "SWAGGER_EXAMPLE_CONDITION"
)
@ZeroOrOneOfs({
  @ZeroOrOneOf(
    fields = {"onsetDateTime", "onsetAge", "onsetPeriod", "onsetRange", "onsetString"},
    message = "Only one onset value may be specified"
  ),
  @ZeroOrOneOf(
    fields = {
      "abatementDateTime",
      "abatementAge",
      "abatementBoolean",
      "abatementPeriod",
      "abatementRange",
      "abatementString"
    },
    message = "Only one abatement value may be specified"
  )
})
public class Condition implements Resource {

  @Pattern(regexp = Fhir.ID)
  String id;

  @NotBlank String resourceType;
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

  @Valid @NotNull Reference patient;
  @Valid Reference encounter;
  @Valid Reference asserter;

  @Pattern(regexp = Fhir.DATE)
  String dateRecorded;

  @Valid @NotNull CodeableConcept code;
  @Valid @NotNull CodeableConcept category;
  @Valid ClinicalStatusCode clinicalStatus;
  @Valid @NotNull VerificationStatusCode verificationStatus;
  @Valid CodeableConcept severity;

  @Pattern(regexp = Fhir.DATETIME)
  String onsetDateTime;

  @Valid Age onsetAge;
  @Valid Period onsetPeriod;
  @Valid Range onsetRange;
  String onsetString;

  @Pattern(regexp = Fhir.DATETIME)
  String abatementDateTime;

  @Valid Age abatementAge;
  Boolean abatementBoolean;
  @Valid Period abatementPeriod;
  @Valid Range abatementRange;
  String abatementString;

  @Valid Stage stage;

  @Valid List<Evidence> evidence;

  @Valid List<CodeableConcept> bodySite;
  String notes;

  @SuppressWarnings("unused")
  public enum ClinicalStatusCode {
    active,
    relapse,
    remission,
    resolved
  }

  @SuppressWarnings("unused")
  public enum VerificationStatusCode {
    provisional,
    differential,
    confirmed,
    refuted,
    @JsonProperty("entered-in-error")
    entered_in_error,
    unknown
  }

  @Data
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonDeserialize(builder = Condition.Bundle.BundleBuilder.class)
  @Schema(name = "ConditionBundle", example = "SWAGGER_EXAMPLE_CONDITION_BUNDLE")
  public static class Bundle extends AbstractBundle<Condition.Entry> {

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
  @JsonDeserialize(builder = Condition.Entry.EntryBuilder.class)
  @Schema(name = "ConditionEntry")
  public static class Entry extends AbstractEntry<Condition> {

    @Builder
    public Entry(
        @Pattern(regexp = Fhir.ID) String id,
        @Valid List<Extension> extension,
        @Valid List<Extension> modifierExtension,
        @Valid List<BundleLink> link,
        @Pattern(regexp = Fhir.URI) String fullUrl,
        @Valid Condition resource,
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
  public static class Evidence implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;

    @Valid CodeableConcept code;
    @Valid List<Reference> detail;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class Stage implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;

    @Valid CodeableConcept summary;
    @Valid List<Reference> assessment;
  }
}
