package gov.va.api.health.dataquery.tools.minimart.transformers;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.service.controller.Transformers;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReports;
import gov.va.api.health.dataquery.tools.minimart.RevealSecretIdentity;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class F2DDiagnosticReportTransformer {

  RevealSecretIdentity fauxIds;

  public DatamartDiagnosticReports fhirToDatamart(DiagnosticReport diagnosticReport) {
    return DatamartDiagnosticReports.builder()
        .fullIcn(
            fauxIds.unmask("Patient", splitReferenceForId(diagnosticReport.subject().reference())))
        .patientName(diagnosticReport.subject().display())
        .reports(reports(diagnosticReport))
        .build();
  }

  public List<DatamartDiagnosticReports.DiagnosticReport> reports(
      DiagnosticReport diagnosticReport) {
    return Transformers.emptyToNull(
        asList(
            DatamartDiagnosticReports.DiagnosticReport.builder()
                .identifier(fauxIds.unmask("DiagnosticReport", diagnosticReport.id()))
                .effectiveDateTime(diagnosticReport.effectiveDateTime())
                .issuedDateTime(diagnosticReport.issued())
                .accessionInstitutionSid(
                    diagnosticReport.performer() != null
                        ? fauxIds.unmask(
                            splitReferenceForType(diagnosticReport.performer().reference()),
                            splitReferenceForId(diagnosticReport.performer().reference()))
                        : null)
                .accessionInstitutionName(
                    diagnosticReport.performer() != null
                        ? diagnosticReport.performer().display()
                        : null)
                .build()));
  }

  private String splitReferenceForId(String reference) {
    String[] splitRef = reference.split("/");
    return splitRef[splitRef.length - 1];
  }

  private String splitReferenceForType(String reference) {
    String[] splitRef = reference.split("/");
    return splitRef[splitRef.length - 2];
  }
}
