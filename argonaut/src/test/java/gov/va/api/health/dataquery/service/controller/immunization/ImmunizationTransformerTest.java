package gov.va.api.health.dataquery.service.controller.immunization;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.api.DataAbsentReason;
import gov.va.api.health.dataquery.api.DataAbsentReason.Reason;
import gov.va.api.health.dataquery.api.datatypes.Annotation;
import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Coding;
import gov.va.api.health.dataquery.api.datatypes.Identifier;
import gov.va.api.health.dataquery.api.datatypes.Identifier.IdentifierUse;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.resources.Immunization;
import gov.va.api.health.dataquery.api.resources.Immunization.Reaction;
import gov.va.api.health.dataquery.api.resources.Immunization.Status;
import gov.va.dvp.cdw.xsd.model.CdwCodeableConcept;
import gov.va.dvp.cdw.xsd.model.CdwCoding;
import gov.va.dvp.cdw.xsd.model.CdwIdentifier;
import gov.va.dvp.cdw.xsd.model.CdwImmunization103Root.CdwImmunizations.CdwImmunization;
import gov.va.dvp.cdw.xsd.model.CdwImmunization103Root.CdwImmunizations.CdwImmunization.CdwIdentifiers;
import gov.va.dvp.cdw.xsd.model.CdwImmunization103Root.CdwImmunizations.CdwImmunization.CdwNotes;
import gov.va.dvp.cdw.xsd.model.CdwImmunization103Root.CdwImmunizations.CdwImmunization.CdwReactions;
import gov.va.dvp.cdw.xsd.model.CdwImmunizationNote;
import gov.va.dvp.cdw.xsd.model.CdwImmunizationReported;
import gov.va.dvp.cdw.xsd.model.CdwImmunizationStatus;
import gov.va.dvp.cdw.xsd.model.CdwReactionBackboneElement;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.SneakyThrows;
import org.junit.Test;

public class ImmunizationTransformerTest {

  private ImmunizationTransformer tx = new ImmunizationTransformer();
  private CdwSampleData cdw = new CdwSampleData();
  private Expected expected = new Expected();

  @Test
  public void coding() {
    assertThat(tx.codings(cdw.vaccineCode().getCoding()))
        .isEqualTo(expected.vaccineCode().coding());
    assertThat(tx.codings(null)).isNull();
    assertThat(tx.codings(emptyList())).isNull();
    assertThat(tx.codings(singletonList(new CdwCoding()))).isNull();
  }

  @Test
  public void identifier() {
    assertThat(tx.identifier(null)).isNull();
    assertThat(tx.identifier(new CdwIdentifiers())).isNull();
    assertThat(tx.identifier(cdw.identifiers())).isEqualTo(expected.identifiers());
  }

  @Test
  public void immunization() {
    assertThat(tx.apply(cdw.immunization())).isEqualTo(expected.immunization());
  }

  @Test
  public void note() {
    assertThat(tx.note(null)).isNull();
    assertThat(tx.note(new CdwNotes())).isNull();
    assertThat(tx.note(cdw.notes())).isEqualTo(expected.notes());
  }

  @Test
  public void reaction() {
    assertThat(tx.reaction(null)).isNull();
    assertThat(tx.reaction(new CdwReactions())).isNull();
    assertThat(tx.reaction(cdw.reactions())).isEqualTo(expected.reactions());
  }

  @Test
  public void reference() {
    assertThat(tx.reference(cdw.reference("Patient/185601V825290", "VETERAN,JOHN Q")))
        .isEqualTo(expected.reference("Patient/185601V825290", "VETERAN,JOHN Q"));
    assertThat(tx.reference(null)).isNull();
    assertThat(tx.reference(new CdwReference())).isNull();
  }

  @Test
  public void reported() {
    // reported field
    assertThat(tx.reported(null)).isNull();
    assertThat(tx.reported(CdwImmunizationReported.TRUE)).isTrue();
    assertThat(tx.reported(CdwImmunizationReported.FALSE)).isFalse();
    assertThat(tx.reported(CdwImmunizationReported.DATA_ABSENT_REASON_UNSUPPORTED)).isNull();
    // _reported field
    assertThat(tx.reportedExtension(null)).isEqualTo(DataAbsentReason.of(Reason.unknown));
    assertThat(tx.reportedExtension(CdwImmunizationReported.TRUE)).isNull();
    assertThat(tx.reportedExtension(CdwImmunizationReported.FALSE)).isNull();
    assertThat(tx.reportedExtension(CdwImmunizationReported.DATA_ABSENT_REASON_UNSUPPORTED))
        .isEqualTo(DataAbsentReason.of(Reason.unsupported));
  }

  @Test
  public void status() {
    // status field
    assertThat(tx.status(null)).isNull();
    assertThat(tx.status(CdwImmunizationStatus.COMPLETED)).isEqualTo(Immunization.Status.completed);
    assertThat(tx.status(CdwImmunizationStatus.ENTERED_IN_ERROR))
        .isEqualTo(Status.entered_in_error);
    assertThat(tx.status(CdwImmunizationStatus.DATA_ABSENT_REASON_UNSUPPORTED)).isNull();
    // _status field
    assertThat(tx.statusExtension(null)).isEqualTo(DataAbsentReason.of(Reason.unknown));
    assertThat(tx.statusExtension(CdwImmunizationStatus.COMPLETED)).isNull();
    assertThat(tx.statusExtension(CdwImmunizationStatus.ENTERED_IN_ERROR)).isNull();
    assertThat(tx.statusExtension(CdwImmunizationStatus.DATA_ABSENT_REASON_UNSUPPORTED))
        .isEqualTo(DataAbsentReason.of(Reason.unsupported));
  }

  @Test
  public void vaccineCode() {
    assertThat(tx.vaccineCode(cdw.vaccineCode())).isEqualTo(expected.vaccineCode());
    assertThat(tx.vaccineCode(null)).isNull();
    assertThat(tx.vaccineCode(new CdwCodeableConcept())).isNull();
  }

  private static class CdwSampleData {

    @SneakyThrows
    private XMLGregorianCalendar dateTime(String s) {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(s);
    }

    private CdwIdentifier identifier() {
      CdwIdentifier cdw = new CdwIdentifier();
      cdw.setSystem("http://example.com");
      cdw.setUse("official");
      cdw.setValue("123");
      return cdw;
    }

    private CdwIdentifiers identifiers() {
      CdwIdentifiers cdw = new CdwIdentifiers();
      cdw.getIdentifier().add(identifier());
      return cdw;
    }

    CdwImmunization immunization() {
      CdwImmunization cdw = new CdwImmunization();
      cdw.setCdwId("1000000043979");
      cdw.setIdentifiers(identifiers());
      cdw.setStatus(CdwImmunizationStatus.COMPLETED);
      cdw.setDate(dateTime("1999-03-29T18:23:27Z"));
      cdw.setVaccineCode(vaccineCode());
      cdw.setPatient(reference("Patient/185601V825290", "VETERAN,JOHN Q"));
      cdw.setWasNotGiven(true);
      cdw.setReported(CdwImmunizationReported.TRUE);
      cdw.setPerformer(reference("Practitioner/3884437", "SMITH,RESIDENT D"));
      cdw.setRequester(reference("Practitioner/1234567", "SMITH,RESIDENT D"));
      cdw.setEncounter(reference("Encounter/1000589900220", "1000589900220"));
      cdw.setLocation(reference("Location/0", "*Unknown at this time*"));
      cdw.setNotes(notes());
      cdw.setReactions(reactions());
      return cdw;
    }

    private CdwImmunizationNote note() {
      CdwImmunizationNote cdw = new CdwImmunizationNote();
      cdw.setText("L ARM");
      return cdw;
    }

    private CdwNotes notes() {
      CdwNotes cdw = new CdwNotes();
      cdw.getNote().add(note());
      return cdw;
    }

    private CdwReactionBackboneElement reaction() {
      CdwReactionBackboneElement cdw = new CdwReactionBackboneElement();
      cdw.setDetail(reference(null, "Other"));
      return cdw;
    }

    private CdwReactions reactions() {
      CdwReactions cdw = new CdwReactions();
      cdw.getReaction().add(reaction());
      return cdw;
    }

    private CdwReference reference(String reference, String display) {
      CdwReference cdw = new CdwReference();
      cdw.setReference(reference);
      cdw.setDisplay(display);
      return cdw;
    }

    private CdwCodeableConcept vaccineCode() {
      CdwCodeableConcept cdw = new CdwCodeableConcept();
      cdw.setText("M/R");
      cdw.getCoding().add(vaccineCodeCoding());
      return cdw;
    }

    private CdwCoding vaccineCodeCoding() {
      CdwCoding cdw = new CdwCoding();
      cdw.setSystem("http://hl7.org/fhir/sid/cvx");
      cdw.setDisplay("04");
      return cdw;
    }
  }

  private static class Expected {

    private Identifier identifier() {
      return Identifier.builder()
          .use(IdentifierUse.official)
          .system("http://example.com")
          .value("123")
          .build();
    }

    private List<Identifier> identifiers() {
      return singletonList(identifier());
    }

    Immunization immunization() {
      return Immunization.builder()
          .resourceType("Immunization")
          .id("1000000043979")
          .identifier(identifiers())
          .status(Status.completed)
          ._status(null)
          .date("1999-03-29T18:23:27Z")
          .vaccineCode(vaccineCode())
          .patient(reference("Patient/185601V825290", "VETERAN,JOHN Q"))
          .wasNotGiven(true)
          .reported(true)
          ._reported(null)
          .performer(reference("Practitioner/3884437", "SMITH,RESIDENT D"))
          .requester(reference("Practitioner/1234567", "SMITH,RESIDENT D"))
          .encounter(reference("Encounter/1000589900220", "1000589900220"))
          .location(reference("Location/0", "*Unknown at this time*"))
          .note(notes())
          .reaction(reactions())
          .build();
    }

    private Annotation note() {
      return Annotation.builder().text("L ARM").build();
    }

    private List<Annotation> notes() {
      return singletonList(note());
    }

    private Reaction reaction() {
      return Reaction.builder().detail(reference(null, "Other")).build();
    }

    private List<Reaction> reactions() {
      return singletonList(reaction());
    }

    private Reference reference(String reference, String display) {
      return Reference.builder().reference(reference).display(display).build();
    }

    private CodeableConcept vaccineCode() {
      return CodeableConcept.builder()
          .text("M/R")
          .coding(singletonList(vaccineCodeCoding()))
          .build();
    }

    private Coding vaccineCodeCoding() {
      return Coding.builder().system("http://hl7.org/fhir/sid/cvx").display("04").build();
    }
  }
}
