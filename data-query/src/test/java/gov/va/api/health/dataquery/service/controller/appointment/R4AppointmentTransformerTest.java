package gov.va.api.health.dataquery.service.controller.appointment;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.R4Transformers;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class R4AppointmentTransformerTest {
  @Test
  void appointment() {
    assertThat(tx(AppointmentSamples.Datamart.create().appointment()).toFhir())
        .isEqualTo(AppointmentSamples.R4.create().appointment());
  }

  @Test
  void empty() {
    var tx =
        R4AppointmentTransformer.builder()
            .compositeCdwId(CompositeCdwId.fromCdwId("1234:A"))
            .dm(DatamartAppointment.builder().build())
            .build();
    assertThat(tx.toFhir()).isEqualTo(Appointment.builder().resourceType("Appointment").build());
  }

  @Test
  void participant() {
    var tx =
        R4AppointmentTransformer.builder()
            .compositeCdwId(CompositeCdwId.fromCdwId("123:A"))
            .dm(DatamartAppointment.builder().build())
            .build();
    assertThat(tx.participant(null, null)).isNull();
    assertThat(
            tx.participant(
                DatamartReference.builder()
                    .type(Optional.of("NotPatient"))
                    .reference(Optional.of("666"))
                    .display(Optional.of("some dude"))
                    .build(),
                Appointment.ParticipationStatus.tentative))
        .isEqualTo(null);
    var ref =
        DatamartReference.builder()
            .type(Optional.of("Patient"))
            .reference(Optional.of("124"))
            .display(Optional.of("patient dude"))
            .build();
    assertThat(tx.participant(ref, Appointment.ParticipationStatus.tentative))
        .isEqualTo(
            Appointment.Participant.builder()
                .actor(R4Transformers.asReference(ref))
                .status(Appointment.ParticipationStatus.tentative)
                .build());
  }

  @Test
  void participants() {
    var tx =
        R4AppointmentTransformer.builder()
            .compositeCdwId(CompositeCdwId.fromCdwId("1234:A"))
            .dm(DatamartAppointment.builder().build())
            .build();
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
                        R4Transformers.asReference(
                            DatamartReference.builder()
                                .type(Optional.of("Patient"))
                                .reference(Optional.of("124"))
                                .display(Optional.of("patient dude"))
                                .build()))
                    .status(Appointment.ParticipationStatus.accepted)
                    .build(),
                Appointment.Participant.builder()
                    .actor(
                        R4Transformers.asReference(
                            DatamartReference.builder()
                                .type(Optional.of("Location"))
                                .reference(Optional.of("124"))
                                .display(Optional.of("location place"))
                                .build()))
                    .status(Appointment.ParticipationStatus.accepted)
                    .build()));
    tx =
        R4AppointmentTransformer.builder()
            .compositeCdwId(CompositeCdwId.fromCdwId("1234:W"))
            .dm(DatamartAppointment.builder().build())
            .build();
    assertThat(tx.participants(participantRefs))
        .isEqualTo(
            List.of(
                Appointment.Participant.builder()
                    .actor(
                        R4Transformers.asReference(
                            DatamartReference.builder()
                                .type(Optional.of("Patient"))
                                .reference(Optional.of("124"))
                                .display(Optional.of("patient dude"))
                                .build()))
                    .status(Appointment.ParticipationStatus.tentative)
                    .build(),
                Appointment.Participant.builder()
                    .actor(
                        R4Transformers.asReference(
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
    var tx =
        R4AppointmentTransformer.builder()
            .compositeCdwId(CompositeCdwId.fromCdwId("1234:A"))
            .dm(DatamartAppointment.builder().build())
            .build();
    assertThat(tx.serviceCategory(null)).isNull();
    assertThat(
            Appointment.builder()
                .serviceCategory(tx.serviceCategory(Optional.of("SURGERY")))
                .build())
        .isEqualTo(
            Appointment.builder()
                .serviceCategory(
                    List.of(
                        CodeableConcept.builder()
                            .coding(
                                List.of(
                                    Coding.builder()
                                        .display("SURGERY")
                                        .code("S")
                                        .system(
                                            "http://www.va.gov/Terminology/VistADefinedTerms/44-9")
                                        .build()))
                            .build()))
                .build());
    assertThat(
            Appointment.builder()
                .serviceCategory(tx.serviceCategory(Optional.of("MEDICINE")))
                .build())
        .isEqualTo(
            Appointment.builder()
                .serviceCategory(
                    List.of(
                        CodeableConcept.builder()
                            .coding(
                                List.of(
                                    Coding.builder()
                                        .display("MEDICINE")
                                        .code("M")
                                        .system(
                                            "http://www.va.gov/Terminology/VistADefinedTerms/44-9")
                                        .build()))
                            .build()))
                .build());
    assertThat(
            Appointment.builder()
                .serviceCategory(tx.serviceCategory(Optional.of("NEUROLOGY")))
                .build())
        .isEqualTo(
            Appointment.builder()
                .serviceCategory(
                    List.of(
                        CodeableConcept.builder()
                            .coding(
                                List.of(
                                    Coding.builder()
                                        .display("NEUROLOGY")
                                        .code("N")
                                        .system(
                                            "http://www.va.gov/Terminology/VistADefinedTerms/44-9")
                                        .build()))
                            .build()))
                .build());
    assertThat(
            Appointment.builder().serviceCategory(tx.serviceCategory(Optional.of("NONE"))).build())
        .isEqualTo(
            Appointment.builder()
                .serviceCategory(
                    List.of(
                        CodeableConcept.builder()
                            .coding(
                                List.of(
                                    Coding.builder()
                                        .display("NONE")
                                        .code("0")
                                        .system(
                                            "http://www.va.gov/Terminology/VistADefinedTerms/44-9")
                                        .build()))
                            .build()))
                .build());
    assertThat(
            Appointment.builder()
                .serviceCategory(tx.serviceCategory(Optional.of("PSYCHIATRY")))
                .build())
        .isEqualTo(
            Appointment.builder()
                .serviceCategory(
                    List.of(
                        CodeableConcept.builder()
                            .coding(
                                List.of(
                                    Coding.builder()
                                        .display("PSYCHIATRY")
                                        .code("P")
                                        .system(
                                            "http://www.va.gov/Terminology/VistADefinedTerms/44-9")
                                        .build()))
                            .build()))
                .build());
    assertThat(
            Appointment.builder()
                .serviceCategory(tx.serviceCategory(Optional.of("REHAB MEDICINE")))
                .build())
        .isEqualTo(
            Appointment.builder()
                .serviceCategory(
                    List.of(
                        CodeableConcept.builder()
                            .coding(
                                List.of(
                                    Coding.builder()
                                        .display("REHAB MEDICINE")
                                        .code("R")
                                        .system(
                                            "http://www.va.gov/Terminology/VistADefinedTerms/44-9")
                                        .build()))
                            .build()))
                .build());
    assertThat(
            Appointment.builder()
                .serviceCategory(tx.serviceCategory(Optional.of("SOMEBADDATA")))
                .build())
        .isEqualTo(Appointment.builder().serviceCategory(null).build());
  }

  @Test
  void status() {
    Instant startInPast = Instant.now().minus(Duration.ofDays(2));
    Instant startInFuture = Instant.now().plus(Duration.ofDays(2));
    var tx =
        R4AppointmentTransformer.builder()
            .compositeCdwId(CompositeCdwId.fromCdwId("123:A"))
            .dm(DatamartAppointment.builder().build())
            .build();
    assertThat(tx.status(Optional.of(startInPast), Optional.empty(), Optional.of(1L)))
        .isEqualTo(Appointment.AppointmentStatus.fulfilled);
    assertThat(tx.status(Optional.of(startInPast), Optional.of("NO SHOW"), Optional.of(-1L)))
        .isEqualTo(Appointment.AppointmentStatus.noshow);
    assertThat(
            tx.status(
                Optional.of(startInPast), Optional.of("NO-SHOW & AUTO RE-BOOK"), Optional.of(-1L)))
        .isEqualTo(Appointment.AppointmentStatus.noshow);
    assertThat(
            tx.status(
                Optional.of(startInPast), Optional.of("CANCELLED BY PATIENT"), Optional.of(-1L)))
        .isEqualTo(Appointment.AppointmentStatus.cancelled);
    assertThat(
            tx.status(
                Optional.of(startInPast),
                Optional.of("CANCELLED BY PATIENT & AUTO-REBOOK"),
                Optional.of(-1L)))
        .isEqualTo(Appointment.AppointmentStatus.cancelled);
    assertThat(
            tx.status(
                Optional.of(startInPast), Optional.of("CANCELLED BY CLINIC"), Optional.of(-1L)))
        .isEqualTo(Appointment.AppointmentStatus.cancelled);
    assertThat(
            tx.status(
                Optional.of(startInPast),
                Optional.of("CANCELLED BY CLINIC & AUTO RE-BOOK"),
                Optional.of(-1L)))
        .isEqualTo(Appointment.AppointmentStatus.cancelled);
    assertThat(
            tx.status(
                Optional.of(startInFuture), Optional.of("INPATIENT APPOINTMENT"), Optional.of(-1L)))
        .isEqualTo(Appointment.AppointmentStatus.booked);
    assertThat(tx.status(Optional.empty(), Optional.of("INPATIENT APPOINTMENT"), Optional.of(-1L)))
        .isEqualTo(Appointment.AppointmentStatus.booked);
    assertThat(
            tx.status(
                Optional.of(startInPast), Optional.of("INPATIENT APPOINTMENT"), Optional.of(1L)))
        .isEqualTo(Appointment.AppointmentStatus.fulfilled);
    assertThat(
            tx.status(
                Optional.of(startInPast), Optional.of("INPATIENT APPOINTMENT"), Optional.of(-1L)))
        .isEqualTo(Appointment.AppointmentStatus.noshow);
    assertThat(tx.status(Optional.of(startInPast), Optional.of("NO ACTION TAKEN"), Optional.of(1L)))
        .isEqualTo(Appointment.AppointmentStatus.fulfilled);
    assertThat(tx.status(Optional.empty(), Optional.of("NO ACTION TAKEN"), Optional.of(-1L)))
        .isEqualTo(Appointment.AppointmentStatus.booked);
    assertThat(
            tx.status(
                Optional.of(Instant.parse("2020-11-25T08:00:00Z")),
                Optional.of("WTF MAN?"),
                Optional.empty()))
        .isEqualTo(null);
    tx =
        R4AppointmentTransformer.builder()
            .compositeCdwId(CompositeCdwId.fromCdwId("123:W"))
            .dm(DatamartAppointment.builder().build())
            .build();
    assertThat(tx.status(Optional.of(startInFuture), Optional.of("NO SHOW"), Optional.of(-1L)))
        .isEqualTo(Appointment.AppointmentStatus.waitlist);
  }

  R4AppointmentTransformer tx(DatamartAppointment dm) {
    return R4AppointmentTransformer.builder()
        .compositeCdwId(CompositeCdwId.fromCdwId("1234:A"))
        .dm(dm)
        .build();
  }
}
