package gov.va.api.health.dataquery.service.controller.observation;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface ObservationRepository
    extends PagingAndSortingRepository<ObservationEntity, String> {
  // Page<ObservationEntity> findByIcn(String icn, Pageable pageable);
}
