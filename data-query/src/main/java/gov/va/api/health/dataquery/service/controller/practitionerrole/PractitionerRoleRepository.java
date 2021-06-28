package gov.va.api.health.dataquery.service.controller.practitionerrole;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface PractitionerRoleRepository
    extends PagingAndSortingRepository<PractitionerRoleEntity, CompositeCdwId>,
        JpaSpecificationExecutor<PractitionerRoleEntity> {
  Page<PractitionerRoleEntity> findByFamilyNameAndGivenName(
      String familyName, String givenName, Pageable pageable);

  Page<PractitionerRoleEntity> findByNpi(String npi, Pageable pageable);
}