package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface AllergyIntoleranceRepository
    extends PagingAndSortingRepository<AllergyIntoleranceEntity, String> {
  Page<AllergyIntoleranceEntity> findByIcn(String icn, Pageable pageable);
}
