package gov.va.api.health.dataquery.service.controller.medication;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MedicationRepository
        extends PagingAndSortingRepository<MedicationEntity, String> {
    Page<MedicationEntity> findByCdwId(String cdwId, Pageable pageable);
}

