package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Allergy intollerance DB. */
@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface AllergyIntoleranceRepository
    extends PagingAndSortingRepository<AllergyIntoleranceEntity, String>,
        JpaSpecificationExecutor<AllergyIntoleranceEntity> {
  Page<AllergyIntoleranceEntity> findByIcn(String icn, Pageable pageable);
}
