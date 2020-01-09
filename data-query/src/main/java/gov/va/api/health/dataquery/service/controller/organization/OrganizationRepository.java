package gov.va.api.health.dataquery.service.controller.organization;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.repository.PagingAndSortingRepository;

@Loggable
public interface OrganizationRepository
    extends PagingAndSortingRepository<OrganizationEntity, String> {}
