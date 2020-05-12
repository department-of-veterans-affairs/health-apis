package gov.va.api.health.dataquery.tests;

import gov.va.api.health.sentinel.ServiceDefinition;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public final class SystemDefinition {
  @NonNull ServiceDefinition ids;

  @NonNull ServiceDefinition dstu2DataQuery;

  @NonNull ServiceDefinition stu3DataQuery;

  @NonNull ServiceDefinition r4DataQuery;

  @NonNull ServiceDefinition internalDataQuery;

  @NonNull TestIds cdwIds;
}
