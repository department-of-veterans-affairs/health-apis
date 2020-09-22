package gov.va.api.health.dataquery.service.controller.etlstatus;


import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;


@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface LatestResourceEtlStatusRepository extends
        PagingAndSortingRepository<LatestResourceEtlStatusEntity, String> {
    Page<LatestResourceEtlStatusEntity> findByResourceName(String resourceName, Pageable pageable);



    @Query("select e.EndDateTimeUTC from #{#entityName} e")
    Instant findTimes();

}
