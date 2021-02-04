package gov.va.api.health.dataquery.service.controller.appointment;

import gov.va.api.health.r4.api.resources.Appointment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class R4AppointmentTransformerTest {

  @Test
  void cancelationReason() {
    fail();
  }

  @Test
  void serviceCategory() {
    fail();
  }

  @Test
  void serviceType() {
    fail();
  }

  @Test
  void minutesDuration() {
    fail();
  }

  @Test
  void specialty() {
    fail();
  }

  @Test
  void appointmentType() {
    fail();
  }

  @Test
  void description() {
    fail();
  }

  @Test
  void comment() {
    fail();
  }

  @Test
  void participants() {}

  @Test
  void appointment() {
    assertThat(tx(AppointmentSamples.Datamart.create().appointment()).toFhir())
            .isEqualTo(AppointmentSamples.R4.create().appointment());
  }

  @Test
  void empty() {
    assertThat(tx(DatamartAppointment.builder().build()).toFhir())
        .isEqualTo(Appointment.builder().resourceType("Appointment").build());
  }

  R4AppointmentTransformer tx(DatamartAppointment dm) {
    return R4AppointmentTransformer.builder().dm(dm).build();
  }
}
