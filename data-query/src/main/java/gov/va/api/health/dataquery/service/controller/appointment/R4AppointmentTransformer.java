package gov.va.api.health.dataquery.service.controller.appointment;

import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static java.util.Map.entry;

import gov.va.api.health.dataquery.service.controller.R4Transformers;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
final class R4AppointmentTransformer {
  private static final Set<String> SUPPORTED_PARTICIPANT_TYPES = Set.of("Location", "Patient");

  private static final Map<String, String> CANCELLATION_REASON_MAPPINGS =
      Map.ofEntries(
          entry("patient", "pat"),
          entry("patient: canceled via automated reminder system", "pat-crs"),
          entry("patient: canceled via patient portal", "pat-cpp"),
          entry("patient: deceased", "pat-dec"),
          entry("patient: feeling better", "pat-fb"),
          entry("patient: lack of transportation", "pat-lt"),
          entry("patient: member terminated", "pat-mt"),
          entry("patient: moved", "pat-mv"),
          entry("patient: pregnant", "pat-preg"),
          entry("patient: scheduled from wait list", "pat-swl"),
          entry("patient: unhappy/changed provider", "pat-ucp"),
          entry("provider", "prov"),
          entry("provider: personal", "prov-pers"),
          entry("provider: discharged", "prov-dch"),
          entry("provider: edu/meeting", "prov-edu"),
          entry("provider: hospitalized", "prov-hosp"),
          entry("provider: labs out of acceptable range", "prov-labs"),
          entry("provider: mri screening form marked do not proceed", "prov-mri"),
          entry("provider: oncology treatment plan changes", "prov-onc"),
          entry("equipment maintenance/repair", "maint"),
          entry("prep/med incomplete", "meds-inc"),
          entry("other", "other"),
          entry("other: cms therapy cap service not authorized", "oth-cms"),
          entry("other: error", "oth-err"),
          entry("other: financial", "oth-fin"),
          entry("other: improper iv access/infiltrate iv", "oth-iv"),
          entry("other: no interpreter available", "oth-int"),
          entry("other: prep/med/results unavailable", "oth-mu"),
          entry("other: room/resource maintenance", "oth-room"),
          entry("other: schedule order error", "oth-oerr"),
          entry("other: silent walk in error", "oth-swie"),
          entry("other: weather", "oth-weath"));

  @NonNull private final DatamartAppointment dm;

  CodeableConcept appointmentType(Optional<String> maybeAppointmentType) {
    if (isBlank(maybeAppointmentType)) {
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

  CodeableConcept cancelationReason(Optional<String> maybeCancelationReason) {
    if (isBlank(maybeCancelationReason)) {
      return null;
    }
    var lookUpCode = CANCELLATION_REASON_MAPPINGS.get(
            maybeCancelationReason.get().toLowerCase());
    if(lookUpCode == null){
      log.warn("Appointment.cancelationReason value: {} not found.", maybeCancelationReason.get());
      return null;
    }
    return CodeableConcept.builder()
        .coding(
            List.of(
                Coding.builder()
                    .system("http://terminology.hl7.org/CodeSystem/appointment-cancellation-reason")
                    .code(
                        CANCELLATION_REASON_MAPPINGS.get(
                            maybeCancelationReason.get().toLowerCase()))
                    .display(maybeCancelationReason.get())
                    .build()))
        .text(maybeCancelationReason.get())
        .build();
  }

  String comment(Optional<String> maybeComment) {
    if (isBlank(maybeComment)) {
      return null;
    }
    return maybeComment.get();
  }

  String description(Optional<String> maybeDescription) {
    if (isBlank(maybeDescription)) {
      return null;
    }
    return maybeDescription.get();
  }

  boolean isSupportedParticipant(DatamartReference r) {
    return !isBlank(r)
        && r.hasTypeAndReference()
        && SUPPORTED_PARTICIPANT_TYPES.contains(r.type().get());
  }

  Integer minutesDuration(Optional<Integer> maybeMinutesDuration) {
    if (isBlank(maybeMinutesDuration)) {
      return null;
    }
    return maybeMinutesDuration.get();
  }

  Appointment.Participant participant(
      DatamartReference dmReference, Appointment.ParticipationStatus participationStatus) {
    if (isBlank(dmReference)) {
      return null;
    }
    // We only understand Appointment Participants that are Locations or Patients at this time.
    if (!isSupportedParticipant(dmReference)) {
      return null;
    }
    return Appointment.Participant.builder()
        .actor(R4Transformers.asReference(dmReference))
        .status(participationStatus)
        .build();
  }

  List<Appointment.Participant> participants(List<DatamartReference> dmParticipants, String cdwId) {
    if (isBlank(cdwId)) {
      return null;
    }
    // If the appointment is from the WAITLIST TABLE(cdwId = 123456:W) status is tentative
    // If the appointment is from the APPOINTMENT TABLE(cdwId = 123456:A) status is accepted
    var cdwIdParts = cdwId.split(":", -1);
    if (cdwIdParts.length != 2) {
      return null;
    }
    Appointment.ParticipationStatus participationStatus;
    switch (cdwIdParts[1]) {
      case "W":
        participationStatus = Appointment.ParticipationStatus.tentative;
        break;
      case "A":
        participationStatus = Appointment.ParticipationStatus.accepted;
        break;
      default:
        return null;
    }
    return dmParticipants.stream()
        .map(p -> participant(p, participationStatus))
        .collect(Collectors.toList());
  }

  List<CodeableConcept> serviceCategory(Optional<String> maybeServiceCategory) {
    if (isBlank(maybeServiceCategory)) {
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
    if (isBlank(serviceType)) {
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

  List<CodeableConcept> specialty(Optional<String> maybeSpecialty) {
    if (isBlank(maybeSpecialty)) {
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

  Appointment toFhir() {
    return Appointment.builder()
        .resourceType("Appointment")
        .id(dm.cdwId())
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
        .participant(participants(dm.participant(), dm.cdwId()))
        .build();
  }
}
