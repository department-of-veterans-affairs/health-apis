package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DiagnosticReportCrudRepository
    extends PagingAndSortingRepository<DiagnosticReportEntity, Long> {

  //	  List<ResourceIdentityDetail> findBySystemAndResourceAndIdentifier(
  //		      String system, String resource, String identifier);
  //

  List<DiagnosticReportEntity> findByIdentifier(String identifier);

  //	Iterable<T> findAll(Sort sort);
  //
  //	Page<T> findAll(Pageable pageable);

  //	Sort sort = new Sort(new Sort.Order(Direction.ASC, "lastName"));
  //	Pageable pageable = new PageRequest(0, 5, sort);
}
