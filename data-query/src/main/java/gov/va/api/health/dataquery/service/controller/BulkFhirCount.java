package gov.va.api.health.dataquery.service.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** Basic metadata for bulk FHIR. */
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BulkFhirCount {
  String resourceType;
  long count;
  int maxRecordsPerPage;
}
