package gov.va.api.health.dataquery.service.controller.appointment;

import gov.va.api.health.dataquery.service.controller.R4Transformers;
import gov.va.api.health.dataquery.service.controller.Transformers;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.lighthouse.datamart.DatamartReference;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static gov.va.api.health.dataquery.service.controller.Transformers.*;

@Builder
final class R4AppointmentTransformer {
  @NonNull private final DatamartAppointment dm;

  CodeableConcept cancelationReason(Optional<String> maybeCancelationReason) {
    if (Transformers.isBlank(maybeCancelationReason)) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(
            List.of(
                Coding.builder()
                    .system("http://terminology.hl7.org/CodeSystem/appointment-cancellation-reason")
                    .code(maybeCancelationReason.get())
                    .display(maybeCancelationReason.get())
                    .build()))
        .text(maybeCancelationReason.get())
        .build();
  }

  List<CodeableConcept> serviceCategory(Optional<String> maybeServiceCategory) {
    if (Transformers.isBlank(maybeServiceCategory)) {
      return null;
    }
    return List.of(
        CodeableConcept.builder()
            .coding(
                List.of(
                    Coding.builder()
                        .system("http://terminology.hl7.org/CodeSystem/service-category")
                        .code(maybeServiceCategory.get())
                        .display(maybeServiceCategory.get())
                        .build()))
            .text(maybeServiceCategory.get())
            .build());
  }

  List<CodeableConcept> serviceType(String serviceType) {
    if (Transformers.isBlank(serviceType)) {
      return null;
    }
    return List.of(
        CodeableConcept.builder()
            .coding(
                List.of(
                    Coding.builder()
                        .system("http://terminology.hl7.org/CodeSystem/service-type")
                        .code(serviceType)
                        .display(serviceType)
                        .build()))
            .text(serviceType)
            .build());
  }

  Integer minutesDuration(Optional<Integer> maybeMinutesDuration) {
    if (Transformers.isBlank(maybeMinutesDuration)) {
      return null;
    }
    return maybeMinutesDuration.get();
  }

  List<CodeableConcept> specialty(Optional<String> maybeSpecialty) {
    if (Transformers.isBlank(maybeSpecialty)) {
      return null;
    }
    return List.of(
        CodeableConcept.builder()
            .coding(
                List.of(
                    Coding.builder()
                        .system("http://hl7.org/fhir/ValueSet/c80-practice-codes")
                        .code(maybeSpecialty.get())
                        .display(maybeSpecialty.get())
                        .build()))
            .text(maybeSpecialty.get())
            .build());
  }

  CodeableConcept appointmentType(Optional<String> maybeAppointmentType) {
    if (Transformers.isBlank(maybeAppointmentType)) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(
            List.of(
                Coding.builder()
                    .system("http://terminology.hl7.org/CodeSystem/v2-0276")
                    .code(maybeAppointmentType.get())
                    .display(maybeAppointmentType.get())
                    .build()))
        .text(maybeAppointmentType.get())
        .build();
  }

  String description(Optional<String> maybeDescription) {
    if (Transformers.isBlank(maybeDescription)) {
      return null;
    }
    return maybeDescription.get();
  }

  String comment(Optional<String> maybeComment) {
    if (Transformers.isBlank(maybeComment)) {
      return null;
    }
    return maybeComment.get();
  }

  List<Appointment.Participant> participants(List<DatamartReference> dmParticipants) {
      return dmParticipants.stream().map(this::participant).collect(Collectors.toList());
  }

  Appointment.Participant participant(DatamartReference dmReference) {
    if(Transformers.isBlank(dmReference)) {
      return null;
    }
    // We only understand Appointment Participants that are Locations or Patients at this time.
    if(dmReference.type().isPresent() && !(StringUtils.equals(dmReference.type().get(), "Location") || StringUtils.equals(dmReference.type().get(), "Patient"))) {
      return null;
    }
    return Appointment.Participant.builder()
            .actor(R4Transformers.asReference(dmReference))
            .status(Appointment.ParticipationStatus.accepted)
            .build();
  }

  Appointment toFhir() {
    return Appointment.builder()
        .resourceType("Appointment")
        .id(dm.cdwId())
        //.status()
        .cancelationReason(cancelationReason(dm.cancelationReason()))
        .serviceCategory(serviceCategory(dm.serviceCategory()))
        .serviceType(serviceType(dm.serviceType()))
        .specialty(specialty(dm.specialty()))
        .appointmentType(appointmentType(dm.appointmentType()))
        .description(description(dm.description()))
        .start(asDateTimeString(dm.start()))
        .end(asDateTimeString(dm.end()))
        .minutesDuration(minutesDuration(dm.minutesDuration()))
        .created(dm.created())
        .comment(comment(dm.comment()))
        .participant(participants(dm.participant()))
        .build();
  }
}
