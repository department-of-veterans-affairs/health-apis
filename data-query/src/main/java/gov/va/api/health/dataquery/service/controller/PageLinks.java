package gov.va.api.health.dataquery.service.controller;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.springframework.util.MultiValueMap;

/**
 * This provides paging links for bundles. It will create links for first, self, and last always. It
 * will conditionally create previous and next links.
 */
public interface PageLinks {
  /** Create a list of parameters that will contain 3 to 5 values. */
  List<gov.va.api.health.dstu2.api.bundle.BundleLink> dstu2Links(LinkConfig config);

  /** Provides direct read link for a given id, e.g. /api/dstu2/Patient/123. */
  String dstu2ReadLink(String resourcePath, String id);

  /** Create a list of parameters that will contain 3 to 5 values. */
  List<gov.va.api.health.r4.api.bundle.BundleLink> r4Links(LinkConfig config);

  /** Provides direct read link for a given id, e.g. /r4/Patient/123. */
  String r4ReadLink(String resourcePath, String id);

  /** Create a list of parameters that will contain 3 to 5 values. */
  List<gov.va.api.health.stu3.api.bundle.BundleLink> stu3Links(LinkConfig config);

  /** Provides direct read link for a given id, e.g. /api/stu3/Patient/123. */
  String stu3ReadLink(String resourcePath, String id);

  @Value
  @Builder
  final class LinkConfig {
    /** The resource path without the base URL or port. E.g. /api/Patient/1234 */
    private final String path;

    private final int recordsPerPage;

    private final int page;

    private final int totalRecords;

    private final MultiValueMap<String, String> queryParams;
  }
}
