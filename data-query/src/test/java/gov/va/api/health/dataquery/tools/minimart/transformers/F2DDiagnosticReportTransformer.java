package gov.va.api.health.dataquery.tools.minimart.transformers;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.service.controller.Transformers;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReports;
import gov.va.api.health.dataquery.tools.minimart.FhirToDatamartUtils;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.List;
import java.util.stream.Collectors;

public class F2DDiagnosticReportTransformer {

  public DatamartDiagnosticReports fhirToDatamart(DiagnosticReport diagnosticReport) {
    return DatamartDiagnosticReports.builder()
        .fullIcn(
            FhirToDatamartUtils.revealSecretIdentity(
                FhirToDatamartUtils.getReferenceIdentifier(diagnosticReport.subject().reference())))
        .patientName(diagnosticReport.subject().display())
        .reports(reports(diagnosticReport))
        .build();
  }

  public List<DatamartDiagnosticReports.DiagnosticReport> reports(
      DiagnosticReport diagnosticReport) {
    String performer =
        diagnosticReport.performer() != null && diagnosticReport.performer().reference() != null
            ? FhirToDatamartUtils.revealSecretIdentity(
                FhirToDatamartUtils.getReferenceIdentifier(
                    diagnosticReport.performer().reference()))
            : null;
    return Transformers.emptyToNull(
        asList(
            DatamartDiagnosticReports.DiagnosticReport.builder()
                .identifier(FhirToDatamartUtils.revealSecretIdentity(diagnosticReport.id()))
                .effectiveDateTime(diagnosticReport.effectiveDateTime())
                .issuedDateTime(diagnosticReport.issued())
                .accessionInstitutionSid(performer)
                .accessionInstitutionName(
                    performer != null ? diagnosticReport.performer().display() : null)
                .results(results(diagnosticReport.result()))
                .build()));
  }

  private List<DatamartDiagnosticReports.Result> results(List<Reference> results) {
    if (results == null || results.isEmpty()) {
      return null;
    }
    return results
        .stream()
        .map(
            r ->
                DatamartDiagnosticReports.Result.builder()
                    .result(
                        FhirToDatamartUtils.revealSecretIdentity(
                            FhirToDatamartUtils.getReferenceIdentifier(r.reference())))
                    .display(r.display())
                    .build())
        .collect(Collectors.toList());
  }
}
