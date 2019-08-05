package gov.va.api.health.dataquery.service.controller.immunization;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ImmuizationRepository
    extends PagingAndSortingRepository<ImmunizationEntity, String> {
  Page<ImmunizationEntity> findByIcn(String icn, Pageable pageable);
}
