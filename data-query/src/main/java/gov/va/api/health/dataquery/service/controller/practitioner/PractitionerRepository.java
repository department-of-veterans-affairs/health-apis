package gov.va.api.health.dataquery.service.controller.practitioner;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

@Loggable
public interface PractitionerRepository
    extends PagingAndSortingRepository<PractitionerEntity, String>,
        JpaSpecificationExecutor<PractitionerEntity> {
  // Page<PractitionerEntity> findByFamilyAndGiven(String name, Pageable pageable);

}
