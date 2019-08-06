package gov.va.api.health.dataquery.service.controller.immunization;

import java.math.BigInteger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ImmunizationRepository
    extends PagingAndSortingRepository<ImmunizationEntity, BigInteger> {
  Page<ImmunizationEntity> findByIcn(String icn, Pageable pageable);
}
