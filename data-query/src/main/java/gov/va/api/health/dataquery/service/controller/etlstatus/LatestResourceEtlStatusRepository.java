package gov.va.api.health.dataquery.service.controller.etlstatus;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface LatestResourceEtlStatusRepository
    extends CrudRepository<LatestResourceEtlStatusEntity, String> {}
