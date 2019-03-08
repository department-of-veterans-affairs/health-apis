package gov.va.api.health.dataquery.api.resources;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
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
import gov.va.api.health.dataquery.api.datatypes.Signature;
import gov.va.api.health.dataquery.api.datatypes.SimpleResource;
import gov.va.api.health.dataquery.api.elements.BackboneElement;
import gov.va.api.health.dataquery.api.elements.Extension;
import gov.va.api.health.dataquery.api.elements.Meta;
import gov.va.api.health.dataquery.api.elements.Narrative;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.validation.ExactlyOneOf;
import gov.va.api.health.dataquery.api.validation.ZeroOrOneOf;
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
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(
  description =
      "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-diagnosticreport.html",
  example = "SWAGGER_EXAMPLE_DIAGNOSTIC_REPORT"
)
@ExactlyOneOf(fields = {"performer", "_performer"})
@ZeroOrOneOf(
  fields = {"effectiveDateTime", "effectivePeriod"},
  message = "Only one effective value may be specified"
)
public class DiagnosticReport implements Resource {
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

  @NotNull Code status;

  @NotNull @Valid CodeableConcept category;
  @NotNull @Valid CodeableConcept code;
  @NotNull @Valid Reference subject;

  @Valid Reference encounter;

  @Pattern(regexp = Fhir.DATETIME)
  String effectiveDateTime;

  @Valid Period effectivePeriod;

  @Pattern(regexp = Fhir.INSTANT)
  @NotBlank
  String issued;

  @Valid Reference performer;
  @Valid Extension _performer;

  @Valid List<Reference> request;
  @Valid List<Reference> specimen;

  @Valid List<Reference> result;

  @Valid List<Reference> imagingStudy;
  @Valid List<Image> image;

  String conclusion;

  @Valid List<CodeableConcept> codedDiagnosis;
  @Valid List<Attachment> presentedForm;

  @JsonIgnore
  @AssertTrue(
    message = "Category system should be http://hl7.org/fhir/ValueSet/diagnostic-service-sections."
  )
  private boolean isValidCategory() {
    if (category == null) {
      return true;
    }
    if (category.coding() == null || category.coding().isEmpty()) {
      return false;
    }
    return StringUtils.equals(
        "http://hl7.org/fhir/ValueSet/diagnostic-service-sections",
        category.coding().get(0).system());
  }

  @SuppressWarnings("unused")
  public enum Code {
    registered,
    partial,
    @JsonProperty("final")
    _final,
    corrected,
    appended,
    cancelled,
    @JsonProperty("entered-in-error")
    entered_in_error
  }

  @Data
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonDeserialize(builder = DiagnosticReport.Bundle.BundleBuilder.class)
  @Schema(name = "DiagnosticReportBundle", example = "SWAGGER_EXAMPLE_DIAGNOSTIC_REPORT_BUNDLE")
  public static class Bundle extends AbstractBundle<DiagnosticReport.Entry> {
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
  @JsonDeserialize(builder = DiagnosticReport.Entry.EntryBuilder.class)
  @Schema(name = "DiagnosticReportEntry")
  public static class Entry extends AbstractEntry<DiagnosticReport> {
    @Builder
    public Entry(
        @Pattern(regexp = Fhir.ID) String id,
        @Valid List<Extension> extension,
        @Valid List<Extension> modifierExtension,
        @Valid List<BundleLink> link,
        @Pattern(regexp = Fhir.URI) String fullUrl,
        @Valid DiagnosticReport resource,
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
  public static class Image implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    String comment;

    @Valid @NotNull Reference link;
  }
}
