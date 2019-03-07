package gov.va.api.health.dataquery.api.resources;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gov.va.api.health.dataquery.api.Fhir;
import gov.va.api.health.dataquery.api.bundle.AbstractBundle;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Identifier;
import gov.va.api.health.dataquery.api.datatypes.Period;
import gov.va.api.health.dataquery.api.datatypes.Range;
import gov.va.api.health.dataquery.api.datatypes.Ratio;
import gov.va.api.health.dataquery.api.datatypes.Signature;
import gov.va.api.health.dataquery.api.datatypes.SimpleQuantity;
import gov.va.api.health.dataquery.api.datatypes.SimpleResource;
import gov.va.api.health.dataquery.api.datatypes.Timing;
import gov.va.api.health.dataquery.api.elements.BackboneElement;
import gov.va.api.health.dataquery.api.elements.Extension;
import gov.va.api.health.dataquery.api.elements.Meta;
import gov.va.api.health.dataquery.api.elements.Narrative;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.validation.ExactlyOneOf;
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
@JsonAutoDetect(
  fieldVisibility = JsonAutoDetect.Visibility.ANY,
  isGetterVisibility = Visibility.NONE
)
@Schema(
  description =
      "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-medicationstatement.html",
  example = "SWAGGER_EXAMPLE_MEDICATION_STATEMENT"
)
@ZeroOrOneOfs({
  @ZeroOrOneOf(
    fields = {"reasonForUseCodeableConcept", "reasonForUseReference"},
    message = "Only one reasonForUse value may be specified"
  ),
  @ZeroOrOneOf(
    fields = {"effectiveDateTime", "effectivePeriod"},
    message = "Only one effective value may be specified"
  )
})
@ExactlyOneOf(
  fields = {"medicationCodeableConcept", "medicationReference"},
  message = "Exactly one medication value must be specified"
)
public class MedicationStatement implements Resource {

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
  @NotNull @Valid Reference patient;
  @Valid Reference informationSource;

  @NotBlank
  @Pattern(regexp = Fhir.DATETIME)
  String dateAsserted;

  @NotNull Status status;
  Boolean wasNotTaken;

  @Valid List<CodeableConcept> reasonNotTaken;

  @Valid CodeableConcept reasonForUseCodeableConcept;
  @Valid Reference reasonForUseReference;

  @Pattern(regexp = Fhir.DATETIME)
  String effectiveDateTime;

  @Valid Period effectivePeriod;
  String note;
  @Valid List<Reference> supportingInformation;

  @Valid CodeableConcept medicationCodeableConcept;
  @Valid Reference medicationReference;
  @Valid List<Dosage> dosage;

  @SuppressWarnings("unused")
  public enum Status {
    active,
    completed,
    @JsonProperty("entered-in-error")
    entered_in_error,
    intended
  }

  @Data
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonDeserialize(builder = MedicationStatement.Bundle.BundleBuilder.class)
  @Schema(
    name = "MedicationStatementBundle",
    example = "SWAGGER_EXAMPLE_MEDICATION_STATEMENT_BUNDLE"
  )
  public static class Bundle extends AbstractBundle<MedicationStatement.Entry> {

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
        @Valid List<MedicationStatement.Entry> entry,
        @Valid Signature signature) {
      super(resourceType, id, meta, implicitRules, language, type, total, link, entry, signature);
    }
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @ZeroOrOneOfs({
    @ZeroOrOneOf(
      fields = {"asNeededBoolean", "asNeededCodeableConcept"},
      message = "Only one asNeeded field may be specified"
    ),
    @ZeroOrOneOf(
      fields = {"siteCodeableConcept", "siteReference"},
      message = "Only one site field may be specified"
    ),
    @ZeroOrOneOf(
      fields = {"doseRange", "doseQuantity"},
      message = "Only one dose field may be specified"
    ),
    @ZeroOrOneOf(
      fields = {"rateRatio", "rateRange"},
      message = "Only one rate field may be specified"
    )
  })
  public static class Dosage implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> extension;
    @Valid List<Extension> modifierExtension;

    String text;
    @Valid Timing timing;
    Boolean asNeededBoolean;
    @Valid CodeableConcept asNeededCodeableConcept;
    @Valid CodeableConcept siteCodeableConcept;
    @Valid Reference siteReference;
    @Valid CodeableConcept route;
    @Valid CodeableConcept method;
    @Valid Range doseRange;
    @Valid SimpleQuantity doseQuantity;
    @Valid Ratio rateRatio;
    @Valid Range rateRange;
    @Valid Ratio maxDosePerPeriod;
  }

  @Data
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonDeserialize(builder = MedicationStatement.Entry.EntryBuilder.class)
  @Schema(name = "MedicationStatementEntry")
  public static class Entry extends AbstractEntry<MedicationStatement> {

    @Builder
    public Entry(
        @Pattern(regexp = Fhir.ID) String id,
        @Valid List<Extension> extension,
        @Valid List<Extension> modifierExtension,
        @Valid List<BundleLink> link,
        @Pattern(regexp = Fhir.URI) String fullUrl,
        @Valid MedicationStatement resource,
        @Valid Search search,
        @Valid Request request,
        @Valid Response response) {
      super(id, extension, modifierExtension, link, fullUrl, resource, search, request, response);
    }
  }
}
