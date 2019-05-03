package gov.va.api.health.dataquery.service.api.conformance;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import gov.va.api.health.dstu2.api.Fhir;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.datatypes.SimpleResource;
import gov.va.api.health.dstu2.api.elements.BackboneElement;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Meta;
import gov.va.api.health.dstu2.api.elements.Narrative;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.Resource;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
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
@Schema(description = "http://hl7.org/fhir/DSTU2/conformance.html")
public class Conformance implements Resource {
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

  @Pattern(regexp = Fhir.URI)
  String url;

  String version;
  String name;
  Status status;
  Boolean experimental;
  String publisher;
  @Valid List<Contact> contact;

  @NotBlank
  @Pattern(regexp = Fhir.DATETIME)
  String date;

  String description;
  String requirements;
  String copyright;
  @NotNull Kind kind;
  @Valid Software software;
  @Valid Implementation implementation;
  @NotBlank String fhirVersion;
  @NotNull AcceptUnknown acceptUnknown;

  @NotNull
  @Size(min = 1)
  List<@Pattern(regexp = Fhir.CODE) String> format;

  @Valid List<Reference> profile;
  @Valid List<Rest> rest;
  @Valid List<Messaging> messaging;
  @Valid List<Document> document;

  @SuppressWarnings("unused")
  public enum AcceptUnknown {
    no,
    extensions,
    elements,
    both
  }

  @SuppressWarnings("unused")
  public enum Kind {
    instance,
    capability,
    requirements
  }

  @SuppressWarnings("unused")
  public enum Status {
    draft,
    active,
    retired
  }

  @SuppressWarnings("unused")
  public enum RestMode {
    client,
    server
  }

  @SuppressWarnings("unused")
  public enum DocumentMode {
    producer,
    consumer
  }

  @SuppressWarnings("unused")
  public enum SearchParamType {
    number,
    date,
    string,
    token,
    reference,
    composite,
    quantity,
    uri
  }

  @SuppressWarnings("unused")
  public enum SearchParamModifier {
    missing,
    exact,
    contains,
    not,
    text,
    in,
    @JsonProperty("not-in")
    not_in,
    below,
    above,
    type
  }

  @SuppressWarnings("unused")
  public enum ResourceInteractionCode {
    read,
    vread,
    update,
    delete,
    @JsonProperty("history-instance")
    history_instance,
    validate,
    @JsonProperty("history-type")
    history_type,
    create,
    @JsonProperty("search-type")
    search_type
  }

  @SuppressWarnings("unused")
  public enum RestResourceVersion {
    @JsonProperty("no-version")
    no_version,
    versioned,
    @JsonProperty("versioned-update")
    versioned_update
  }

  @SuppressWarnings("unused")
  public enum RestInteractionCode {
    transaction,
    @JsonProperty("search-system")
    search_system,
    @JsonProperty("history-system")
    history_system
  }

  @SuppressWarnings("unused")
  public enum RestTransactionMode {
    @JsonProperty("not-supported")
    not_supported,
    batch,
    transaction,
    both
  }

  @SuppressWarnings("unused")
  public enum MessagingEventCategory {
    Consequence,
    Currency,
    Notification
  }

  @SuppressWarnings("unused")
  public enum MessagingEventMode {
    sender,
    receiver
  }

  @SuppressWarnings("unused")
  public enum DeleteCode {
    @JsonProperty("not-supported")
    not_supported,
    single,
    multiple
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @Schema(name = "ConformanceContact")
  public static class Contact implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    String name;
    @Valid List<ContactPoint> telecom;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class Document implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    @NotNull DocumentMode mode;
    String documentation;
    @NotNull @Valid Reference profile;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class Implementation implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    @NotBlank String description;

    @Pattern(regexp = Fhir.URI)
    String url;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class Messaging implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    @Valid List<MessagingEndpoint> endpoint;

    @Min(0)
    Integer reliableCache;

    String documentation;

    @NotEmpty
    @Size(min = 1)
    @Valid
    List<MessagingEvent> event;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class MessagingEndpoint implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    @NotNull @Valid Coding protocol;

    @NotEmpty
    @Pattern(regexp = Fhir.URI)
    String address;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class MessagingEvent implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    @NotNull @Valid Coding code;
    @Valid MessagingEventCategory category;
    @NotNull @Valid MessagingEventMode mode;

    @NotBlank
    @Pattern(regexp = Fhir.CODE)
    String focus;

    @NotNull @Valid Reference request;
    @NotNull @Valid Reference response;
    String documentation;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class ResourceInteraction implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    @NotNull ResourceInteractionCode code;
    String documentation;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class Rest implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;

    @NotNull RestMode mode;
    String documentation;

    @Valid RestSecurity security;

    @NotEmpty
    @Size(min = 1)
    @Valid
    List<RestResource> resource;

    @Valid List<RestInteraction> interaction;
    RestTransactionMode transactionMode;
    @Valid List<SearchParam> searchParam;
    @Valid List<RestOperation> operation;
    List<String> compartment;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class RestInteraction implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    @NotNull RestInteractionCode code;
    String documentation;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class RestOperation implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    @NotBlank String name;
    @NotNull @Valid Reference definition;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class RestResource implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;

    @NotBlank
    @Pattern(regexp = Fhir.CODE)
    String type;

    @Valid Reference profile;

    @NotEmpty
    @Size(min = 1)
    @Valid
    List<ResourceInteraction> interaction;

    RestResourceVersion versioning;
    Boolean readHistory;
    Boolean updateCreate;
    Boolean conditionalCreate;
    Boolean conditionalUpdate;
    DeleteCode conditionalDelete;
    List<String> searchInclude;
    List<String> searchRevInclude;
    @Valid List<SearchParam> searchParam;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class RestSecurity implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    Boolean cors;
    @Valid List<CodeableConcept> service;
    String description;
    @Valid List<SecurityCertificate> certificate;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class SearchParam implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    @NotBlank String name;

    @Pattern(regexp = Fhir.URI)
    String definition;

    @NotNull SearchParamType type;
    String documentation;
    List<@Pattern(regexp = Fhir.CODE) String> target;
    @Valid List<SearchParamModifier> modifier;
    List<String> chain;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class SecurityCertificate implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;

    @Pattern(regexp = Fhir.CODE)
    String type;

    @Pattern(regexp = Fhir.BASE64)
    String blob;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class Software implements BackboneElement {
    @Pattern(regexp = Fhir.ID)
    String id;

    @Valid List<Extension> modifierExtension;
    @Valid List<Extension> extension;
    @NotBlank String name;
    String version;

    @Pattern(regexp = Fhir.DATETIME)
    String releaseDate;
  }
}
