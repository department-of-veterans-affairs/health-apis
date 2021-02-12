package gov.va.api.health.dataquery.service.controller.appointment;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.R4Transformers;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.datamart.DatamartReference;
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
  void status() {
    var tx =
        R4AppointmentTransformer.builder()
            .compositeCdwId(CompositeCdwId.fromCdwId("123:A"))
            .dm(DatamartAppointment.builder().build())
            .build();
    assertThat(
            tx.status(
                Optional.of(Instant.parse("2020-11-25T08:00:00Z")),
                Optional.of(Instant.parse("2020-11-26T08:00:00Z")),
                Optional.empty()))
        .isEqualTo(Appointment.AppointmentStatus.fulfilled);
    assertThat(
            tx.status(
                Optional.of(Instant.parse("2020-11-25T08:00:00Z")),
                Optional.of(Instant.parse("2020-11-26T08:00:00Z")),
                Optional.of("NO SHOW")))
        .isEqualTo(Appointment.AppointmentStatus.noshow);
    assertThat(
            tx.status(
                Optional.of(Instant.parse("2020-11-25T08:00:00Z")),
                Optional.of(Instant.parse("2020-11-26T08:00:00Z")),
                Optional.of("NO-SHOW & AUTO RE-BOOK")))
        .isEqualTo(Appointment.AppointmentStatus.noshow);
    assertThat(
            tx.status(
                Optional.of(Instant.parse("2020-11-25T08:00:00Z")),
                Optional.of(Instant.parse("2020-11-26T08:00:00Z")),
                Optional.of("CANCELLED BY PATIENT")))
        .isEqualTo(Appointment.AppointmentStatus.cancelled);
    assertThat(
            tx.status(
                Optional.of(Instant.parse("2020-11-25T08:00:00Z")),
                Optional.of(Instant.parse("2020-11-26T08:00:00Z")),
                Optional.of("CANCELLED BY PATIENT & AUTO-REBOOK")))
        .isEqualTo(Appointment.AppointmentStatus.cancelled);
    assertThat(
            tx.status(
                Optional.of(Instant.parse("2020-11-25T08:00:00Z")),
                Optional.of(Instant.parse("2020-11-26T08:00:00Z")),
                Optional.of("CANCELLED BY CLINIC")))
        .isEqualTo(Appointment.AppointmentStatus.cancelled);
    assertThat(
            tx.status(
                Optional.of(Instant.parse("2020-11-25T08:00:00Z")),
                Optional.of(Instant.parse("2020-11-26T08:00:00Z")),
                Optional.of("CANCELLED BY CLINIC & AUTO RE-BOOK")))
        .isEqualTo(Appointment.AppointmentStatus.cancelled);
    assertThat(
            tx.status(
                Optional.of(Instant.parse("2020-11-25T08:00:00Z")),
                Optional.empty(),
                Optional.of("INPATIENT APPOINTMENT")))
        .isEqualTo(Appointment.AppointmentStatus.arrived);
    assertThat(tx.status(Optional.empty(), Optional.empty(), Optional.of("INPATIENT APPOINTMENT")))
        .isEqualTo(Appointment.AppointmentStatus.booked);
    assertThat(
            tx.status(
                Optional.of(Instant.parse("2020-11-25T08:00:00Z")),
                Optional.of(Instant.parse("2020-11-26T08:00:00Z")),
                Optional.of("INPATIENT APPOINTMENT")))
        .isEqualTo(Appointment.AppointmentStatus.fulfilled);
    assertThat(
            tx.status(
                Optional.of(Instant.parse("2020-11-25T08:00:00Z")),
                Optional.empty(),
                Optional.of("NO ACTION TAKEN")))
        .isEqualTo(Appointment.AppointmentStatus.arrived);
    assertThat(tx.status(Optional.empty(), Optional.empty(), Optional.of("NO ACTION TAKEN")))
        .isEqualTo(Appointment.AppointmentStatus.booked);
    assertThat(
            tx.status(
                Optional.of(Instant.parse("2020-11-25T08:00:00Z")),
                Optional.of(Instant.parse("2020-11-26T08:00:00Z")),
                Optional.of("NO ACTION TAKEN")))
        .isEqualTo(Appointment.AppointmentStatus.fulfilled);
    assertThat(
            tx.status(
                Optional.of(Instant.parse("2020-11-25T08:00:00Z")),
                Optional.of(Instant.parse("2020-11-26T08:00:00Z")),
                Optional.of("WTF MAN?")))
        .isEqualTo(null);

    tx =
        R4AppointmentTransformer.builder()
            .compositeCdwId(CompositeCdwId.fromCdwId("123:W"))
            .dm(DatamartAppointment.builder().build())
            .build();
    assertThat(
            tx.status(
                Optional.of(Instant.parse("2020-11-25T08:00:00Z")),
                Optional.of(Instant.parse("2020-11-26T08:00:00Z")),
                Optional.of("NO SHOW")))
        .isEqualTo(Appointment.AppointmentStatus.waitlist);
  }

  R4AppointmentTransformer tx(DatamartAppointment dm) {
    return R4AppointmentTransformer.builder()
        .compositeCdwId(CompositeCdwId.fromCdwId("1234:A"))
        .dm(dm)
        .build();
  }
}