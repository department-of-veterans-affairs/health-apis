package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.repository.PagingAndSortingRepository;

@Loggable
public interface MedicationRepository
    extends PagingAndSortingRepository<MedicationEntity, String> {}
