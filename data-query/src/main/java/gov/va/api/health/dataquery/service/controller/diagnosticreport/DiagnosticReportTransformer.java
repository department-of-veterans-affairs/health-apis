package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.convertAll;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.ifPresent;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dstu2.api.DataAbsentReason;
import gov.va.api.health.dstu2.api.DataAbsentReason.Reason;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport.CdwResults;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReportCategory;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReportCategoryCode;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReportCategoryCoding;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReportCategoryDisplay;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReportCode;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReportCodeCoding;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReportStatus;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DiagnosticReportTransformer implements DiagnosticReportController.Transformer {

  @Override
  public DiagnosticReport apply(CdwDiagnosticReport source) {
    return diagnosticReport(source);
  }

  CodeableConcept category(CdwDiagnosticReportCategory source) {
    if (source == null || allBlank(source.getCoding(), source.getText())) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(categoryCodings(source.getCoding()))
        .text(source.getText())
        .build();
  }

  List<Coding> categoryCodings(CdwDiagnosticReportCategoryCoding optionalSource) {
    if (optionalSource == null
        || allBlank(
            optionalSource.getSystem(), optionalSource.getCode(), optionalSource.getDisplay())) {
      return null;
    }
    return Collections.singletonList(
        Coding.builder()
            .system(optionalSource.getSystem())
            .code(ifPresent(optionalSource.getCode(), CdwDiagnosticReportCategoryCode::value))
            .display(
                ifPresent(optionalSource.getDisplay(), CdwDiagnosticReportCategoryDisplay::value))
            .build());
  }

  CodeableConcept code(CdwDiagnosticReportCode source) {
    if (source == null) {
      return null;
    }
    if (source.getCoding().isEmpty() && isBlank(source.getText())) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(codeCodings(source.getCoding()))
        .text(source.getText())
        .build();
  }

  private Coding codeCoding(CdwDiagnosticReportCodeCoding cdw) {
    if (cdw == null || allBlank(cdw.getCode(), cdw.getDisplay(), cdw.getSystem())) {
      return null;
    }
    return Coding.builder()
        .system(cdw.getSystem())
        .code(cdw.getCode())
        .display(cdw.getDisplay())
        .build();
  }

  List<Coding> codeCodings(List<CdwDiagnosticReportCodeCoding> source) {
    return convertAll(source, this::codeCoding);
  }

  private DiagnosticReport diagnosticReport(CdwDiagnosticReport source) {
    /*
     * While we have reference data for requests, specimens, and results, we do not
     * have full support for these resources and therefore must be omitted from this
     * resource.
     */
    return DiagnosticReport.builder()
        .id(source.getCdwId())
        .resourceType("DiagnosticReport")
        .status(status(source.getStatus()))
        .category(category(source.getCategory()))
        .code(code(source.getCode()))
        .subject(reference(source.getSubject()))
        .encounter(reference(source.getEncounter()))
        .effectiveDateTime(asDateTimeString(source.getEffective()))
        .issued(asDateTimeString(source.getIssued()))
        .performer(performer(source.getPerformer()))
        ._performer(performerExtenstion(source.getPerformer()))
        .result(result(source.getResults()))
        .build();
  }

  private boolean isUsable(CdwReference reference) {
    return reference != null && !allBlank(reference.getDisplay(), reference.getReference());
  }

  Reference performer(CdwReference maybeReference) {
    if (!isUsable(maybeReference)) {
      return null;
    }
    return convert(
        maybeReference,
        source ->
            Reference.builder()
                .reference(source.getReference())
                .display(source.getDisplay())
                .build());
  }

  Extension performerExtenstion(CdwReference maybeReference) {
    if (isUsable(maybeReference)) {
      return null;
    }
    return DataAbsentReason.of(Reason.unknown);
  }

  Reference reference(CdwReference maybeSource) {
    if (!isUsable(maybeSource)) {
      return null;
    }
    return convert(
        maybeSource,
        source ->
            Reference.builder()
                .reference(source.getReference())
                .display(source.getDisplay())
                .build());
  }

  List<Reference> result(CdwResults maybeCdw) {
    return convertAll(ifPresent(maybeCdw, CdwResults::getResult), this::reference);
  }

  DiagnosticReport.Code status(CdwDiagnosticReportStatus source) {
    return ifPresent(
        source, status -> EnumSearcher.of(DiagnosticReport.Code.class).find(status.value()));
  }
}
