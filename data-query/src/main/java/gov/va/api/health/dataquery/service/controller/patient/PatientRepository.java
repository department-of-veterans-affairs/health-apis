package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

@Loggable
public interface PatientRepository extends PagingAndSortingRepository<PatientEntity, String> {
  /**
   * A paged search that returns a view of the patients with just the payload column read.
   *
   * @param page The page data to find
   * @return A page of patient payloads
   */
  Page<PatientPayloadDto> findAllProjectedBy(Pageable page);
}
