package gov.va.api.health.sentinel;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public final class SystemDefinition {
  @NonNull ServiceDefinition ids;

  @NonNull ServiceDefinition mrAnderson;

  @NonNull ServiceDefinition dataQuery;

  @NonNull TestIds cdwIds;
}
