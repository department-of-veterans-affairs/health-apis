package gov.va.api.health.dataquery.service.controller.patient;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BulkFhirCount {
  String resourceType;
  int count;
  int maxPageSize;
}
