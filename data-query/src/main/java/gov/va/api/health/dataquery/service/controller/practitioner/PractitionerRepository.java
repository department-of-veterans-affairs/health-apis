package gov.va.api.health.dataquery.service.controller.practitioner;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.lighthouse.datamart.CompositeCdwId;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Practitioner DB. */
@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface PractitionerRepository
    extends PagingAndSortingRepository<PractitionerEntity, CompositeCdwId>,
        JpaSpecificationExecutor<PractitionerEntity> {
  Page<PractitionerEntity> findByFamilyNameAndGivenName(
      String familyName, String givenName, Pageable pageable);

  Page<PractitionerEntity> findByNpi(String npi, Pageable pageable);
}
