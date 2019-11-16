package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.repository.PagingAndSortingRepository;

@Loggable
public interface PatientRepository extends PagingAndSortingRepository<PatientEntity, String> {}
