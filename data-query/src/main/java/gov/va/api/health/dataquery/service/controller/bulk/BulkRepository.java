package gov.va.api.health.dataquery.service.controller.bulk;

import gov.va.api.health.dataquery.service.controller.datamart.HasPayload;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BulkRepository<DTO extends HasPayload<?>> {
  Page<DTO> findAllProjectedBy(Pageable page);

  Page<DTO> findByLastUpdatedGreaterThan(Instant lastUpdated, Pageable page);
}
