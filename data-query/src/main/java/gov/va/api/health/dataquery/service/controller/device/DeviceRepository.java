package gov.va.api.health.dataquery.service.controller.device;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Interact with the database and get device entities. */
@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface DeviceRepository
    extends PagingAndSortingRepository<DeviceEntity, String>,
        JpaSpecificationExecutor<DeviceEntity> {
  Page<DeviceEntity> findByIcn(String icn, Pageable pageable);
}
