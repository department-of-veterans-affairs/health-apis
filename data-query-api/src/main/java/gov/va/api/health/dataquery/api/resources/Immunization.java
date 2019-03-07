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
import gov.va.api.health.dataquery.api.datatypes.SimpleQuantity;
import gov.va.api.health.dataquery.api.datatypes.SimpleResource;
import gov.va.api.health.dataquery.api.elements.BackboneElement;
import gov.va.api.health.dataquery.api.elements.Extension;
import gov.va.api.health.dataquery.api.elements.Meta;
import gov.va.api.health.dataquery.api.elements.Narrative;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.validation.ExactlyOneOf;
import gov.va.api.health.dataquery.api.validation.ExactlyOneOfs;
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
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(
  description = "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-immunization.html",
  example = "SWAGGER_EXAMPLE_IMMUNIZATION"
)
@ExactlyOneOfs({
  @ExactlyOneOf(fields = {"status", "_status"}),
  @ExactlyOneOf(fields = {"reported", "_reported"})
})
public class Immunization implements Resource {

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
  @Valid Status status;
  @Valid Extension _status;

  @NotBlank
  @Pattern(regexp = Fhir.DATETIME)
  String date;

  @NotNull @Valid CodeableConcept vaccineCode;
  @NotNull @Valid Reference patient;
  @NotNull Boolean wasNotGiven;
  Boolean reported;
  @Valid Extension _reported;
  @Valid Reference performer;
  @Valid Reference requester;
  @Valid Reference encounter;
  @Valid Reference manufacturer;
  @Valid Reference location;
  String lotNumber;

  @Pattern(regexp = Fhir.DATE)
  String expirationDate;

  @Valid CodeableConcept site;
  @Valid CodeableConcept route;
  @Valid SimpleQuantity doseQuantity;
  @Valid List<Annotation> note;
  @Valid Explanation explanation;
  @Valid List<Reaction> reaction;
  @Valid List<VaccinationProtocol> vaccinationProtocol;

  @SuppressWarnings("unused")
  public enum Status {
    @JsonProperty("in-progress")
    in_progress,
    @JsonProperty("on-hold")
    on_hold,
    completed,
    @JsonProperty("entered-in-error")
    entered_in_error,
    stopped
  }

  @Data
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonDeserialize(builder = Immunization.Bundle.BundleBuilder.class)
  @Schema(name = "ImmunizationBundle", example = "SWAGGER_EXAMPLE_IMMUNIZATION_BUNDLE")
  public static class Bundle extends AbstractBundle<Immunization.Entry> {

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
  @JsonDeserialize(builder = Immunization.Entry.EntryBuilder.class)
  @Schema(name = "ImmunizationEntry")
  public static class Entry extends AbstractEntry<Immunization> {

    @Builder
    public Entry(
        @Pattern(regexp = Fhir.ID) String id,
        @Valid List<Extension> extension,
        @Valid List<Extension> modifierExtension,
        @Valid List<BundleLink> link,
        @Pattern(regexp = Fhir.URI) String fullUrl,
        @Valid Immunization resource,
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
  public static class Explanation implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> extension;
    @Valid List<Extension> modifierExtension;
    @Valid List<CodeableConcept> reason;
    @Valid List<CodeableConcept> reasonNotGiven;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @Schema(name = "ImmunizationReaction")
  public static class Reaction implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> extension;
    @Valid List<Extension> modifierExtension;

    @Pattern(regexp = Fhir.DATETIME)
    String date;

    @Valid Reference detail;
    Boolean reported;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class VaccinationProtocol implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> extension;
    @Valid List<Extension> modifierExtension;

    @NotNull
    @Min(1)
    Integer doseSequence;

    String description;
    @Valid Reference authority;
    String series;

    @Min(1)
    Integer seriesDoses;

    @NotEmpty @Valid List<CodeableConcept> targetDisease;
    @NotNull @Valid CodeableConcept doseStatus;
    @Valid CodeableConcept doseStatusReason;
  }
}
