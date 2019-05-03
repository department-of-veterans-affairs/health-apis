package gov.va.api.health.dataquery.service.controller.procedure;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.argonaut.api.resources.Procedure.Status;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.dvp.cdw.xsd.model.CdwCodeSystem;
import gov.va.dvp.cdw.xsd.model.CdwProcedure101Root.CdwProcedures.CdwProcedure;
import gov.va.dvp.cdw.xsd.model.CdwProcedure101Root.CdwProcedures.CdwProcedure.CdwCode;
import gov.va.dvp.cdw.xsd.model.CdwProcedure101Root.CdwProcedures.CdwProcedure.CdwReasonNotPerformed;
import gov.va.dvp.cdw.xsd.model.CdwProcedureStatus;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.SneakyThrows;
import org.junit.Test;

public class ProcedureTransformerTest {
  private ProcedureTransformer tx = new ProcedureTransformer();
  private CdwSampleData cdw = new CdwSampleData();
  private Expected expected = new Expected();

  @Test
  public void code() {
    assertThat(tx.code(cdw.procedure().getCode())).isEqualTo(expected.procedure().code());
  }

  @Test
  public void codeCodings() {
    assertThat(tx.codeCodings(singletonList(null))).isNull();
    assertThat(tx.codeCodings(null)).isNull();
    assertThat(tx.codeCodings(cdw.code().getCoding())).isEqualTo(expected.codeCoding());
  }

  @Test
  public void procedure() {
    assertThat(tx.apply(cdw.procedure())).isEqualTo(expected.procedure());
  }

  @Test
  public void reasonNotPerformed() {
    assertThat(tx.reasonNotPerformed(null)).isNull();
    assertThat(tx.reasonNotPerformed(new CdwReasonNotPerformed())).isNull();
    assertThat(tx.reasonNotPerformed(cdw.procedure().getReasonNotPerformed()))
        .isEqualTo(expected.procedure().reasonNotPerformed());
  }

  @Test
  public void reference() {
    assertThat(tx.reference(cdw.procedure().getLocation()))
        .isEqualTo(expected.procedure().location());
  }

  @Test
  public void status() {
    assertThat(tx.status(null)).isNull();
    assertThat(tx.status(cdw.procedure().getStatus())).isEqualTo(expected.procedure().status());
  }

  private static class CdwSampleData {
    private CdwCode code() {
      CdwCode code = new CdwCode();
      CdwCode.CdwCoding coding = new CdwCode.CdwCoding();
      coding.setSystem(CdwCodeSystem.HTTP_WWW_AMA_ASSN_ORG_GO_CPT);
      coding.setCode("43239");
      coding.setDisplay("EGD BIOPSY SINGLE/MULTIPLE");
      code.getCoding().add(coding);
      return code;
    }

    @SneakyThrows
    private XMLGregorianCalendar dateTime(String s) {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(s);
    }

    private CdwReference encounter() {
      CdwReference ref = new CdwReference();
      ref.setReference("Encounter/69f76c18-7e54-5da1-817f-91d453f7d9a7");
      ref.setDisplay("1400065292107");
      return ref;
    }

    private CdwReference location() {
      CdwReference ref = new CdwReference();
      ref.setReference("Location/81f8f1e2-c749-54e0-a8f6-24016c5174cb");
      ref.setDisplay("ZZDO NOT USE!!");
      return ref;
    }

    CdwProcedure procedure() {
      CdwProcedure cdw = new CdwProcedure();
      cdw.setCdwId("b3108f9c-7ec0-5a7e-88c8-9b704a3a11d1");
      cdw.setSubject(subject());
      cdw.setStatus(CdwProcedureStatus.COMPLETED);
      cdw.setCode(code());
      cdw.setNotPerformed(false);
      cdw.setReasonNotPerformed(reason());
      cdw.setPerformedDateTime(dateTime("2009-05-01T04:00:00Z"));
      cdw.setEncounter(encounter());
      cdw.setLocation(location());
      return cdw;
    }

    private CdwReasonNotPerformed reason() {
      CdwReasonNotPerformed reason = new CdwReasonNotPerformed();
      reason.setText("NOT NEEDED");
      return reason;
    }

    private CdwReference subject() {
      CdwReference ref = new CdwReference();
      ref.setReference("Patient/185601V825290");
      ref.setDisplay("VETERAN,JOHN Q");
      return ref;
    }
  }

  private static class Expected {
    private CodeableConcept code() {
      return CodeableConcept.builder().coding(codeCoding()).build();
    }

    private List<Coding> codeCoding() {
      return singletonList(
          Coding.builder()
              .system("http://www.ama-assn.org/go/cpt")
              .code("43239")
              .display("EGD BIOPSY SINGLE/MULTIPLE")
              .build());
    }

    Procedure procedure() {
      return Procedure.builder()
          .resourceType("Procedure")
          .id("b3108f9c-7ec0-5a7e-88c8-9b704a3a11d1")
          .subject(
              Reference.builder()
                  .reference("Patient/185601V825290")
                  .display("VETERAN,JOHN Q")
                  .build())
          .status(Status.completed)
          .code(code())
          .notPerformed(false)
          .reasonNotPerformed(reason())
          .performedDateTime("2009-05-01T04:00:00Z")
          .encounter(
              Reference.builder()
                  .reference("Encounter/69f76c18-7e54-5da1-817f-91d453f7d9a7")
                  .display("1400065292107")
                  .build())
          .location(
              Reference.builder()
                  .reference("Location/81f8f1e2-c749-54e0-a8f6-24016c5174cb")
                  .display("ZZDO NOT USE!!")
                  .build())
          .build();
    }

    private List<CodeableConcept> reason() {
      return singletonList(CodeableConcept.builder().text("NOT NEEDED").build());
    }
  }
}
