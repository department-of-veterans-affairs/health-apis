package gov.va.api.health.dataquery.service.controller.appointment;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface AppointmentRepository
    extends PagingAndSortingRepository<AppointmentEntity, CompositeCdwId>,
        JpaSpecificationExecutor<AppointmentEntity> {}
