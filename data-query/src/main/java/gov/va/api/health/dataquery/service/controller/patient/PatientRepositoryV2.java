package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface PatientRepositoryV2 extends PagingAndSortingRepository<PatientEntityV2, String> {
  Page<PatientEntityV2> findByIcn(String icn, Pageable pageable);
}
