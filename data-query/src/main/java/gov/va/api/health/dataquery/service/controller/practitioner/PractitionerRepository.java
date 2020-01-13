package gov.va.api.health.dataquery.service.controller.practitioner;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

@Loggable
public interface PractitionerRepository
    extends PagingAndSortingRepository<PractitionerEntity, String> {
  Page<PractitionerEntity> findByFamilyNameAndGivenName(
      String familyName, String givenName, Pageable pageable);
}
