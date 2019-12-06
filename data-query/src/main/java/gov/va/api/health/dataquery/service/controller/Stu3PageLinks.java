package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.stu3.api.bundle.BundleLink;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.MultiValueMap;

/**
 * This provides paging links for bundles. It will create links for first, self, and last always. It
 * will conditionally create previous and next links.
 */
public interface Stu3PageLinks {
  /** Create a list of parameters that will contain 3 to 5 values. */
  List<BundleLink> create(LinkConfig config);

  /** Provides direct read link for a given id, e.g. /api/Patient/123. */
  String readLink(String resourcePath, String id);

  @Data
  @Builder
  class LinkConfig {
    /** The resource path without the base URL or port. E.g. /api/Patient/1234 */
    private final String path;

    private final int recordsPerPage;
    private final int page;
    private final int totalRecords;
    private final MultiValueMap<String, String> queryParams;
  }
}
