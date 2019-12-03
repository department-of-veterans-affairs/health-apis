package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.repository.PagingAndSortingRepository;

@Loggable
public interface LocationRepository extends PagingAndSortingRepository<LocationEntity, String> {
  //  Page<ConditionEntity> findByIcn(String icn, Pageable pageable);
  //
  //  Page<ConditionEntity> findByIcnAndCategory(String icn, String category, Pageable pageable);
  //
  //  Page<ConditionEntity> findByIcnAndClinicalStatusIn(
  //      String icn, Set<String> clinicalStatus, Pageable pageable);
}
