package gov.va.api.health.dataquery.service.controller.medicationstatement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MedicationStatementRepository
    extends PagingAndSortingRepository<MedicationStatementEntity, String> {
  Page<MedicationStatementEntity> findByIcn(String icn, Pageable pageable);
}
