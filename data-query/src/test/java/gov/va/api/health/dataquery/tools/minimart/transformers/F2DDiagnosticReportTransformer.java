package gov.va.api.health.dataquery.tools.minimart.transformers;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.service.controller.Transformers;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReports;
import gov.va.api.health.dataquery.tools.minimart.RevealSecretIdentity;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.List;
import java.util.stream.Collectors;

public class F2DDiagnosticReportTransformer {

  public DatamartDiagnosticReports fhirToDatamart(DiagnosticReport diagnosticReport) {
    return DatamartDiagnosticReports.builder()
        .fullIcn(
            RevealSecretIdentity.unmask(splitReference(diagnosticReport.subject().reference())))
        .patientName(diagnosticReport.subject().display())
        .reports(reports(diagnosticReport))
        .build();
  }

  private List<DatamartDiagnosticReports.Order> orders(List<Reference> results) {
    if (results == null || results.isEmpty()) {
      return null;
    }
    return results
        .stream()
        .map(
            r ->
                DatamartDiagnosticReports.Order.builder()
                    .sid(RevealSecretIdentity.unmask(splitReference(r.reference())))
                    .display(r.display())
                    .build())
        .collect(Collectors.toList());
  }

  public List<DatamartDiagnosticReports.DiagnosticReport> reports(
      DiagnosticReport diagnosticReport) {

    String performer =
        diagnosticReport.performer() != null && diagnosticReport.performer().reference() != null
            ? RevealSecretIdentity.unmask(splitReference(diagnosticReport.performer().reference()))
            : null;

    return Transformers.emptyToNull(
        asList(
            DatamartDiagnosticReports.DiagnosticReport.builder()
                .identifier(RevealSecretIdentity.unmask(diagnosticReport.id()))
                .effectiveDateTime(diagnosticReport.effectiveDateTime())
                .issuedDateTime(diagnosticReport.issued())
                .accessionInstitutionSid(performer)
                .accessionInstitutionName(
                    performer != null ? diagnosticReport.performer().display() : null)
                .orders(orders(diagnosticReport.result()))
                .build()));
  }

  public String splitReference(String reference) {
    String[] splitRef = reference.split("/");
    return splitRef[splitRef.length - 1];
  }
}
