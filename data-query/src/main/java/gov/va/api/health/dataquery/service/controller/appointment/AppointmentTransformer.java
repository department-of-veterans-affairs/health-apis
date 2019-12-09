package gov.va.api.health.dataquery.service.controller.appointment;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asInteger;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.convertAll;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.ifPresent;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.Appointment;
import gov.va.api.health.dstu2.api.resources.Appointment.Participant;
import gov.va.api.health.dstu2.api.resources.Appointment.Participant.ParticipantStatus;
import gov.va.api.health.dstu2.api.resources.Appointment.Participant.RequiredCode;
import gov.va.api.health.dstu2.api.resources.Appointment.Status;
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
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AppointmentTransformer implements AppointmentController.Transformer {
  @Override
  public Appointment apply(CdwAppointment cdw) {
    return Appointment.builder()
        .resourceType("Appointment")
        .id(cdw.getCdwId())
        .status(status(cdw.getStatus()))
        .description(cdw.getDescription())
        .start(asDateTimeString(cdw.getStart()))
        .end(asDateTimeString(cdw.getEnd()))
        .minutesDuration(asInteger(cdw.getMinutesDuration()))
        .comment(cdw.getComment())
        .participant(participants(cdw.getParticipants()))
        .build();
  }

  Participant participant(CdwParticipant cdw) {
    if (cdw == null
        || allBlank(cdw.getActor(), cdw.getRequired(), cdw.getStatus(), cdw.getTypes())) {
      return null;
    }
    return convert(
        cdw,
        source ->
            Participant.builder()
                .actor(reference(source.getActor()))
                .required(required(source.getRequired()))
                .status(participantStatus(source.getStatus()))
                .type(types(source.getTypes()))
                .build());
  }

  ParticipantStatus participantStatus(CdwAppointmentParticipantStatus cdw) {
    return convert(cdw, source -> EnumSearcher.of(ParticipantStatus.class).find(source.value()));
  }

  List<Participant> participants(CdwParticipants cdw) {
    return convertAll(ifPresent(cdw, CdwParticipants::getParticipant), this::participant);
  }

  Reference reference(CdwReference maybeCdw) {
    if (maybeCdw == null || allBlank(maybeCdw.getDisplay(), maybeCdw.getReference())) {
      return null;
    }
    return convert(
        maybeCdw,
        source ->
            Reference.builder()
                .reference(source.getReference())
                .display(source.getDisplay())
                .build());
  }

  RequiredCode required(CdwAppointmentParticipantRequired cdw) {
    return convert(cdw, source -> EnumSearcher.of(RequiredCode.class).find(source.value()));
  }

  Status status(CdwAppointmentStatus source) {
    return EnumSearcher.of(Appointment.Status.class).find(source.value());
  }

  CodeableConcept type(CdwAppointmentParticipantType cdw) {
    if (cdw == null) {
      return null;
    }
    if (cdw.getCoding().isEmpty() && isBlank(cdw.getText())) {
      return null;
    }
    return convert(
        cdw,
        source ->
            CodeableConcept.builder()
                .text(source.getText())
                .coding(typeCodings(source.getCoding()))
                .build());
  }

  private Coding typeCoding(CdwAppointmentParticipantTypeCoding cdw) {
    if (cdw == null || allBlank(cdw.getCode(), cdw.getDisplay(), cdw.getSystem())) {
      return null;
    }
    return Coding.builder()
        .system(cdw.getSystem())
        .code(ifPresent(cdw.getCode(), CdwAppointmentParticipantTypeCode::value))
        .display(ifPresent(cdw.getDisplay(), CdwAppointmentParticipantTypeDisplay::value))
        .build();
  }

  List<Coding> typeCodings(List<CdwAppointmentParticipantTypeCoding> source) {
    return convertAll(source, this::typeCoding);
  }

  List<CodeableConcept> types(CdwTypes cdw) {
    return convertAll(ifPresent(cdw, CdwTypes::getType), this::type);
  }
}
