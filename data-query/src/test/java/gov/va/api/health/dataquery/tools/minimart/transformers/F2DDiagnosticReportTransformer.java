package gov.va.api.health.dataquery.tools.minimart.transformers;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.service.controller.Transformers;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReports;
import gov.va.api.health.dataquery.tools.minimart.RevealSecretIdentity;
import java.util.List;

public class F2DDiagnosticReportTransformer {

  public DatamartDiagnosticReports fhirToDatamart(DiagnosticReport diagnosticReport) {
    return DatamartDiagnosticReports.builder()
        .fullIcn(
            RevealSecretIdentity.unmask(splitReference(diagnosticReport.subject().reference())))
        .patientName(diagnosticReport.subject().display())
        .reports(reports(diagnosticReport))
        .build();
  }

  public List<DatamartDiagnosticReports.DiagnosticReport> reports(
      DiagnosticReport diagnosticReport) {
    return Transformers.emptyToNull(
        asList(
            DatamartDiagnosticReports.DiagnosticReport.builder()
                .identifier(RevealSecretIdentity.unmask(diagnosticReport.id()))
                .effectiveDateTime(diagnosticReport.effectiveDateTime())
                .issuedDateTime(diagnosticReport.issued())
                .accessionInstitutionSid(
                    diagnosticReport.performer() != null
                        ? RevealSecretIdentity.unmask(
                            splitReference(diagnosticReport.performer().reference()))
                        : null)
                .accessionInstitutionName(
                    diagnosticReport.performer() != null
                        ? diagnosticReport.performer().display()
                        : null)
                .build()));
  }

  public String splitReference(String reference) {
    String[] splitRef = reference.split("/");
    return splitRef[splitRef.length - 1];
  }
}
