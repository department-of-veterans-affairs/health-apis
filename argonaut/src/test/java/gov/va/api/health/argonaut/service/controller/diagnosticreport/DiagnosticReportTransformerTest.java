package gov.va.api.health.argonaut.service.controller.diagnosticreport;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.DataAbsentReason;
import gov.va.api.health.argonaut.api.DataAbsentReason.Reason;
import gov.va.api.health.argonaut.api.datatypes.CodeableConcept;
import gov.va.api.health.argonaut.api.datatypes.Coding;
import gov.va.api.health.argonaut.api.elements.Reference;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport.Code;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReport102Root;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReportCategory;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReportCategoryCode;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReportCategoryCoding;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReportCategoryDisplay;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReportCode;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReportCodeCoding;
import gov.va.dvp.cdw.xsd.model.CdwDiagnosticReportStatus;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.SneakyThrows;
import org.junit.Test;

public class DiagnosticReportTransformerTest {

  private final DiagnosticReportTransformer tx = new DiagnosticReportTransformer();

  private final CdwSampleData cdw = new CdwSampleData();

  private final Expected expected = new Expected();

  @Test
  public void category() {
    assertThat(tx.category(cdw.category())).isEqualTo(expected.category());
    assertThat(tx.category(null)).isNull();
    assertThat(tx.category(new CdwDiagnosticReportCategory())).isNull();
  }

  @Test
  public void categoryCoding() {
    assertThat(tx.categoryCodings(null)).isNull();
    assertThat(tx.categoryCodings(new CdwDiagnosticReportCategoryCoding())).isNull();
    assertThat(tx.categoryCodings(cdw.categoryCoding())).isEqualTo(expected.categoryCoding());
  }

  @Test
  public void cdwReference() {
    assertThat(tx.reference(null)).isNull();
    assertThat(tx.reference(new CdwReference())).isNull();
    assertThat(tx.reference(cdw.cdwReference())).isEqualTo(expected.reference());
  }

  @Test
  public void code() {
    assertThat(tx.code(null)).isNull();
    assertThat(tx.code(new CdwDiagnosticReportCode())).isNull();
    assertThat(tx.code(cdw.code())).isEqualTo(expected.code());
  }

  @Test
  public void codeCodings() {
    assertThat(tx.codeCodings(null)).isNull();
    assertThat(tx.codeCodings(singletonList(new CdwDiagnosticReportCodeCoding()))).isNull();
    assertThat(tx.codeCodings(cdw.code().getCoding())).isEqualTo(expected.code().coding());
  }

  @Test
  public void diagnosticReport() {
    assertThat(tx.apply(cdw.diagnosticReport())).isEqualTo(expected.diagnosticReport());
    assertThat(tx.apply(cdw.diagnosticReportNullPerformer()))
        .isEqualTo(expected.diagnosticReportNullPerformer());
  }

  @Test
  public void performer() {
    assertThat(tx.performer(cdw.cdwReference())).isEqualTo(expected.reference());
    assertThat(tx.performer(null)).isNull();
    assertThat(tx.performer(new CdwReference())).isNull();
    // _performer
    assertThat(tx.performerExtenstion(null)).isEqualTo(DataAbsentReason.of(Reason.unknown));
    assertThat(tx.performerExtenstion(cdw.cdwReference())).isNull();
  }

  @Test
  public void status() {
    assertThat(tx.status(cdw.status())).isEqualTo(expected.status());
    assertThat(tx.status(CdwDiagnosticReportStatus.APPENDED)).isEqualTo(Code.appended);
    assertThat(tx.status(CdwDiagnosticReportStatus.CANCELLED)).isEqualTo(Code.cancelled);
    assertThat(tx.status(CdwDiagnosticReportStatus.CORRECTED)).isEqualTo(Code.corrected);
    assertThat(tx.status(CdwDiagnosticReportStatus.FINAL)).isEqualTo(Code._final);
    assertThat(tx.status(CdwDiagnosticReportStatus.ENTERED_IN_ERROR))
        .isEqualTo(Code.entered_in_error);
    assertThat(tx.status(CdwDiagnosticReportStatus.PARTIAL)).isEqualTo(Code.partial);
    assertThat(tx.status(CdwDiagnosticReportStatus.REGISTERED)).isEqualTo(Code.registered);
    assertThat(tx.status(null)).isNull();
  }

  private static class CdwSampleData {

    private CdwDiagnosticReportCategory category() {
      CdwDiagnosticReportCategory category = new CdwDiagnosticReportCategory();
      category.setCoding(categoryCoding());
      return category;
    }

    private CdwDiagnosticReportCategoryCoding categoryCoding() {
      CdwDiagnosticReportCategoryCoding coding = new CdwDiagnosticReportCategoryCoding();
      coding.setSystem("http://hl7.org/fhir/ValueSet/diagnostic-service-sections");
      coding.setCode(CdwDiagnosticReportCategoryCode.LAB);
      coding.setDisplay(CdwDiagnosticReportCategoryDisplay.LABORATORY);
      return coding;
    }

    private CdwReference cdwReference() {
      CdwReference ref = new CdwReference();
      ref.setReference("HelloReference");
      ref.setDisplay("HelloDisplay");
      return ref;
    }

    private CdwDiagnosticReportCode code() {
      CdwDiagnosticReportCode code = new CdwDiagnosticReportCode();
      code.setText("panel");
      code.getCoding().add(codeCoding());
      return code;
    }

    private CdwDiagnosticReportCodeCoding codeCoding() {
      CdwDiagnosticReportCodeCoding codeCoding = new CdwDiagnosticReportCodeCoding();
      codeCoding.setCode("LAB");
      codeCoding.setSystem("http://hl7.org/fhir/ValueSet/diagnostic-service-sections");
      codeCoding.setDisplay("Laboratory");
      return codeCoding;
    }

    CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport diagnosticReport() {
      CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport sampleDR =
          new CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport();
      sampleDR.setCdwId("5d582505-650e-58b3-8673-49138f7b2b04");
      sampleDR.setStatus(status());
      sampleDR.setCategory(category());
      sampleDR.setCode(code());
      sampleDR.setSubject(cdwReference());
      sampleDR.setEncounter(cdwReference());
      sampleDR.setEffective(effective());
      sampleDR.setIssued(issued());
      sampleDR.setPerformer(cdwReference());
      return sampleDR;
    }

    CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport
        diagnosticReportNullPerformer() {
      CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport sampleDR =
          new CdwDiagnosticReport102Root.CdwDiagnosticReports.CdwDiagnosticReport();
      sampleDR.setCdwId("5d582505-650e-58b3-8673-49138f7b2b04");
      sampleDR.setStatus(status());
      sampleDR.setCategory(category());
      sampleDR.setCode(code());
      sampleDR.setSubject(cdwReference());
      sampleDR.setEncounter(cdwReference());
      sampleDR.setEffective(effective());
      sampleDR.setIssued(issued());
      sampleDR.setPerformer(null);
      return sampleDR;
    }

    @SneakyThrows
    private XMLGregorianCalendar effective() {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar("2013-06-21T19:03:16Z");
    }

    @SneakyThrows
    private XMLGregorianCalendar issued() {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar("2013-06-21T20:05:12Z");
    }

    private CdwDiagnosticReportStatus status() {
      return CdwDiagnosticReportStatus.FINAL;
    }
  }

  private static class Expected {

    private CodeableConcept category() {
      return CodeableConcept.builder().coding(categoryCoding()).build();
    }

    private List<Coding> categoryCoding() {
      return singletonList(
          Coding.builder()
              .system("http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
              .code("LAB")
              .display("Laboratory")
              .build());
    }

    private CodeableConcept code() {
      return CodeableConcept.builder().text("panel").coding(codeCodings()).build();
    }

    private List<Coding> codeCodings() {
      return singletonList(
          Coding.builder()
              .system("http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
              .code("LAB")
              .display("Laboratory")
              .build());
    }

    DiagnosticReport diagnosticReport() {
      return DiagnosticReport.builder()
          .resourceType("DiagnosticReport")
          .id("5d582505-650e-58b3-8673-49138f7b2b04")
          .status(status())
          .category(category())
          .code(code())
          .subject(reference())
          .encounter(reference())
          .effectiveDateTime("2013-06-21T19:03:16Z")
          .issued("2013-06-21T20:05:12Z")
          .performer(reference())
          .build();
    }

    DiagnosticReport diagnosticReportNullPerformer() {
      return DiagnosticReport.builder()
          .resourceType("DiagnosticReport")
          .id("5d582505-650e-58b3-8673-49138f7b2b04")
          .status(status())
          .category(category())
          .code(code())
          .subject(reference())
          .encounter(reference())
          .effectiveDateTime("2013-06-21T19:03:16Z")
          .issued("2013-06-21T20:05:12Z")
          ._performer(DataAbsentReason.of(Reason.unknown))
          .build();
    }

    private Reference reference() {
      return Reference.builder().reference("HelloReference").display("HelloDisplay").build();
    }

    private Code status() {
      return Code._final;
    }
  }
}
