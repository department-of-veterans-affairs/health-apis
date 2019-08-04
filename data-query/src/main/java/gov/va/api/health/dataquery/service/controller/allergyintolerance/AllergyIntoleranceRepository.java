package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AllergyIntoleranceRepository
    extends PagingAndSortingRepository<AllergyIntoleranceEntity, String> {
  Page<AllergyIntoleranceEntity> findByIcn(Pageable pageable, String icn);

  List<AllergyIntoleranceEntity> findByIcn(String icn);
}
