package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.service.controller.Dstu2Transformers;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Intercept all RequestMapping payloads of Type DiagnosticReport.class or Bundle.class. Extract
 * ICN(s) from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class Dstu2DiagnosticReportIncludesIcnMajig implements ResponseBodyAdvice<Object> {
  @Delegate
  private final IncludesIcnMajig<DiagnosticReport, DiagnosticReport.Bundle> delegate =
      IncludesIcnMajig.<DiagnosticReport, DiagnosticReport.Bundle>builder()
          .type(DiagnosticReport.class)
          .bundleType(DiagnosticReport.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(body -> Stream.ofNullable(Dstu2Transformers.asReferenceId(body.subject())))
          .build();
}
