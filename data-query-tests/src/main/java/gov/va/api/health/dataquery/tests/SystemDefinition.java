package gov.va.api.health.dataquery.tests;

import gov.va.api.health.sentinel.ServiceDefinition;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** Defines all service definitions required for integration tests. */
@Value
@Builder
public final class SystemDefinition {
  @NonNull ServiceDefinition dstu2DataQuery;

  @NonNull ServiceDefinition stu3DataQuery;

  @NonNull ServiceDefinition r4DataQuery;

  @NonNull ServiceDefinition internalDataQuery;

  @NonNull TestIds publicIds;
}
