package gov.va.api.health.dataquery.service.controller.appointment;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Coding;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.resources.Appointment;
import gov.va.api.health.dataquery.api.resources.Appointment.Participant;
import gov.va.api.health.dataquery.api.resources.Appointment.Participant.ParticipantStatus;
import gov.va.api.health.dataquery.api.resources.Appointment.Participant.RequiredCode;
import gov.va.api.health.dataquery.api.resources.Appointment.Status;
import gov.va.dvp.cdw.xsd.model.CdwAppointment101Root.CdwAppointments.CdwAppointment;
import gov.va.dvp.cdw.xsd.model.CdwAppointment101Root.CdwAppointments.CdwAppointment.CdwParticipants;
import gov.va.dvp.cdw.xsd.model.CdwAppointment101Root.CdwAppointments.CdwAppointment.CdwParticipants.CdwParticipant;
import gov.va.dvp.cdw.xsd.model.CdwAppointment101Root.CdwAppointments.CdwAppointment.CdwParticipants.CdwParticipant.CdwTypes;
import gov.va.dvp.cdw.xsd.model.CdwAppointmentParticipantRequired;
import gov.va.dvp.cdw.xsd.model.CdwAppointmentParticipantStatus;
import gov.va.dvp.cdw.xsd.model.CdwAppointmentParticipantType;
import gov.va.dvp.cdw.xsd.model.CdwAppointmentParticipantTypeCode;
import gov.va.dvp.cdw.xsd.model.CdwAppointmentParticipantTypeCoding;
import gov.va.dvp.cdw.xsd.model.CdwAppointmentParticipantTypeDisplay;
import gov.va.dvp.cdw.xsd.model.CdwAppointmentStatus;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import java.math.BigInteger;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.Test;

public class AppointmentTransformerTest {

  private final AppointmentTransformer tx = new AppointmentTransformer();
  private final CdwSampleData cdw = CdwSampleData.get();
  private final Expected expected = Expected.get();

  @Test
  public void appointment() {
    assertThat(tx.apply(cdw.appointment())).isEqualTo(expected.appointment());
  }

  @Test
  public void participant() {
    assertThat(tx.participant(null)).isNull();
    assertThat(tx.participant(cdw.participant())).isEqualTo(expected.participant());
  }

  @Test
  public void participantStatus() {
    assertThat(tx.participantStatus(CdwAppointmentParticipantStatus.ACCEPTED))
        .isEqualTo(ParticipantStatus.accepted);
    assertThat(tx.participantStatus(CdwAppointmentParticipantStatus.DECLINED))
        .isEqualTo(ParticipantStatus.declined);
    assertThat(tx.participantStatus(CdwAppointmentParticipantStatus.NEEDS_ACTION))
        .isEqualTo(ParticipantStatus.needs_action);
    assertThat(tx.participantStatus(CdwAppointmentParticipantStatus.TENTATIVE))
        .isEqualTo(ParticipantStatus.tentative);
  }

  @Test
  public void participants() {
    assertThat(tx.participants(null)).isNull();
    assertThat(tx.participants(new CdwParticipants())).isNull();
    assertThat(tx.participants(cdw.participants())).isEqualTo(expected.participants());
  }

  @Test
  public void reference() {
    assertThat(tx.reference(null)).isNull();
    assertThat(tx.reference(new CdwReference())).isNull();
    assertThat(tx.reference(cdw.reference("x", "y"))).isEqualTo(expected.reference("x", "y"));
  }

  @Test
  public void required() {
    assertThat(tx.required(CdwAppointmentParticipantRequired.INFORMATION_ONLY))
        .isEqualTo(RequiredCode.information_only);
    assertThat(tx.required(CdwAppointmentParticipantRequired.OPTIONAL))
        .isEqualTo(RequiredCode.optional);
    assertThat(tx.required(CdwAppointmentParticipantRequired.REQUIRED))
        .isEqualTo(RequiredCode.required);
  }

  public void status() {
    assertThat(tx.status(CdwAppointmentStatus.PROPOSED)).isEqualTo(Status.proposed);
    assertThat(tx.status(CdwAppointmentStatus.PENDING)).isEqualTo(Status.pending);
    assertThat(tx.status(CdwAppointmentStatus.BOOKED)).isEqualTo(Status.booked);
    assertThat(tx.status(CdwAppointmentStatus.ARRIVED)).isEqualTo(Status.arrived);
    assertThat(tx.status(CdwAppointmentStatus.FULFILLED)).isEqualTo(Status.fulfilled);
    assertThat(tx.status(CdwAppointmentStatus.CANCELLED)).isEqualTo(Status.cancelled);
    assertThat(tx.status(CdwAppointmentStatus.NOSHOW)).isEqualTo(Status.noshow);
  }

  @Test
  public void type() {
    assertThat(tx.type(null)).isNull();
    assertThat(tx.type(new CdwAppointmentParticipantType())).isNull();
    assertThat(tx.type(cdw.type())).isEqualTo(expected.type());
  }

  @Test
  public void typeCoding() {
    assertThat(tx.typeCodings(singletonList(cdw.typeCoding()))).isEqualTo(expected.type().coding());
    assertThat(tx.typeCodings(emptyList())).isNull();
    assertThat(tx.typeCodings(null)).isNull();
    assertThat(tx.typeCodings(singletonList(new CdwAppointmentParticipantTypeCoding()))).isNull();
  }

  @Test
  public void types() {
    assertThat(tx.types(null)).isNull();
    assertThat(tx.types(new CdwTypes())).isNull();
    assertThat(tx.types(cdw.types())).isEqualTo(expected.types());
  }

  @NoArgsConstructor(staticName = "get", access = AccessLevel.PUBLIC)
  static class CdwSampleData {
    CdwAppointment appointment() {
      CdwAppointment cdw = new CdwAppointment();
      cdw.setCdwId("1200438317388");
      cdw.setStatus(CdwAppointmentStatus.FULFILLED);
      cdw.setDescription("Unscheduled Visit");
      cdw.setStart(dateTime("2015-04-15T14:25:00Z"));
      cdw.setEnd(dateTime("2015-04-15T15:25:00Z"));
      cdw.setMinutesDuration(BigInteger.TEN);
      cdw.setComment("example comment");
      cdw.setParticipants(participants());
      return cdw;
    }

    @SneakyThrows
    private XMLGregorianCalendar dateTime(String timestamp) {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(timestamp);
    }

    private CdwParticipant participant() {
      CdwParticipant cdw = new CdwParticipant();
      cdw.setActor(reference("Patient/185601V825290", "VETERAN,JOHN Q"));
      cdw.setRequired(CdwAppointmentParticipantRequired.INFORMATION_ONLY);
      cdw.setStatus(CdwAppointmentParticipantStatus.NEEDS_ACTION);
      cdw.setTypes(types());
      return cdw;
    }

    private CdwParticipants participants() {
      CdwParticipants cdw = new CdwParticipants();
      cdw.getParticipant().add(participant());
      return cdw;
    }

    private CdwReference reference(String ref, String display) {
      CdwReference cdw = new CdwReference();
      cdw.setReference(ref);
      cdw.setDisplay(display);
      return cdw;
    }

    private CdwAppointmentParticipantType type() {
      CdwAppointmentParticipantType cdw = new CdwAppointmentParticipantType();
      cdw.setText("Example text");
      cdw.getCoding().add(typeCoding());
      return cdw;
    }

    private CdwAppointmentParticipantTypeCoding typeCoding() {
      CdwAppointmentParticipantTypeCoding cdw = new CdwAppointmentParticipantTypeCoding();
      cdw.setSystem("http://hl7.org/fhir/v3/ParticipationType");
      cdw.setCode(CdwAppointmentParticipantTypeCode.PART);
      cdw.setDisplay(CdwAppointmentParticipantTypeDisplay.PARTICIPATION);
      return cdw;
    }

    private CdwTypes types() {
      CdwTypes cdw = new CdwTypes();
      cdw.getType().add(type());
      return cdw;
    }
  }

  @NoArgsConstructor(staticName = "get")
  public static class Expected {

    Appointment appointment() {
      return Appointment.builder()
          .resourceType("Appointment")
          .id("1200438317388")
          .status(Status.fulfilled)
          .description("Unscheduled Visit")
          .start("2015-04-15T14:25:00Z")
          .end("2015-04-15T15:25:00Z")
          .minutesDuration(10)
          .comment("example comment")
          .participant(participants())
          .build();
    }

    private Participant participant() {
      return Participant.builder()
          .actor(reference("Patient/185601V825290", "VETERAN,JOHN Q"))
          .required(RequiredCode.information_only)
          .status(ParticipantStatus.needs_action)
          .type(types())
          .build();
    }

    private List<Participant> participants() {
      return singletonList(participant());
    }

    Reference reference(String ref, String display) {
      return Reference.builder().reference(ref).display(display).build();
    }

    CodeableConcept type() {
      return CodeableConcept.builder()
          .text("Example text")
          .coding(singletonList(typeCoding()))
          .build();
    }

    private Coding typeCoding() {
      return Coding.builder()
          .system("http://hl7.org/fhir/v3/ParticipationType")
          .code("PART")
          .display("Participation")
          .build();
    }

    private List<CodeableConcept> types() {
      return singletonList(type());
    }
  }
}
