package gov.va.api.health.dataquery.service.controller.medication;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface MedicationRepository
    extends PagingAndSortingRepository<MedicationEntity, String> {}
