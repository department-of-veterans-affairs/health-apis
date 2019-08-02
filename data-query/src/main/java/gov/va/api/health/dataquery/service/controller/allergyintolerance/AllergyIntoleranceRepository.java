package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AllergyIntoleranceRepository
    extends PagingAndSortingRepository<AllergyIntoleranceEntity, String> {
  Page<AllergyIntoleranceEntity> findAllByIcn(String icn, Pageable pageable);
}
