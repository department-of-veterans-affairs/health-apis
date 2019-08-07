package gov.va.api.health.dataquery.service.controller.procedure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ProcedureRepository extends PagingAndSortingRepository<ProcedureEntity, String> {
  Page<ProcedureEntity> findByIcn(String icn, Pageable pageable);
}
