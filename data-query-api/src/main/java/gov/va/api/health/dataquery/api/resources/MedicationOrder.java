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
import gov.va.api.health.dataquery.api.datatypes.Duration;
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
import gov.va.api.health.dataquery.api.validation.ExactlyOneOfs;
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
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@Schema(
  description =
      "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-medicationorder.html",
  example = "SWAGGER_EXAMPLE_MEDICATION_ORDER"
)
@ZeroOrOneOf(
  fields = {"reasonCodeableConcept", "reasonReference"},
  message = "Only one reason field may be specified"
)
@ExactlyOneOfs({
  @ExactlyOneOf(
    fields = {"medicationCodeableConcept", "medicationReference"},
    message = "Exactly one medication field must be specified"
  ),
  @ExactlyOneOf(
    fields = {"prescriber", "_prescriber"},
    message = "Exactly one prescriber field must be specified"
  ),
})
public class MedicationOrder implements Resource {

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

  @NotBlank
  @Pattern(regexp = Fhir.DATETIME)
  String dateWritten;

  @NotNull Status status;

  @Pattern(regexp = Fhir.DATETIME)
  String dateEnded;

  @Valid CodeableConcept reasonEnded;
  @Valid @NotNull Reference patient;
  @Valid Reference prescriber;
  @Valid Extension _prescriber;
  @Valid Reference encounter;
  @Valid CodeableConcept reasonCodeableConcept;
  @Valid Reference reasonReference;
  String note;
  @Valid CodeableConcept medicationCodeableConcept;
  @Valid Reference medicationReference;
  @Valid List<DosageInstruction> dosageInstruction;
  @Valid DispenseRequest dispenseRequest;
  @Valid Substitution substitution;
  @Valid Reference priorPrescription;

  @SuppressWarnings("unused")
  public enum Status {
    active,
    @JsonProperty("on-hold")
    on_hold,
    completed,
    @JsonProperty("entered-in-error")
    entered_in_error,
    stopped,
    draft
  }

  @Data
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonDeserialize(builder = MedicationOrder.Bundle.BundleBuilder.class)
  @Schema(name = "MedicationOrderBundle", example = "SWAGGER_EXAMPLE_MEDICATION_ORDER_BUNDLE")
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
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @ZeroOrOneOf(
    fields = {"medicationCodeableConcept", "medicationReference"},
    message = "Only one medication field may be specified"
  )
  public static class DispenseRequest implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> extension;
    @Valid List<Extension> modifierExtension;

    @Valid CodeableConcept medicationCodeableConcept;
    @Valid Reference medicationReference;
    @Valid Period validityPeriod;

    @Min(1)
    Integer numberOfRepeatsAllowed;

    @Valid SimpleQuantity quantity;
    @Valid Duration expectedSupplyDuration;
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
  public static class DosageInstruction implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> extension;
    @Valid List<Extension> modifierExtension;

    String text;
    @Valid CodeableConcept additionalInstructions;
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
    @Valid Ratio maxDosePerDay;
  }

  @Data
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonDeserialize(builder = MedicationOrder.Entry.EntryBuilder.class)
  @Schema(name = "MedicationOrderEntry")
  public static class Entry extends AbstractEntry<MedicationOrder> {

    @Builder
    public Entry(
        @Pattern(regexp = Fhir.ID) String id,
        @Valid List<Extension> extension,
        @Valid List<Extension> modifierExtension,
        @Valid List<BundleLink> link,
        @Pattern(regexp = Fhir.URI) String fullUrl,
        @Valid MedicationOrder resource,
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
  public static class Substitution implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> extension;
    @Valid List<Extension> modifierExtension;

    @NotNull @Valid CodeableConcept type;
    @Valid CodeableConcept reason;
  }
}
