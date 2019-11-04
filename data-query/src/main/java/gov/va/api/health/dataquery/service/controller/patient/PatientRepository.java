package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.dataquery.service.controller.condition.ConditionEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

@Loggable
public interface PatientRepository extends PagingAndSortingRepository<PatientEntity, String> {
	 Page<PatientEntity> findByFamilyAndGender(String family, String gender, Pageable pageable);
}
