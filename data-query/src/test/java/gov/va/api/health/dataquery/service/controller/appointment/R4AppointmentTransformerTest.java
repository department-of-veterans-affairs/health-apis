package gov.va.api.health.dataquery.service.controller.appointment;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.resources.Appointment;
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
    Instant startInPast = Instant.now().minus(Duration.ofDays(2));
    Instant startInFuture = Instant.now().plus(Duration.ofDays(2));
    return Stream.of(
        arguments(
            Optional.of(startInPast),
            Optional.empty(),
            Optional.of(1L),
            Appointment.AppointmentStatus.fulfilled),
        arguments(
            Optional.of(startInPast),
            Optional.of("NO SHOW"),
            Optional.of(-1L),
            Appointment.AppointmentStatus.noshow),
        arguments(
            Optional.of(startInPast),
            Optional.of("NO-SHOW & AUTO RE-BOOK"),
            Optional.of(-1L),
            Appointment.AppointmentStatus.noshow),
        arguments(
            Optional.of(startInPast),
            Optional.of("CANCELLED BY PATIENT"),
            Optional.of(-1L),
            Appointment.AppointmentStatus.cancelled),
        arguments(
            Optional.of(startInPast),
            Optional.of("CANCELLED BY PATIENT & AUTO-REBOOK"),
            Optional.of(-1L),
            Appointment.AppointmentStatus.cancelled),
        arguments(
            Optional.of(startInPast),
            Optional.of("CANCELLED BY CLINIC"),
            Optional.of(-1L),
            Appointment.AppointmentStatus.cancelled),
        arguments(
            Optional.of(startInPast),
            Optional.of("CANCELLED BY CLINIC & AUTO RE-BOOK"),
            Optional.of(-1L),
            Appointment.AppointmentStatus.cancelled),
        arguments(
            Optional.of(startInFuture),
            Optional.of("INPATIENT APPOINTMENT"),
            Optional.of(-1L),
            Appointment.AppointmentStatus.booked),
        arguments(
            Optional.empty(),
            Optional.of("INPATIENT APPOINTMENT"),
            Optional.of(-1L),
            Appointment.AppointmentStatus.booked),
        arguments(
            Optional.of(startInPast),
            Optional.of("INPATIENT APPOINTMENT"),
            Optional.of(1L),
            Appointment.AppointmentStatus.fulfilled),
        arguments(
            Optional.of(startInPast),
            Optional.of("INPATIENT APPOINTMENT"),
            Optional.of(-1L),
            Appointment.AppointmentStatus.noshow),
        arguments(
            Optional.of(startInPast),
            Optional.of("NO ACTION TAKEN"),
            Optional.of(1L),
            Appointment.AppointmentStatus.fulfilled),
        arguments(
            Optional.empty(),
            Optional.of("NO ACTION TAKEN"),
            Optional.of(-1L),
            Appointment.AppointmentStatus.booked),
        arguments(Optional.of(startInPast), Optional.of("WTF MAN?"), Optional.empty(), null),
        arguments(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Appointment.AppointmentStatus.booked),
        arguments(
            Optional.of(startInFuture),
            Optional.of("NO ACTION TAKEN"),
            Optional.of(1L),
            Appointment.AppointmentStatus.booked),
        arguments(
            Optional.of(startInFuture),
            Optional.empty(),
            Optional.empty(),
            Appointment.AppointmentStatus.booked),
        arguments(
            Optional.empty(),
            Optional.of("INPATIENT APPOINTMENT"),
            Optional.empty(),
            Appointment.AppointmentStatus.booked),
        arguments(Optional.empty(), Optional.of("WTF MAN?"), Optional.empty(), null),
        arguments(Optional.of(startInPast), Optional.of("WTF MAN?"), Optional.empty(), null),
        arguments(
            Optional.of(startInPast),
            Optional.of("INPATIENT APPOINTMENT"),
            Optional.empty(),
            Appointment.AppointmentStatus.booked),
        arguments(
            Optional.of(startInPast),
            Optional.of("INPATIENT APPOINTMENT"),
            Optional.of(-1L),
            Appointment.AppointmentStatus.noshow),
        arguments(
            Optional.of(startInPast),
            Optional.of("INPATIENT APPOINTMENT"),
            Optional.of(1L),
            Appointment.AppointmentStatus.fulfilled));
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

  @ParameterizedTest
  @MethodSource
  void status(
      Optional<Instant> startTime,
      Optional<String> status,
      Optional<Long> visitSid,
      Appointment.AppointmentStatus actual) {
    var tx = tx(DatamartAppointment.builder().cdwId("123:A").build());
    assertThat(tx.status(startTime, status, visitSid)).isEqualTo(actual);
  }

  @Test
  void waitlistStatus() {
    Instant startInFuture = Instant.now().plus(Duration.ofDays(2));
    var tx = tx(DatamartAppointment.builder().cdwId("123:A").build());
    tx = tx(DatamartAppointment.builder().cdwId("123:W").build());
    assertThat(tx.status(Optional.of(startInFuture), Optional.of("NO SHOW"), Optional.of(-1L)))
        .isEqualTo(Appointment.AppointmentStatus.waitlist);
  }
}
