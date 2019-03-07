package gov.va.api.health.dataquery.api.resources;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gov.va.api.health.dataquery.api.Fhir;
import gov.va.api.health.dataquery.api.bundle.AbstractBundle;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.datatypes.Address;
import gov.va.api.health.dataquery.api.datatypes.Attachment;
import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.ContactPoint;
import gov.va.api.health.dataquery.api.datatypes.HumanName;
import gov.va.api.health.dataquery.api.datatypes.Identifier;
import gov.va.api.health.dataquery.api.datatypes.Period;
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
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
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
  description = "http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-patient.html",
  example = "SWAGGER_EXAMPLE_PATIENT"
)
@ZeroOrOneOfs({
  @ZeroOrOneOf(
    fields = {"deceasedBoolean", "deceasedDateTime"},
    message = "Only one deceased value may be specified"
  ),
  @ZeroOrOneOf(
    fields = {"multipleBirthBoolean", "multipleBirthInteger"},
    message = "Only one multiple birth value may be specified"
  )
})
public class Patient implements Resource {

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
  @NotEmpty @Valid List<Identifier> identifier;
  @Valid List<Extension> modifierExtension;
  Boolean active;
  @NotEmpty @Valid List<HumanName> name;
  @Valid List<ContactPoint> telecom;
  @NotNull Gender gender;

  @Pattern(regexp = Fhir.DATE)
  String birthDate;

  Boolean deceasedBoolean;

  @Pattern(regexp = Fhir.DATETIME)
  String deceasedDateTime;

  @Valid List<Address> address;
  @Valid CodeableConcept maritalStatus;
  Boolean multipleBirthBoolean;
  Integer multipleBirthInteger;
  @Valid List<Attachment> photo;
  @Valid List<Contact> contact;
  @Valid List<Communication> communication;
  @Valid List<Reference> careProvider;
  @Valid Reference managingOrganization;
  @Valid List<PatientLink> link;

  private boolean isValidArgonautExtensionCount(String url, int maxAllowedOmbExtensionCount) {
    if (extension == null) {
      return true;
    }
    Optional<Extension> argonautExtension =
        extension.stream().filter(e -> url.equals(e.url())).findFirst();
    if (!argonautExtension.isPresent()) {
      return true;
    }
    int ombExtensionCount = 0;
    int textExtensionCount = 0;
    for (Extension e : argonautExtension.get().extension()) {
      switch (e.url()) {
        case "ombCategory":
          ombExtensionCount++;
          break;
        case "text":
          textExtensionCount++;
          break;
        default:
          break;
      }
    }
    return ombExtensionCount <= maxAllowedOmbExtensionCount && textExtensionCount == 1;
  }

  @JsonIgnore
  @AssertTrue(message = "Argo-Ethnicity extension is not valid")
  private boolean isValidEthnicityExtension() {
    return isValidArgonautExtensionCount(
        "http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity", 1);
  }

  @JsonIgnore
  @AssertTrue(message = "Argo-Race extension is not valid")
  private boolean isValidRaceExtension() {
    return isValidArgonautExtensionCount(
        "http://fhir.org/guides/argonaut/StructureDefinition/argo-race", 5);
  }

  @SuppressWarnings("unused")
  public enum Gender {
    male,
    female,
    other,
    unknown
  }

  @Data
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonDeserialize(builder = Patient.Bundle.BundleBuilder.class)
  @Schema(name = "PatientBundle", example = "SWAGGER_EXAMPLE_PATIENT_BUNDLE")
  public static class Bundle extends AbstractBundle<Patient.Entry> {

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
  public static class Communication implements BackboneElement {

    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    @NotNull @Valid CodeableConcept language;
    Boolean preferred;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @Schema(name = "PatientContact")
  public static class Contact implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    @Valid List<CodeableConcept> relationship;
    @Valid HumanName name;
    @Valid List<ContactPoint> telecom;
    @Valid Address address;

    Gender gender;

    @Valid Reference organization;
    @Valid Period period;
  }

  @Data
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonDeserialize(builder = Patient.Entry.EntryBuilder.class)
  @Schema(name = "PatientEntry")
  public static class Entry extends AbstractEntry<Patient> {

    @Builder
    public Entry(
        @Pattern(regexp = Fhir.ID) String id,
        @Valid List<Extension> extension,
        @Valid List<Extension> modifierExtension,
        @Valid List<BundleLink> link,
        @Pattern(regexp = Fhir.URI) String fullUrl,
        @Valid Patient resource,
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
  public static class PatientLink implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    @Valid Reference other;

    @Pattern(regexp = Fhir.CODE)
    String type;
  }
}
