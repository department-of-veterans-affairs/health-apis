package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import org.springframework.data.repository.CrudRepository;

public interface DiagnosticReportCrudRepository
    extends CrudRepository<DiagnosticReportEntity, Long> {}
