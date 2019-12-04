package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.repository.PagingAndSortingRepository;

@Loggable
public interface LocationRepository extends PagingAndSortingRepository<LocationEntity, String> {}
