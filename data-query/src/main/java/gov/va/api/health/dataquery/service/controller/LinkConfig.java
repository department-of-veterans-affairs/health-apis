package gov.va.api.health.dataquery.service.controller;

import lombok.Builder;
import lombok.Value;
import org.springframework.util.MultiValueMap;

@Value
@Builder
public class LinkConfig {
  /** The resource path without the base URL or port. E.g. /api/Patient/1234 */
  private final String path;

  private final int recordsPerPage;

  private final int page;

  private final int totalRecords;

  private final MultiValueMap<String, String> queryParams;
}
