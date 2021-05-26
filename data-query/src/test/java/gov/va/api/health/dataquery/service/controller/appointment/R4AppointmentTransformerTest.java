package gov.va.api.health.dataquery.service.controller.appointment;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.health.r4.api.resources.Appointment.AppointmentStatus;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class R4AppointmentTransformerTest {
  static List<CodeableConcept> serviceCategory(String display, String code) {
    return List.of(
        CodeableConcept.builder()
            .coding(
                List.of(
                    Coding.builder()
                        .display(display)
                        .code(code)
                        .system("http://www.va.gov/Terminology/VistADefinedTerms/44-9")
                        .build()))
            .text(display)
            .build());
  }

  static Stream<Arguments> status() {
    Instant past = Instant.now().minus(Duration.ofDays(2));
    Instant future = Instant.now().plus(Duration.ofDays(2));
    String noShow = "NO SHOW";
    String noShowAuto = "NO-SHOW & AUTO RE-BOOK";
    String cancelPat = "CANCELLED BY PATIENT";
    String cancelPatAuto = "CANCELLED BY PATIENT & AUTO-REBOOK";
    String cancelClin = "CANCELLED BY CLINIC";
    String cancelClinAuto = "CANCELLED BY CLINIC & AUTO RE-BOOK";
    String inpatient = "INPATIENT APPOINTMENT";
    String noAction = "NO ACTION TAKEN";
    String badStatus = "WTF MAN?";
    AppointmentStatus booked = Appointment.AppointmentStatus.booked;
    AppointmentStatus fulfilled = Appointment.AppointmentStatus.fulfilled;
    AppointmentStatus cancelled = Appointment.AppointmentStatus.cancelled;
    AppointmentStatus noshow = Appointment.AppointmentStatus.noshow;
    return Stream.of(
        arguments(null, null, null, booked),
        arguments(null, null, -1L, booked),
        arguments(null, null, 1L, booked),
        arguments(null, noShow, null, noshow),
        arguments(null, noShow, -1L, noshow),
        arguments(null, noShow, 1L, noshow),
        arguments(null, noShowAuto, null, noshow),
        arguments(null, noShowAuto, -1L, noshow),
        arguments(null, noShowAuto, 1L, noshow),
        arguments(null, cancelPat, null, cancelled),
        arguments(null, cancelPat, -1L, cancelled),
        arguments(null, cancelPat, 1L, cancelled),
        arguments(null, cancelPatAuto, null, cancelled),
        arguments(null, cancelPatAuto, -1L, cancelled),
        arguments(null, cancelPatAuto, 1L, cancelled),
        arguments(null, cancelClin, null, cancelled),
        arguments(null, cancelClin, -1L, cancelled),
        arguments(null, cancelClin, 1L, cancelled),
        arguments(null, cancelClinAuto, null, cancelled),
        arguments(null, cancelClinAuto, -1L, cancelled),
        arguments(null, cancelClinAuto, 1L, cancelled),
        arguments(null, inpatient, null, booked),
        arguments(null, inpatient, -1L, booked),
        arguments(null, inpatient, 1L, booked),
        arguments(null, noAction, null, booked),
        arguments(null, noAction, -1L, booked),
        arguments(null, noAction, 1L, booked),
        arguments(null, badStatus, null, booked),
        arguments(null, badStatus, -1L, booked),
        arguments(null, badStatus, 1L, booked),
        arguments(past, null, null, booked),
        arguments(past, null, -1L, noshow),
        arguments(past, null, 1L, fulfilled),
        arguments(past, noShow, null, noshow),
        arguments(past, noShow, -1L, noshow),
        arguments(past, noShow, 1L, noshow),
        arguments(past, noShowAuto, null, noshow),
        arguments(past, noShowAuto, -1L, noshow),
        arguments(past, noShowAuto, 1L, noshow),
        arguments(past, cancelPat, null, cancelled),
        arguments(past, cancelPat, -1L, cancelled),
        arguments(past, cancelPat, 1L, cancelled),
        arguments(past, cancelPatAuto, null, cancelled),
        arguments(past, cancelPatAuto, -1L, cancelled),
        arguments(past, cancelPatAuto, 1L, cancelled),
        arguments(past, cancelClin, null, cancelled),
        arguments(past, cancelClin, -1L, cancelled),
        arguments(past, cancelClin, 1L, cancelled),
        arguments(past, cancelClinAuto, null, cancelled),
        arguments(past, cancelClinAuto, -1L, cancelled),
        arguments(past, cancelClinAuto, 1L, cancelled),
        arguments(past, inpatient, null, booked),
        arguments(past, inpatient, -1L, noshow),
        arguments(past, inpatient, 1L, fulfilled),
        arguments(past, noAction, null, booked),
        arguments(past, noAction, -1L, noshow),
        arguments(past, noAction, 1L, fulfilled),
        arguments(past, badStatus, null, booked),
        arguments(past, badStatus, -1L, booked),
        arguments(past, badStatus, 1L, booked),
        arguments(future, null, null, booked),
        arguments(future, null, -1L, booked),
        arguments(future, null, 1L, booked),
        arguments(future, noShow, null, noshow),
        arguments(future, noShow, -1L, noshow),
        arguments(future, noShow, 1L, noshow),
        arguments(future, noShowAuto, null, noshow),
        arguments(future, noShowAuto, -1L, noshow),
        arguments(future, noShowAuto, 1L, noshow),
        arguments(future, cancelPat, null, cancelled),
        arguments(future, cancelPat, -1L, cancelled),
        arguments(future, cancelPat, 1L, cancelled),
        arguments(future, cancelPatAuto, null, cancelled),
        arguments(future, cancelPatAuto, -1L, cancelled),
        arguments(future, cancelPatAuto, 1L, cancelled),
        arguments(future, cancelClin, null, cancelled),
        arguments(future, cancelClin, -1L, cancelled),
        arguments(future, cancelClin, 1L, cancelled),
        arguments(future, cancelClinAuto, null, cancelled),
        arguments(future, cancelClinAuto, -1L, cancelled),
        arguments(future, cancelClinAuto, 1L, cancelled),
        arguments(future, inpatient, null, booked),
        arguments(future, inpatient, -1L, booked),
        arguments(future, inpatient, 1L, booked),
        arguments(future, noAction, null, booked),
        arguments(future, noAction, -1L, booked),
        arguments(future, noAction, 1L, booked),
        arguments(future, badStatus, null, booked),
        arguments(future, badStatus, -1L, booked),
        arguments(future, badStatus, 1L, booked));
  }

  static R4AppointmentTransformer tx(DatamartAppointment dm) {
    return R4AppointmentTransformer.builder()
        .dm(dm)
        .compositeCdwId(CompositeCdwId.fromCdwId(dm.cdwId()))
        .build();
  }

  @Test
  void appointment() {
    assertThat(tx(AppointmentSamples.Datamart.create().appointment()).toFhir())
        .isEqualTo(AppointmentSamples.R4.create().appointment());
  }

  @Test
  void empty() {
    var tx =
        R4AppointmentTransformer.builder()
            .dm(DatamartAppointment.builder().build())
            .compositeCdwId(CompositeCdwId.fromCdwId("1234:A"))
            .build();
    assertThat(tx.toFhir())
        .isEqualTo(Appointment.builder().status(Appointment.AppointmentStatus.booked).build());
  }

  @Test
  void participant() {
    var tx = tx(DatamartAppointment.builder().cdwId("123:A").build());
    assertThat(tx.participant(null, null)).isNull();
    assertThat(
            tx.participant(
                DatamartReference.builder()
                    .type(Optional.of("NotPatient"))
                    .reference(Optional.of("666"))
                    .display(Optional.of("some dude"))
                    .build(),
                Appointment.ParticipationStatus.tentative))
        .isNull();
    var ref =
        DatamartReference.builder()
            .type(Optional.of("Patient"))
            .reference(Optional.of("124"))
            .display(Optional.of("patient dude"))
            .build();
    assertThat(tx.participant(ref, Appointment.ParticipationStatus.tentative))
        .isEqualTo(
            Appointment.Participant.builder()
                .actor(asReference(ref))
                .status(Appointment.ParticipationStatus.tentative)
                .build());
  }

  @Test
  void participants() {
    var tx = tx(DatamartAppointment.builder().cdwId("1234:A").build());
    var participantRefs =
        List.of(
            DatamartReference.builder()
                .type(Optional.of("Patient"))
                .reference(Optional.of("124"))
                .display(Optional.of("patient dude"))
                .build(),
            DatamartReference.builder()
                .type(Optional.of("Location"))
                .reference(Optional.of("124"))
                .display(Optional.of("location place"))
                .build());
    assertThat(tx.participants(participantRefs))
        .isEqualTo(
            List.of(
                Appointment.Participant.builder()
                    .actor(
                        asReference(
                            DatamartReference.builder()
                                .type(Optional.of("Patient"))
                                .reference(Optional.of("124"))
                                .display(Optional.of("patient dude"))
                                .build()))
                    .status(Appointment.ParticipationStatus.accepted)
                    .build(),
                Appointment.Participant.builder()
                    .actor(
                        asReference(
                            DatamartReference.builder()
                                .type(Optional.of("Location"))
                                .reference(Optional.of("124"))
                                .display(Optional.of("location place"))
                                .build()))
                    .status(Appointment.ParticipationStatus.accepted)
                    .build()));
    tx = tx(DatamartAppointment.builder().cdwId("1234:W").build());
    assertThat(tx.participants(participantRefs))
        .isEqualTo(
            List.of(
                Appointment.Participant.builder()
                    .actor(
                        asReference(
                            DatamartReference.builder()
                                .type(Optional.of("Patient"))
                                .reference(Optional.of("124"))
                                .display(Optional.of("patient dude"))
                                .build()))
                    .status(Appointment.ParticipationStatus.tentative)
                    .build(),
                Appointment.Participant.builder()
                    .actor(
                        asReference(
                            DatamartReference.builder()
                                .type(Optional.of("Location"))
                                .reference(Optional.of("124"))
                                .display(Optional.of("location place"))
                                .build()))
                    .status(Appointment.ParticipationStatus.tentative)
                    .build()));
  }

  @Test
  void serviceCategory() {
    var tx = tx(DatamartAppointment.builder().cdwId("1234:A").build());
    assertThat(tx.serviceCategory(null)).isNull();
    assertThat(tx.serviceCategory(Optional.empty())).isNull();
    assertThat(tx.serviceCategory(Optional.of("SOMEBADDATA"))).isNull();
    assertThat(tx.serviceCategory(Optional.of("MEDICINE")))
        .isEqualTo(serviceCategory("MEDICINE", "M"));
    assertThat(tx.serviceCategory(Optional.of("NEUROLOGY")))
        .isEqualTo(serviceCategory("NEUROLOGY", "N"));
    assertThat(tx.serviceCategory(Optional.of("NONE"))).isEqualTo(serviceCategory("NONE", "0"));
    assertThat(tx.serviceCategory(Optional.of("PSYCHIATRY")))
        .isEqualTo(serviceCategory("PSYCHIATRY", "P"));
    assertThat(tx.serviceCategory(Optional.of("REHAB MEDICINE")))
        .isEqualTo(serviceCategory("REHAB MEDICINE", "R"));
    assertThat(tx.serviceCategory(Optional.of("SURGERY")))
        .isEqualTo(serviceCategory("SURGERY", "S"));
  }

  @MethodSource
  @ParameterizedTest
  void status(
      Instant startTime, String status, Long visitSid, Appointment.AppointmentStatus expected) {
    var tx = tx(DatamartAppointment.builder().cdwId("123:A").build());
    assertThat(
            tx.status(
                Optional.ofNullable(startTime),
                Optional.ofNullable(status),
                Optional.ofNullable(visitSid)))
        .isEqualTo(expected);
  }

  @Test
  void status_waitlist() {
    var tx = tx(DatamartAppointment.builder().cdwId("123:W").build());
    assertThat(tx.status(Optional.empty(), Optional.empty(), Optional.empty()))
        .isEqualTo(Appointment.AppointmentStatus.waitlist);
  }
}
