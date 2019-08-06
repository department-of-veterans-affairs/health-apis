package gov.va.api.health.dataquery.service.controller.medicationstatement;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface MedicationStatementRepository
    extends PagingAndSortingRepository<MedicationStatementEntity, String> {}
