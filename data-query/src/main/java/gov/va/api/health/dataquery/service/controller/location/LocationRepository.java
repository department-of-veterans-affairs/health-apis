package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

@Loggable
public interface LocationRepository extends PagingAndSortingRepository<LocationEntity, String> {
  Page<LocationEntity> findByName(String name, Pageable pageable);

  Page<LocationEntity> findByStreetAndCityAndStateAndPostalCode(
      String street, String city, String state, String postalCode, Pageable pageable);
}
