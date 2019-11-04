package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.autoconfig.logging.Loggable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

@Loggable
public interface PatientRepository extends PagingAndSortingRepository<PatientSearchEntity, String> {
  Page<PatientSearchEntity> findByFamilyAndGender(String family, String gender, Pageable pageable);

  Page<PatientSearchEntity> findByGivenAndGender(String given, String gender, Pageable pageable);
}
