package gov.va.api.health.dataquery.api.bundle;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import gov.va.api.health.dataquery.api.Fhir;
import gov.va.api.health.dataquery.api.elements.BackboneElement;
import gov.va.api.health.dataquery.api.elements.Extension;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class BundleLink implements BackboneElement {
  @Pattern(regexp = Fhir.ID)
  String id;

  @Valid List<Extension> extension;
  @Valid List<Extension> modifierExtension;

  @NotNull LinkRelation relation;

  @NotBlank
  @Pattern(regexp = Fhir.URI)
  String url;

  @SuppressWarnings("unused")
  public enum LinkRelation {
    about,
    alternate,
    appendix,
    archives,
    author,
    @JsonProperty("blocked-by")
    blocked_by,
    bookmark,
    canonical,
    chapter,
    @JsonProperty("cite-as")
    cite_as,
    collection,
    contents,
    convertedFrom,
    copyright,
    @JsonProperty("create-form")
    create_form,
    current,
    describedby,
    describes,
    disclosure,
    @JsonProperty("dns-prefetch")
    dns_prefetch,
    duplicate,
    edit,
    @JsonProperty("edit-form")
    edit_form,
    @JsonProperty("edit-media")
    edit_media,
    enclosure,
    first,
    glossary,
    help,
    hosts,
    hub,
    icon,
    index,
    item,
    last,
    @JsonProperty("latest-version")
    latest_version,
    license,
    lrdd,
    memento,
    monitor,
    @JsonProperty("monitor-group")
    monitor_group,
    next,
    @JsonProperty("next-archive")
    next_archive,
    nofollow,
    noreferrer,
    original,
    payment,
    pingback,
    preconnect,
    @JsonProperty("predecessor-version")
    predecessor_version,
    prefetch,
    preload,
    prerender,
    prev,
    preview,
    previous,
    @JsonProperty("prev-archive")
    prev_archive,
    @JsonProperty("privacy-policy")
    privacy_policy,
    profile,
    related,
    restconf,
    replies,
    search,
    section,
    self,
    service,
    start,
    stylesheet,
    subsection,
    @JsonProperty("successor-version")
    successor_version,
    tag,
    @JsonProperty("terms-of-service")
    terms_of_service,
    timegate,
    timemap,
    type,
    up,
    @JsonProperty("version-history")
    version_history,
    via,
    webmention,
    @JsonProperty("working-copy")
    working_copy,
    @JsonProperty("working-copy-of")
    working_copy_of
  }
}
