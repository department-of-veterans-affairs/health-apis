package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.Transformers;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type DiagnosticReport.class or Bundle.class. Extract
 * ICN(s) from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class DiagnosticReportIncludesIcnMajig
    extends AbstractIncludesIcnMajig<
        DiagnosticReport, DiagnosticReport.Entry, DiagnosticReport.Bundle> {
  /** Converts the reference to a Datamart Reference to pull out the patient id. */
  public DiagnosticReportIncludesIcnMajig() {
    super(
        DiagnosticReport.class,
        DiagnosticReport.Bundle.class,
        body -> Stream.ofNullable(Transformers.asReferenceId(body.subject())));
  }
}
